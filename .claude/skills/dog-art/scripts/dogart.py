#!/usr/bin/env python3
"""Headless render, UV map and texture checks for GeckoLib dog rigs. Standard library only.

Commands:
  render     geo png out.png [--view front|left|right|back|top | --yaw D --pitch D] [--scale N] [--spin 180]
  turntable  geo png out.png [--views left,front34,front,top] [--scale N] [--spin 180]
  faces      geo png [--profile rig.json]
  uvmap      geo png out.png [--profile rig.json] [--zoom N]
  palette    png
  check      geo png [--palette file]
  diff       a.png b.png [out.png]
  patch      base.png block.txt out.png --at U V
"""
from __future__ import annotations

import argparse
import json
import math
import struct
import sys
import zlib
from collections import Counter, defaultdict

SLOT_COLOURS = {
    "head": (255, 99, 71), "mouth": (255, 165, 0), "eyes": (255, 255, 0),
    "neck": (154, 205, 50), "torso": (60, 179, 113), "tail": (0, 206, 209),
    "ear_left": (30, 144, 255), "ear_right": (138, 43, 226),
    "leg_front_left": (255, 105, 180), "leg_front_right": (205, 92, 92),
    "leg_back_left": (210, 180, 140), "leg_back_right": (176, 196, 222),
    "fur_shaggy": (240, 230, 140), "fur_overgrown": (189, 183, 107),
}
UNSLOTTED_COLOUR = (128, 128, 128)
BACKGROUND = (58, 62, 70, 255)
VIEWS = {"front": (0, 0), "left": (90, 0), "back": (180, 0), "right": (270, 0),
         "top": (0, 89), "front34": (40, 18), "back34": (220, 18)}


def read_png(path):
    with open(path, "rb") as handle:
        data = handle.read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"{path} is not a PNG")
    pos, chunks, palette, trns = 8, [], None, None
    width = height = depth = colour = interlace = None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b"IHDR":
            width, height, depth, colour, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif kind == b"PLTE":
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif kind == b"tRNS":
            trns = body
        elif kind == b"IDAT":
            chunks.append(body)
        elif kind == b"IEND":
            break
    if depth != 8 or interlace:
        raise SystemExit(f"{path}: only 8-bit non-interlaced PNGs are supported")
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colour]
    raw = zlib.decompress(b"".join(chunks))
    stride = width * channels
    rows, previous = [], bytearray(stride)
    for y in range(height):
        start = y * (stride + 1)
        filter_type = raw[start]
        line = bytearray(raw[start + 1:start + 1 + stride])
        for i in range(stride):
            left = line[i - channels] if i >= channels else 0
            up = previous[i]
            up_left = previous[i - channels] if i >= channels else 0
            if filter_type == 1:
                line[i] = (line[i] + left) & 255
            elif filter_type == 2:
                line[i] = (line[i] + up) & 255
            elif filter_type == 3:
                line[i] = (line[i] + ((left + up) >> 1)) & 255
            elif filter_type == 4:
                p = left + up - up_left
                pa, pb, pc = abs(p - left), abs(p - up), abs(p - up_left)
                pred = left if pa <= pb and pa <= pc else up if pb <= pc else up_left
                line[i] = (line[i] + pred) & 255
        rows.append(to_rgba(line, channels, colour, palette, trns))
        previous = line
    return width, height, rows


def to_rgba(line, channels, colour, palette, trns):
    out = []
    for i in range(0, len(line), channels):
        px = line[i:i + channels]
        if colour == 6:
            out.append((px[0], px[1], px[2], px[3]))
        elif colour == 2:
            out.append((px[0], px[1], px[2], 255))
        elif colour == 0:
            out.append((px[0], px[0], px[0], 255))
        elif colour == 4:
            out.append((px[0], px[0], px[0], px[1]))
        else:
            r, g, b = palette[px[0]]
            alpha = trns[px[0]] if trns and px[0] < len(trns) else 255
            out.append((r, g, b, alpha))
    return out


def write_png(path, width, height, rows):
    raw = bytearray()
    for row in rows:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))

    def chunk(kind, body):
        return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body) & 0xFFFFFFFF)

    with open(path, "wb") as handle:
        handle.write(b"\x89PNG\r\n\x1a\n")
        handle.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
        handle.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        handle.write(chunk(b"IEND", b""))


def blank(width, height, colour=BACKGROUND):
    return [[colour] * width for _ in range(height)]


