import re
from collections import Counter, defaultdict

from bake import exact_rect, inside, pixel_rect, texel_density
from pngio import BACKGROUND, blank, opaque, read_png

HEX = re.compile(r"^#[0-9A-Fa-f]{6}$")


def luminance(rgb):
    return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]


def hue_bucket(rgb):
    r, g, b = [c / 255 for c in rgb]
    hi, lo = max(r, g, b), min(r, g, b)
    chroma = hi - lo
    if chroma < 0.08:
        return "grey"
    if hi == r:
        hue = ((g - b) / chroma) % 6
    elif hi == g:
        hue = (b - r) / chroma + 2
    else:
        hue = (r - g) / chroma + 4
    return f"hue{int(hue * 60) // 30 * 30:03d}"


def opaque_colours(pixels):
    return Counter(px[:3] for row in pixels for px in row if opaque(px))


def palette_ramps(pixels):
    ramps = defaultdict(list)
    for rgb, count in opaque_colours(pixels).items():
        ramps[hue_bucket(rgb)].append((rgb, count))
    return {bucket: sorted(entries, key=lambda e: luminance(e[0])) for bucket, entries in ramps.items()}


def parse_hex(token):
    token = token.lstrip("#")
    return tuple(int(token[i:i + 2], 16) for i in (0, 2, 4))


def read_palette(path):
    if path.lower().endswith(".png"):
        return set(opaque_colours(read_png(path)[2]))
    with open(path) as handle:
        return {parse_hex(tok) for line in handle for tok in line.split() if HEX.match(tok)}


def check_sheet(cubes, tex_w, tex_h, texture, palette=None):
    width, height, pixels = texture
    scale_u, scale_v = width / tex_w, height / tex_h
    covered = [[False] * width for _ in range(height)]
    report = {"holes": [], "unpainted": [], "off_sheet": [], "density": [], "drift": {}, "stray": 0}
    densities = []
    for cube in cubes:
        density = texel_density(cube, scale_u)
        if density is not None:
            densities.append((density, cube))
        for face, _, patch in cube["faces"]:
            x0, y0, x1, y1 = pixel_rect(patch, scale_u, scale_v)
            label = f"{cube['bone']}[{cube['index']}].{face} px x{x0}-{x1} y{y0}-{y1}"
            if x0 < 0 or y0 < 0 or x1 > width or y1 > height:
                report["off_sheet"].append(label)
            exact = exact_rect(patch, scale_u, scale_v)
            total = clear = 0
            for y in range(max(y0, 0), min(y1, height)):
                for x in range(max(x0, 0), min(x1, width)):
                    covered[y][x] = True
                    if not inside(exact, x + 0.5, y + 0.5):
                        continue
                    total += 1
                    clear += not opaque(pixels[y][x])
            if total and clear == total:
                report["unpainted"].append(label)
            elif clear:
                report["holes"].append(f"{label}: {clear}/{total} clear")
    report["stray"] = sum(1 for y in range(height) for x in range(width) if opaque(pixels[y][x]) and not covered[y][x])
    if densities:
        densities.sort(key=lambda e: e[0])
        median = densities[len(densities) // 2][0]
        report["median_density"] = median
        report["density_range"] = (densities[0][0], densities[-1][0])
        report["density"] = [(d, f"{c['bone']}[{c['index']}]") for d, c in densities if d < median * 0.5 or d > median * 2]
    if palette is not None:
        report["drift"] = {rgb: n for rgb, n in opaque_colours(pixels).items() if rgb not in palette}
    return report


def problem_groups(report, strict=False, fail_unpainted=False):
    groups = 0
    if report["drift"]:
        groups += 1
    if fail_unpainted and report["unpainted"]:
        groups += 1
    if strict:
        groups += bool(report["holes"]) + bool(report["stray"]) + bool(report["off_sheet"])
    return groups


def diff_sheets(a, b):
    wa, ha, pa = a
    wb, hb, pb = b
    if (wa, ha) != (wb, hb):
        raise SystemExit(f"size differs: {wa}x{ha} vs {wb}x{hb}")
    changed = 0
    out = blank(wa, ha)
    for y in range(ha):
        for x in range(wa):
            if pa[y][x] != pb[y][x] and (opaque(pa[y][x]) or opaque(pb[y][x])):
                changed += 1
                out[y][x] = (255, 0, 255, 255)
            else:
                r, g, bl, al = pa[y][x]
                out[y][x] = (r // 3, g // 3, bl // 3, 255) if opaque(pa[y][x]) else BACKGROUND
    return changed, out


def parse_block(path):
    palette, rows, in_rows = {".": None}, [], False
    with open(path) as handle:
        for raw in handle:
            line = raw.rstrip("\r\n")
            if in_rows:
                rows.append(line)
                continue
            if not line.strip():
                if line:
                    raise SystemExit(f"{path}: a block cannot start with a keep-only row; paint the first row or move the block down")
                continue
            if line.lstrip().startswith("#"):
                continue
            if "=" in line and len(line.split("=")[0].strip()) == 1:
                key, value = [part.strip() for part in line.split("=", 1)]
                if value and not HEX.match(value) and value.lower() not in ("transparent", "none"):
                    raise SystemExit(f"{path}: palette entry {key!r} needs #RRGGBB or transparent, got {value!r}")
                palette[key] = None if value.lower() in ("transparent", "none", "") else parse_hex(value)
                continue
            in_rows = True
            rows.append(line)
    while rows and not rows[-1].strip():
        rows.pop()
    return palette, rows


def apply_block(pixels, width, height, palette, rows, u, v):
    written = 0
    for dy, row in enumerate(rows):
        for dx, char in enumerate(row):
            if char == " ":
                continue
            if char not in palette:
                raise SystemExit(f"no palette entry for {char!r}")
            x, y = u + dx, v + dy
            if not (0 <= x < width and 0 <= y < height):
                raise SystemExit(f"block runs off the texture at {x},{y}")
            colour = palette[char]
            pixels[y][x] = (0, 0, 0, 0) if colour is None else colour + (255,)
            written += 1
    return written
