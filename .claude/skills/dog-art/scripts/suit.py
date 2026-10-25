import math
import os

import bake
import pngio

SLOTS = ("torso", "mouth", "head", "eyes", "neck", "tail", "ear_left", "ear_right",
         "leg_front_left", "leg_front_right", "leg_back_left", "leg_back_right")
FACES = ("down", "up", "north", "south", "west", "east")
SLOT_SIZE = 48
SLOT_COLUMNS = 4
WIDTH = SLOT_COLUMNS * SLOT_SIZE
HEIGHT = -(-len(SLOTS) // SLOT_COLUMNS) * SLOT_SIZE
FACE_COLUMNS = 3
FACE_WIDTH = SLOT_SIZE // FACE_COLUMNS
FACE_HEIGHT = SLOT_SIZE // 2
QUAD_ORDER = ("west", "east", "north", "south", "up", "down")
UNIT = {"down": (0, -1, 0), "up": (0, 1, 0), "north": (0, 0, -1), "south": (0, 0, 1),
        "west": (-1, 0, 0), "east": (1, 0, 0)}
COUNTERCLOCKWISE = {"north": "west", "west": "south", "south": "east", "east": "north"}
TEXEL_SNAP = 1.0e-3
TRANSPARENT = (0, 0, 0, 0)


def cell_of(slot, face):
    if slot not in SLOTS:
        return None
    square = SLOTS.index(slot)
    index = FACES.index(face)
    return ((square % SLOT_COLUMNS) * SLOT_SIZE + (index % FACE_COLUMNS) * FACE_WIDTH,
            (square // SLOT_COLUMNS) * SLOT_SIZE + (index // FACE_COLUMNS) * FACE_HEIGHT,
            FACE_WIDTH, FACE_HEIGHT)


def turned_by(yaw_degrees):
    quarter_turns = math.floor(yaw_degrees / 90.0 + 0.5) % 4

    def turn(face):
        for _ in range(quarter_turns if face in COUNTERCLOCKWISE else 0):
            face = COUNTERCLOCKWISE[face]
        return face
    return turn


def facing(normal):
    best, best_dot = "north", 1.4e-45
    for face in FACES:
        unit = UNIT[face]
        dot = normal[0] * unit[0] + normal[1] * unit[1] + normal[2] * unit[2]
        if dot > best_dot:
            best, best_dot = face, dot
    return best


def face_direction(cube, face):
    normal = UNIT[face]
    if cube.get("rotation"):
        normal = bake.apply(bake.rotation_matrix(cube["rotation"]), (0, 0, 0), normal)
    return facing(normal)


def snapped(texels):
    whole = math.floor(texels + 0.5)
    return whole if abs(texels - whole) < TEXEL_SNAP else texels


def covered(start, size, atlas):
    low, high = sorted((start, start + size))
    return max(0, math.floor(snapped(low))), min(atlas - 1, math.ceil(snapped(high)) - 1)


def region_of(cube, face, atlas_w, atlas_h):
    u, v, w, h = bake.face_patches(cube, cube.get("mirror") is True)[face]
    x0, x1 = covered(u, w, atlas_w)
    y0, y1 = covered(v, h, atlas_h)
    if x1 < x0 or y1 < y0:
        return None
    return (face_direction(cube, face), x0, y0, x1 - x0 + 1, y1 - y0 + 1)


def suit_bones(profile, slot):
    moved = profile.get("suit_slots", {})
    kept = [name for name in profile.get(slot, []) if moved.get(name, slot) == slot]
    return kept + [name for name, target in moved.items() if target == slot and name not in kept]


def regions_of(bones, atlas_w, atlas_h, profile, slot):
    regions = []
    for name in suit_bones(profile, slot):
        for cube in bones.get(name, {}).get("cubes", []):
            for face in QUAD_ORDER:
                region = region_of(cube, face, atlas_w, atlas_h)
                if region:
                    regions.append(region)
    return regions


def regions_by_slot(geo_path, profile):
    description, _, bones = bake.load_geo(geo_path)
    atlas_w, atlas_h = description.get("texture_width", 64), description.get("texture_height", 64)
    return atlas_w, atlas_h, {slot: regions_of(bones, atlas_w, atlas_h, profile, slot) for slot in SLOTS}


def project(sheet, atlas_w, atlas_h, turn, regions, overrides=None):
    overrides = overrides or {}
    _, _, sheet_pixels = sheet
    atlas = [[TRANSPARENT] * atlas_w for _ in range(atlas_h)]
    for slot in SLOTS:
        override = overrides.get(slot)
        for face, rx, ry, rw, rh in regions.get(slot, ()):
            cell = cell_of(slot, turn(face))
            for row in range(rh):
                for column in range(rw):
                    x, y = rx + column, ry + row
                    if x >= atlas_w or y >= atlas_h:
                        continue
                    if override is not None:
                        atlas[y][x] = override[y][x]
                    else:
                        cx, cy, cw, ch = cell
                        atlas[y][x] = sheet_pixels[cy + row * ch // rh][cx + column * cw // rw]
    return atlas


def resized(atlas, width, height):
    atlas_h, atlas_w = len(atlas), len(atlas[0])
    return [[atlas[y * atlas_h // height][x * atlas_w // width] for x in range(width)] for y in range(height)]


def over(top, bottom):
    alpha = top[3] / 255
    if alpha == 0:
        return bottom
    out_alpha = alpha + bottom[3] / 255 * (1 - alpha)
    channels = tuple(round((top[i] * alpha + bottom[i] * bottom[3] / 255 * (1 - alpha)) / out_alpha) for i in range(3))
    return channels + (round(out_alpha * 255),)


def composite(top, bottom):
    return [[over(t, b) for t, b in zip(top_row, bottom_row)] for top_row, bottom_row in zip(top, bottom)]


def identifier_of(path):
    parts = os.path.normpath(os.path.abspath(path)).split(os.sep)
    if "assets" not in parts:
        return None
    index = len(parts) - 1 - parts[::-1].index("assets")
    return f"{parts[index + 1]}:{'/'.join(parts[index + 2:])}" if index + 2 < len(parts) else None


def path_of(identifier, near):
    namespace, path = identifier.split(":", 1)
    parts = os.path.normpath(os.path.abspath(near)).split(os.sep)
    root = os.sep.join(parts[:len(parts) - 1 - parts[::-1].index("assets") + 1])
    return os.path.join(root, namespace, *path.split("/"))


def overrides_for(profile, sheet_path, atlas_w, atlas_h):
    identifier = identifier_of(sheet_path)
    found, skipped = {}, []
    for slot, texture in profile.get("sheet_overrides", {}).get(identifier, {}).items() if identifier else ():
        path = path_of(texture, sheet_path)
        if os.path.exists(path):
            width, height, pixels = pngio.read_png(path)
            if (width, height) == (atlas_w, atlas_h):
                found[slot] = pixels
                continue
        skipped.append(texture)
    return found, skipped


def suit_atlas(geo_path, profile, sheet_path, yaw_degrees=0):
    sheet = pngio.read_png(sheet_path)
    if sheet[:2] != (WIDTH, HEIGHT):
        raise SystemExit(f"{sheet_path} is {sheet[0]}x{sheet[1]} but a suit sheet must be {WIDTH}x{HEIGHT}")
    atlas_w, atlas_h, regions = regions_by_slot(geo_path, profile)
    overrides, skipped = overrides_for(profile, sheet_path, atlas_w, atlas_h)
    return project(sheet, atlas_w, atlas_h, turned_by(yaw_degrees), regions, overrides), skipped