def load_geo(path):
    with open(path) as handle:
        geometry = json.load(handle)["minecraft:geometry"][0]
    description = geometry["description"]
    bones = {bone["name"]: bone for bone in geometry["bones"]}
    return description.get("texture_width", 64), description.get("texture_height", 64), geometry["bones"], bones


def load_profile(path):
    if not path:
        return {}
    with open(path) as handle:
        profile = json.load(handle)
    return {bone: slot for slot, bones in profile.items() if isinstance(bones, list) for bone in bones}


def rotation_matrix(degrees):
    rx, ry, rz = (math.radians(-degrees[0]), math.radians(-degrees[1]), math.radians(degrees[2]))
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = ((1, 0, 0), (0, cx, -sx), (0, sx, cx))
    my = ((cy, 0, sy), (0, 1, 0), (-sy, 0, cy))
    mz = ((cz, -sz, 0), (sz, cz, 0), (0, 0, 1))
    return mat_mul(mz, mat_mul(my, mx))


def mat_mul(a, b):
    return tuple(tuple(sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)) for i in range(3))


def apply(matrix, pivot, point):
    d = (point[0] - pivot[0], point[1] - pivot[1], point[2] - pivot[2])
    return tuple(sum(matrix[i][k] * d[k] for k in range(3)) + pivot[i] for i in range(3))


def bone_chain(bones, name):
    chain = []
    while name:
        bone = bones[name]
        chain.append(bone)
        name = bone.get("parent")
    return chain


def model_pivot(pivot):
    return (-pivot[0], pivot[1], pivot[2])


FACES = ("west", "north", "east", "south", "up", "down")
NORMALS = {"west": (-1, 0, 0), "east": (1, 0, 0), "north": (0, 0, -1), "south": (0, 0, 1), "up": (0, 1, 0), "down": (0, -1, 0)}


def face_patches(cube, mirror):
    sx, sy, sz = cube["size"]
    if isinstance(cube.get("uv"), dict):
        patches = {}
        for face, spec in cube["uv"].items():
            u, v = spec["uv"]
            w, h = spec.get("uv_size", (sx if face in ("north", "south", "up", "down") else sz, sy if face not in ("up", "down") else sz))
            patches[face] = (u, v, w, h)
        return patches
    u, v = cube["uv"]
    first, third = ("east", "west") if mirror else ("west", "east")
    return {
        first: (u, v + sz, sz, sy),
        "north": (u + sz, v + sz, sx, sy),
        third: (u + sz + sx, v + sz, sz, sy),
        "south": (u + 2 * sz + sx, v + sz, sx, sy),
        "up": (u + sz, v, sx, sz),
        "down": (u + sz + sx, v, sx, sz),
    }


def face_corners(lo, hi, face):
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    if face == "north":
        return [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)]
    if face == "south":
        return [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)]
    if face == "west":
        return [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)]
    if face == "east":
        return [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)]
    if face == "up":
        return [(x1, y1, z1), (x0, y1, z1), (x0, y1, z0), (x1, y1, z0)]
    return [(x1, y0, z0), (x0, y0, z0), (x0, y0, z1), (x1, y0, z1)]


def cubes_of(geo_path, profile_path=None):
    tex_w, tex_h, bone_list, bones = load_geo(geo_path)
    slots = load_profile(profile_path)
    result = []
    for bone in bone_list:
        chain = bone_chain(bones, bone["name"])
        bone_mirror = any(b.get("mirror") for b in chain)
        for index, cube in enumerate(bone.get("cubes", [])):
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            inflate = float(cube.get("inflate", 0))
            lo = (-(ox + sx) - inflate, oy - inflate, oz - inflate)
            hi = (-ox + inflate, oy + sy + inflate, oz + sz + inflate)
            transforms = []
            if cube.get("rotation"):
                pivot = cube.get("pivot", [ox + sx / 2, oy + sy / 2, oz + sz / 2])
                transforms.append((rotation_matrix(cube["rotation"]), model_pivot(pivot)))
            for b in chain:
                if b.get("rotation"):
                    transforms.append((rotation_matrix(b["rotation"]), model_pivot(b.get("pivot", (0, 0, 0)))))
            mirror = bool(cube.get("mirror", bone_mirror))
            faces = []
            for face in FACES:
                corners = face_corners(lo, hi, face)
                for matrix, pivot in transforms:
                    corners = [apply(matrix, pivot, c) for c in corners]
                faces.append((face, corners, face_patches(cube, mirror)[face]))
            visual = tuple(round(s + 2 * inflate, 3) for s in (sx, sy, sz))
            result.append({
                "bone": bone["name"], "index": index, "slot": slots.get(bone["name"]),
                "size": (sx, sy, sz), "visual": visual, "inflate": inflate, "mirror": mirror,
                "faces": faces, "rotated": bool(transforms),
            })
    return tex_w, tex_h, result


