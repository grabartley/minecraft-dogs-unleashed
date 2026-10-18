import json
import math
import os

import anim

FACES = ("west", "north", "east", "south", "up", "down")
FUR_STAGES = ("natural", "shaggy", "overgrown")


def load_geo(path):
    with open(path) as handle:
        geometry = json.load(handle)["minecraft:geometry"][0]
    description = geometry["description"]
    bones = {bone["name"]: bone for bone in geometry["bones"]}
    return description, geometry["bones"], bones


def profile_path_for(geo_path):
    name = os.path.basename(geo_path).split(".")[0]
    candidate = os.path.join(os.path.dirname(geo_path), "..", "rig_profiles", f"{name}.json")
    return os.path.normpath(candidate) if os.path.exists(candidate) else None


def load_profile(path):
    if not path:
        return {}
    with open(path) as handle:
        return json.load(handle)


def slots_by_bone(profile):
    return {bone: slot for slot, bones in profile.items() if isinstance(bones, list) for bone in bones if isinstance(bone, str)}


def pupils(profile):
    return [tuple(texel) for texel in profile.get("pupils", [])]


def hidden_bones(profile, asleep=False, fur_stage="natural"):
    hidden = set()
    if not asleep:
        hidden.update(profile.get("eyes", []))
    if fur_stage != "overgrown":
        hidden.update(profile.get("fur_overgrown", []))
    if fur_stage == "natural":
        hidden.update(profile.get("fur_shaggy", []))
    return hidden


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


def model_pivot(pivot):
    return (-pivot[0], pivot[1], pivot[2])


def bone_chain(bones, name):
    chain = []
    while name:
        bone = bones[name]
        chain.append(bone)
        name = bone.get("parent")
    return chain


def face_patches(cube, mirror):
    if isinstance(cube.get("uv"), dict):
        raise SystemExit("per-face UV cubes are not supported; box UV only")
    sx, sy, sz = (math.floor(v) for v in cube["size"])
    u, v = cube["uv"]
    first, third = ("west", "east") if mirror else ("east", "west")
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
    return [(x1, y0, z1), (x1, y0, z0), (x0, y0, z0), (x0, y0, z1)]


def face_uvs(face, patch, mirror):
    u, v, w, h = patch
    left, right = (u + w, u) if mirror else (u, u + w)
    if face == "down":
        return [(left, v), (left, v + h), (right, v + h), (right, v)]
    return [(left, v), (right, v), (right, v + h), (left, v + h)]


def animation_path_for(geo_path):
    name = os.path.basename(geo_path).split(".")[0]
    candidate = os.path.join(os.path.dirname(geo_path), "..", "animations", f"{name}.animation.json")
    return os.path.normpath(candidate) if os.path.exists(candidate) else None


def closed_eyes(profile, animation_path, clip="sleep"):
    if not animation_path:
        return {}
    with open(animation_path) as handle:
        bones = json.load(handle)["animations"].get(clip, {}).get("bones", {})
    poses = {}
    for bone in profile.get("eyes", []):
        channels = bones.get(bone, {})
        pose = {channel: anim.track_keys(channels[channel])[-1][1] for channel in ("position", "scale") if channel in channels}
        if pose:
            poses[bone] = pose
    return poses


def bone_transforms(bone, pose):
    pivot = model_pivot(bone.get("pivot", (0, 0, 0)))
    steps = []
    if "scale" in pose:
        sx, sy, sz = pose["scale"]
        steps.append(lambda p, c=pivot: (c[0] + (p[0] - c[0]) * sx, c[1] + (p[1] - c[1]) * sy, c[2] + (p[2] - c[2]) * sz))
    if bone.get("rotation"):
        steps.append(lambda p, m=rotation_matrix(bone["rotation"]), c=pivot: apply(m, c, p))
    if "position" in pose:
        ox, oy, oz = pose["position"]
        steps.append(lambda p: (p[0] - ox, p[1] + oy, p[2] + oz))
    return steps


def cubes_of(geo_path, profile=None, hidden=(), poses=None):
    poses = poses or {}
    description, bone_list, bones = load_geo(geo_path)
    slots = slots_by_bone(profile or {})
    result = []
    for bone in bone_list:
        if bone["name"] in hidden:
            continue
        chain = bone_chain(bones, bone["name"])
        for index, cube in enumerate(bone.get("cubes", [])):
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            inflate = float(cube.get("inflate", bone.get("inflate", 0)))
            lo = (-(ox + sx) - inflate, oy - inflate, oz - inflate)
            hi = (-ox + inflate, oy + sy + inflate, oz + sz + inflate)
            transforms = []
            if cube.get("rotation"):
                transforms.append(lambda p, m=rotation_matrix(cube["rotation"]), c=model_pivot(cube.get("pivot", (0, 0, 0))): apply(m, c, p))
            for b in chain:
                transforms.extend(bone_transforms(b, poses.get(b["name"], {})))
            mirror = cube.get("mirror") is True
            patches = face_patches(cube, mirror)
            faces = []
            for face in FACES:
                corners = face_corners(lo, hi, face)
                for transform in transforms:
                    corners = [transform(c) for c in corners]
                faces.append((face, corners, patches[face]))
            result.append({
                "bone": bone["name"], "index": index, "slot": slots.get(bone["name"]),
                "size": (sx, sy, sz), "visual": tuple(round(s + 2 * inflate, 3) for s in (sx, sy, sz)),
                "inflate": inflate, "mirror": mirror, "faces": faces, "rotated": bool(transforms),
            })
    return description, result


def texel_density(cube, scale):
    densities = [math.floor(size) / visual * scale for size, visual in zip(cube["size"], cube["visual"]) if visual > 0 and size >= 1]
    return min(densities) if densities else None


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
