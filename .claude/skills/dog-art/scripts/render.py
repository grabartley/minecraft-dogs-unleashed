import math

from bake import face_uvs
from pngio import blank, opaque, paste

VIEWS = {"front": (0, 0), "left": (270, 0), "back": (180, 0), "right": (90, 0),
         "top": (0, 89), "front34": (320, 18), "back34": (140, 18)}


def view_angles(name, spin=0):
    if name not in VIEWS:
        raise SystemExit(f"unknown view {name!r}; choose from {', '.join(sorted(VIEWS))}")
    yaw, pitch = VIEWS[name]
    return yaw + spin, pitch


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


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def normalize(v):
    length = math.sqrt(dot(v, v)) or 1
    return (v[0] / length, v[1] / length, v[2] / length)


def face_normal(corners):
    return normalize(cross(sub(corners[3], corners[0]), sub(corners[1], corners[0])))


def shade(normal):
    nx, ny, nz = normal
    weight = abs(nx) + abs(ny) + abs(nz) or 1
    vertical = ny if ny > 0 else -ny * 0.5
    return (abs(nx) * 0.6 + abs(nz) * 0.8 + vertical) / weight


def render_view(cubes, texture, tex_w, tex_h, yaw, pitch, scale, margin=2, frame=None):
    width, height, pixels = texture
    scale_u, scale_v = width / tex_w, height / tex_h
    right, up, toward = camera(yaw, pitch)
    projected = []
    for cube in cubes:
        for face, corners, patch in cube["faces"]:
            pts = [(dot(c, right), dot(c, up), dot(c, toward)) for c in corners]
            projected.append((pts, face_uvs(face, patch, cube["mirror"]), shade(face_normal(corners))))
    framing = [corners for cube in (cubes if frame is None else frame) for _, corners, _ in cube["faces"]]
    xs = [dot(c, right) for corners in framing for c in corners]
    ys = [dot(c, up) for corners in framing for c in corners]
    min_x, max_x, min_y, max_y = min(xs), max(xs), min(ys), max(ys)
    img_w = int(math.ceil((max_x - min_x + 2 * margin) * scale))
    img_h = int(math.ceil((max_y - min_y + 2 * margin) * scale))
    image = blank(img_w, img_h)
    depth = [[-1e9] * img_w for _ in range(img_h)]
    sampler = (pixels, width, height, scale_u, scale_v)
    for pts, uvs, brightness in projected:
        screen = [((x - min_x + margin) * scale, (max_y - y + margin) * scale, z) for x, y, z in pts]
        for tri in ((0, 1, 2), (0, 2, 3)):
            raster_triangle(image, depth, [screen[i] for i in tri], [uvs[i] for i in tri], sampler, brightness)
    return img_w, img_h, image


def raster_triangle(image, depth, pts, uvs, sampler, brightness):
    pixels, width, height, scale_u, scale_v = sampler
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
            texel = pixels[min(max(int(v), 0), height - 1)][min(max(int(u), 0), width - 1)]
            if not opaque(texel):
                continue
            depth[py][px] = z
            image[py][px] = (int(texel[0] * brightness), int(texel[1] * brightness), int(texel[2] * brightness), 255)


def strip(panels, gap=4):
    total_w = sum(p[0] for p in panels) + gap * (len(panels) - 1)
    total_h = max(p[1] for p in panels)
    image = blank(total_w, total_h, (30, 32, 36, 255))
    x = 0
    for w, h, panel in panels:
        paste(image, panel, x, (total_h - h) // 2)
        x += w + gap
    return total_w, total_h, image