def camera(yaw, pitch):
    yaw_r, pitch_r = math.radians(yaw), math.radians(min(max(pitch, -89), 89))
    toward = (math.sin(yaw_r) * math.cos(pitch_r), math.sin(pitch_r), -math.cos(yaw_r) * math.cos(pitch_r))
    forward = tuple(-c for c in toward)
    right = normalize(cross(forward, (0, 1, 0)))
    up = cross(right, forward)
    return right, up, toward


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def normalize(v):
    length = math.sqrt(dot(v, v)) or 1
    return (v[0] / length, v[1] / length, v[2] / length)


def shade(normal):
    nx, ny, nz = normal
    weight = abs(nx) + abs(ny) + abs(nz) or 1
    vertical = ny if ny > 0 else -ny * 0.5
    return (abs(nx) * 0.6 + abs(nz) * 0.8 + vertical) / weight


def render_view(cubes, texture, tex_w, tex_h, yaw, pitch, scale, margin=2):
    width, height, pixels = texture
    scale_u, scale_v = width / tex_w, height / tex_h
    right, up, toward = camera(yaw, pitch)
    projected = []
    for cube in cubes:
        for face, corners, patch in cube["faces"]:
            pts = [(dot(c, right), dot(c, up), dot(c, toward)) for c in corners]
            normal = normalize(cross(sub(corners[1], corners[0]), sub(corners[3], corners[0])))
            projected.append((pts, patch, shade(normal)))
    xs = [p[0] for pts, _, _ in projected for p in pts]
    ys = [p[1] for pts, _, _ in projected for p in pts]
    min_x, max_x, min_y, max_y = min(xs), max(xs), min(ys), max(ys)
    img_w = int(math.ceil((max_x - min_x + 2 * margin) * scale))
    img_h = int(math.ceil((max_y - min_y + 2 * margin) * scale))
    image = blank(img_w, img_h)
    depth = [[-1e9] * img_w for _ in range(img_h)]
    for pts, (pu, pv, pw, ph), brightness in projected:
        screen = [((x - min_x + margin) * scale, (max_y - y + margin) * scale, z) for x, y, z in pts]
        uvs = [(pu, pv), (pu + pw, pv), (pu + pw, pv + ph), (pu, pv + ph)]
        for tri in ((0, 1, 2), (0, 2, 3)):
            raster_triangle(image, depth, [screen[i] for i in tri], [uvs[i] for i in tri], pixels, width, height, scale_u, scale_v, brightness)
    return img_w, img_h, image


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def raster_triangle(image, depth, pts, uvs, pixels, width, height, scale_u, scale_v, brightness):
    (x0, y0, z0), (x1, y1, z1), (x2, y2, z2) = pts
    area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
    if abs(area) < 1e-9:
        return
    min_x, max_x = max(int(min(x0, x1, x2)), 0), min(int(max(x0, x1, x2)) + 1, len(image[0]) - 1)
    min_y, max_y = max(int(min(y0, y1, y2)), 0), min(int(max(y0, y1, y2)) + 1, len(image) - 1)
    for py in range(min_y, max_y + 1):
        cy = py + 0.5
        for px in range(min_x, max_x + 1):
            cx = px + 0.5
            w0 = ((x1 - cx) * (y2 - cy) - (x2 - cx) * (y1 - cy)) / area
            w1 = ((x2 - cx) * (y0 - cy) - (x0 - cx) * (y2 - cy)) / area
            w2 = 1 - w0 - w1
            if w0 < -1e-6 or w1 < -1e-6 or w2 < -1e-6:
                continue
            z = w0 * z0 + w1 * z1 + w2 * z2
            if z <= depth[py][px]:
                continue
            u = (w0 * uvs[0][0] + w1 * uvs[1][0] + w2 * uvs[2][0]) * scale_u
            v = (w0 * uvs[0][1] + w1 * uvs[1][1] + w2 * uvs[2][1]) * scale_v
            tx, ty = min(max(int(u), 0), width - 1), min(max(int(v), 0), height - 1)
            r, g, b, a = pixels[ty][tx]
            if a < 128:
                continue
            depth[py][px] = z
            image[py][px] = (int(r * brightness), int(g * brightness), int(b * brightness), 255)


def paste(target, source, x_off, y_off):
    for y, row in enumerate(source):
        for x, px in enumerate(row):
            target[y + y_off][x + x_off] = px


def cmd_render(args):
    tex_w, tex_h, cubes = cubes_of(args.geo)
    texture = read_png(args.png)
    yaw, pitch = VIEWS.get(args.view, (args.yaw, args.pitch))
    w, h, image = render_view(cubes, texture, tex_w, tex_h, yaw + args.spin, pitch, args.scale)
    write_png(args.out, w, h, image)
    print(f"wrote {args.out} {w}x{h} yaw={yaw} pitch={pitch}")


def cmd_turntable(args):
    tex_w, tex_h, cubes = cubes_of(args.geo)
    texture = read_png(args.png)
    panels = [render_view(cubes, texture, tex_w, tex_h, VIEWS[v][0] + args.spin, VIEWS[v][1], args.scale) for v in args.views.split(",")]
    gap = 4
    total_w = sum(p[0] for p in panels) + gap * (len(panels) - 1)
    total_h = max(p[1] for p in panels)
    image = blank(total_w, total_h, (30, 32, 36, 255))
    x = 0
    for w, h, panel in panels:
        paste(image, panel, x, (total_h - h) // 2)
        x += w + gap
    write_png(args.out, total_w, total_h, image)
    print(f"wrote {args.out} {total_w}x{total_h} views={args.views}")


def pixel_rect(patch, scale_u, scale_v):
    u, v, w, h = patch
    x0, y0 = int(round(u * scale_u)), int(round(v * scale_v))
    x1, y1 = int(round((u + w) * scale_u)), int(round((v + h) * scale_v))
    return min(x0, x1), min(y0, y1), max(x0, x1), max(y0, y1)


def exact_rect(patch, scale_u, scale_v):
    u, v, w, h = patch
    x0, x1 = sorted((u * scale_u, (u + w) * scale_u))
    y0, y1 = sorted((v * scale_v, (v + h) * scale_v))
    return x0, y0, x1, y1


def inside(rect, x, y):
    return rect[0] <= x < rect[2] and rect[1] <= y < rect[3]


def cmd_faces(args):
    tex_w, tex_h, cubes = cubes_of(args.geo, args.profile)
    width, height, _ = read_png(args.png)
    scale_u, scale_v = width / tex_w, height / tex_h
    print(f"texture {width}x{height} png, geo uv grid {tex_w}x{tex_h}, {scale_u:g} px per uv unit")
    by_slot = defaultdict(list)
    for cube in cubes:
        by_slot[cube["slot"] or "(unslotted)"].append(cube)
    for slot in sorted(by_slot, key=lambda s: (s == "(unslotted)", s)):
        print(f"\n[{slot}]")
        for cube in by_slot[slot]:
            flags = " mirror" if cube["mirror"] else ""
            flags += " rotated" if cube["rotated"] else ""
            print(f"  {cube['bone']}[{cube['index']}] size={fmt(cube['size'])} visual={fmt(cube['visual'])} inflate={cube['inflate']:g}{flags}")
            for face, _, patch in cube["faces"]:
                x0, y0, x1, y1 = pixel_rect(patch, scale_u, scale_v)
                print(f"      {face:5} px x{x0}-{x1} y{y0}-{y1} ({x1 - x0}x{y1 - y0})")


def fmt(values):
    return "x".join(f"{v:g}" for v in values)


def cmd_uvmap(args):
    tex_w, tex_h, cubes = cubes_of(args.geo, args.profile)
    width, height, pixels = read_png(args.png)
    scale_u, scale_v = width / tex_w, height / tex_h
    zoom = args.zoom
    image = blank(width * zoom, height * zoom, (40, 40, 44, 255))
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[y][x]
            if a == 0:
                continue
            for dy in range(zoom):
                for dx in range(zoom):
                    image[y * zoom + dy][x * zoom + dx] = (r, g, b, 255)
    legend = {}
    for cube in cubes:
        colour = SLOT_COLOURS.get(cube["slot"], UNSLOTTED_COLOUR)
        legend[cube["slot"] or "(unslotted)"] = colour
        for _, _, patch in cube["faces"]:
            x0, y0, x1, y1 = pixel_rect(patch, scale_u, scale_v)
            outline(image, x0 * zoom, y0 * zoom, x1 * zoom - 1, y1 * zoom - 1, colour + (255,))
    write_png(args.out, width * zoom, height * zoom, image)
    print(f"wrote {args.out}")
    for slot, colour in sorted(legend.items()):
        print(f"  {slot:18} #{colour[0]:02X}{colour[1]:02X}{colour[2]:02X}")


def outline(image, x0, y0, x1, y1, colour):
    h, w = len(image), len(image[0])
    for x in range(max(x0, 0), min(x1, w - 1) + 1):
        if 0 <= y0 < h:
            image[y0][x] = colour
        if 0 <= y1 < h:
            image[y1][x] = colour
    for y in range(max(y0, 0), min(y1, h - 1) + 1):
        if 0 <= x0 < w:
            image[y][x0] = colour
        if 0 <= x1 < w:
            image[y][x1] = colour


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
    return Counter((r, g, b) for row in pixels for r, g, b, a in row if a >= 128)


def cmd_palette(args):
    _, _, pixels = read_png(args.png)
    colours = opaque_colours(pixels)
    ramps = defaultdict(list)
    for rgb, count in colours.items():
        ramps[hue_bucket(rgb)].append((rgb, count))
    print(f"{len(colours)} colours over {sum(colours.values())} opaque texels")
    for bucket in sorted(ramps, key=lambda b: -sum(c for _, c in ramps[b])):
        entries = sorted(ramps[bucket], key=lambda e: luminance(e[0]))
        print(f"\n{bucket}: {len(entries)} tones")
        for (r, g, b), count in entries:
            print(f"  #{r:02X}{g:02X}{b:02X} lum={luminance((r, g, b)):5.1f} x{count}")


def read_palette(path):
    if path.lower().endswith(".png"):
        return set(opaque_colours(read_png(path)[2]))
    with open(path) as handle:
        return {parse_hex(tok) for line in handle for tok in line.split() if tok.startswith("#")}


def parse_hex(token):
    token = token.lstrip("#")
    return tuple(int(token[i:i + 2], 16) for i in (0, 2, 4))


def cmd_check(args):
    tex_w, tex_h, cubes = cubes_of(args.geo, args.profile)
    width, height, pixels = read_png(args.png)
    scale_u, scale_v = width / tex_w, height / tex_h
    covered = [[False] * width for _ in range(height)]
    problems = 0
    holes, unpainted = [], []
    for cube in cubes:
        for face, _, patch in cube["faces"]:
            x0, y0, x1, y1 = pixel_rect(patch, scale_u, scale_v)
            exact = exact_rect(patch, scale_u, scale_v)
            total = clear = 0
            for y in range(max(y0, 0), min(y1, height)):
                for x in range(max(x0, 0), min(x1, width)):
                    covered[y][x] = True
                    if not inside(exact, x + 0.5, y + 0.5):
                        continue
                    total += 1
                    clear += pixels[y][x][3] < 128
            label = f"{cube['bone']}[{cube['index']}].{face} px x{x0}-{x1} y{y0}-{y1}"
            if total and clear == total:
                unpainted.append(label)
            elif clear:
                holes.append(f"{label}: {clear}/{total} clear")
    stray = sum(1 for y in range(height) for x in range(width) if pixels[y][x][3] >= 128 and not covered[y][x])
    densities = sorted(((c["size"][0] / c["visual"][0] * scale_u, i, c) for i, c in enumerate(cubes) if c["visual"][0] > 0), key=lambda e: e[0])
    print(f"{args.png}: {len(cubes)} cubes, {len(holes)} faces with holes, {len(unpainted)} unpainted faces, {stray} stray texels")
    for line in holes:
        print(f"  HOLE {line}")
    if args.verbose:
        for line in unpainted:
            print(f"  unpainted {line}")
    if densities:
        median = densities[len(densities) // 2][0]
        print(f"  texel density median {median:.2f} px per visible unit, range {densities[0][0]:.2f} to {densities[-1][0]:.2f}")
        for density, _, cube in densities:
            if density < median * 0.5 or density > median * 2:
                print(f"  DENSITY {cube['bone']}[{cube['index']}] {density:.2f} px per unit")
    if args.strict:
        problems += len(holes) + (stray > 0)
    if args.palette:
        allowed = read_palette(args.palette)
        drift = {rgb: n for rgb, n in opaque_colours(pixels).items() if rgb not in allowed}
        if drift:
            problems += 1
            print(f"  DRIFT {len(drift)} colours outside the palette:")
            for (r, g, b), n in sorted(drift.items(), key=lambda e: -e[1])[:20]:
                print(f"    #{r:02X}{g:02X}{b:02X} x{n}")
    if args.fail_unpainted and unpainted:
        problems += 1
        for line in unpainted:
            print(f"  UNPAINTED {line}")
    print("OK" if not problems else f"FAIL ({problems} problem groups)")
    return 0 if not problems else 1


def cmd_diff(args):
    wa, ha, a = read_png(args.a)
    wb, hb, b = read_png(args.b)
    if (wa, ha) != (wb, hb):
        print(f"size differs: {wa}x{ha} vs {wb}x{hb}")
        return 1
    changed = 0
    out = blank(wa, ha)
    for y in range(ha):
        for x in range(wa):
            if a[y][x] != b[y][x]:
                changed += 1
                out[y][x] = (255, 0, 255, 255)
            else:
                r, g, bl, al = a[y][x]
                out[y][x] = (r // 3, g // 3, bl // 3, 255) if al >= 128 else BACKGROUND
    if args.out:
        write_png(args.out, wa, ha, out)
    print(f"{changed} of {wa * ha} pixels differ")
    return 0 if changed == 0 or not args.fail_on_change else 1


def parse_block(path):
    palette, rows = {".": None}, []
    with open(path) as handle:
        for raw in handle:
            line = raw.rstrip("\n")
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            if "=" in line and len(line.split("=")[0].strip()) == 1:
                key, value = [part.strip() for part in line.split("=", 1)]
                palette[key] = None if value.lower() in ("transparent", "none", "") else parse_hex(value)
            else:
                rows.append(line)
    return palette, rows


def cmd_patch(args):
    width, height, pixels = read_png(args.base)
    palette, rows = parse_block(args.block)
    u, v = args.at
    written = 0
    for dy, row in enumerate(rows):
        for dx, char in enumerate(row):
            if char == " ":
                continue
            if char not in palette:
                raise SystemExit(f"{args.block}: no palette entry for {char!r}")
            x, y = u + dx, v + dy
            if not (0 <= x < width and 0 <= y < height):
                raise SystemExit(f"block runs off the texture at {x},{y}")
            colour = palette[char]
            pixels[y][x] = (0, 0, 0, 0) if colour is None else colour + (255,)
            written += 1
    write_png(args.out, width, height, pixels)
    print(f"wrote {args.out}: {written} texels at {u},{v} over {len(rows)} rows")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    p = sub.add_parser("render")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--view", choices=sorted(VIEWS))
    p.add_argument("--yaw", type=float, default=40)
    p.add_argument("--pitch", type=float, default=18)
    p.add_argument("--scale", type=float, default=8)
    p.add_argument("--spin", type=float, default=0, help="extra yaw, 180 for a rig that faces +z")
    p.set_defaults(func=cmd_render)

    p = sub.add_parser("turntable")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--views", default="left,front34,front,top")
    p.add_argument("--scale", type=float, default=8)
    p.add_argument("--spin", type=float, default=0, help="extra yaw, 180 for a rig that faces +z")
    p.set_defaults(func=cmd_turntable)

    p = sub.add_parser("faces")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("--profile")
    p.set_defaults(func=cmd_faces)

    p = sub.add_parser("uvmap")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--profile")
    p.add_argument("--zoom", type=int, default=4)
    p.set_defaults(func=cmd_uvmap)

    p = sub.add_parser("palette")
    p.add_argument("png")
    p.set_defaults(func=cmd_palette)

    p = sub.add_parser("check")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("--profile")
    p.add_argument("--palette")
    p.add_argument("--fail-unpainted", action="store_true")
    p.add_argument("--strict", action="store_true", help="also fail on holes and stray texels")
    p.add_argument("--verbose", action="store_true")
    p.set_defaults(func=cmd_check)

    p = sub.add_parser("diff")
    p.add_argument("a")
    p.add_argument("b")
    p.add_argument("out", nargs="?")
    p.add_argument("--fail-on-change", action="store_true")
    p.set_defaults(func=cmd_diff)

    p = sub.add_parser("patch")
    p.add_argument("base")
    p.add_argument("block")
    p.add_argument("out")
    p.add_argument("--at", type=int, nargs=2, required=True)
    p.set_defaults(func=cmd_patch)

    args = parser.parse_args(argv)
    return args.func(args) or 0


if __name__ == "__main__":
    sys.exit(main())
