import base64
import json
import math
import os
import uuid

import layout
from bake import face_patches, load_geo, model_pivot
from pngio import read_png


def blockbench_rotation(rotation):
    rx, ry, rz = rotation or (0, 0, 0)
    return [-rx, -ry, rz]


def blockbench_faces(cube, mirror):
    faces = {}
    for face, (u, v, w, h) in face_patches(cube, mirror).items():
        u0, u1 = (u + w, u) if mirror else (u, u + w)
        v0, v1 = v, v + h
        if face == "up":
            u0, u1, v0, v1 = u1, u0, v1, v0
        elif face == "down":
            u0, u1 = u1, u0
        faces[face] = {"uv": [u0, v0, u1, v1], "texture": 0}
    return faces


def project_from_geo(geo_path, png_path, name=None):
    description, bone_list, _ = load_geo(geo_path)
    width, height, _ = read_png(png_path)
    with open(png_path, "rb") as handle:
        png_data = base64.b64encode(handle.read()).decode("ascii")
    elements, groups, nodes = [], [], {}
    for bone in bone_list:
        gid = str(uuid.uuid4())
        groups.append({"name": bone["name"], "uuid": gid, "origin": list(model_pivot(bone.get("pivot", (0, 0, 0)))),
                       "rotation": blockbench_rotation(bone.get("rotation")), "export": True, "visibility": True,
                       "mirror_uv": False, "reset": False, "color": 0, "autouv": 0, "shade": True, "locked": False})
        nodes[bone["name"]] = {"uuid": gid, "isOpen": False, "children": []}
        cubes = bone.get("cubes", [])
        for index, cube in enumerate(cubes):
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            mirror = cube.get("mirror") is True
            origin = model_pivot(cube.get("pivot", (0, 0, 0))) if cube.get("rotation") else (-(ox + sx / 2), oy + sy / 2, oz + sz / 2)
            element = {"name": f"{bone['name']}_{index}" if len(cubes) > 1 else bone["name"], "box_uv": True, "rescale": False,
                       "locked": False, "light_emission": 0, "render_order": "default", "allow_mirror_modeling": True,
                       "from": [-(ox + sx), oy, oz], "to": [-ox, oy + sy, oz + sz], "autouv": 0, "color": index % 8,
                       "origin": list(origin), "inflate": float(cube.get("inflate", bone.get("inflate", 0))),
                       "uv_offset": list(cube["uv"]), "faces": blockbench_faces(cube, mirror), "type": "cube", "uuid": str(uuid.uuid4())}
            if cube.get("rotation"):
                element["rotation"] = blockbench_rotation(cube["rotation"])
            if mirror:
                element["mirror_uv"] = True
            elements.append(element)
            nodes[bone["name"]]["children"].append(element["uuid"])
    outliner = []
    for bone in bone_list:
        parent = bone.get("parent")
        (nodes[parent]["children"] if parent in nodes else outliner).append(nodes[bone["name"]])
    texture_name = os.path.basename(png_path)
    tex_w, tex_h = description.get("texture_width", 64), description.get("texture_height", 64)
    return {
        "meta": {"format_version": "5.0", "model_format": "geckolib_model", "box_uv": True},
        "name": name or description.get("identifier", "geometry.rig").split(".")[-1],
        "model_identifier": name or "",
        "visible_box": [description.get("visible_bounds_width", 1), description.get("visible_bounds_height", 1),
                        (description.get("visible_bounds_offset") or [0, 0, 0])[1]],
        "resolution": {"width": tex_w, "height": tex_h},
        "elements": elements, "groups": groups, "outliner": outliner,
        "textures": [{"path": "", "name": texture_name, "folder": "", "namespace": "", "id": "0", "group": "", "width": width,
                      "height": height, "uv_width": tex_w, "uv_height": tex_h, "particle": False, "use_as_default": False,
                      "layers_enabled": False, "sync_to_project": "", "render_mode": "default", "render_sides": "auto",
                      "frame_time": 1, "frame_order_type": "loop", "frame_order": "", "frame_interpolate": False,
                      "visible": True, "internal": True, "saved": False, "uuid": str(uuid.uuid4()),
                      "source": f"data:image/png;base64,{png_data}"}],
        "variable_placeholders": "", "variable_placeholder_buttons": [], "timeline_setups": [], "unhandled_root_fields": {},
    }


def write_project(project, path):
    with open(path, "w") as handle:
        json.dump(project, handle, indent="\t")


def compare_geo(a_path, b_path, tolerance=1e-4):
    a_desc, a_bones, _ = load_geo(a_path)
    b_desc, b_bones, b_by_name = load_geo(b_path)
    mismatches = []
    for key in ("texture_width", "texture_height"):
        if a_desc.get(key, 64) != b_desc.get(key, 64):
            mismatches.append(f"{key} {a_desc.get(key)} vs {b_desc.get(key)}")
    for bone in a_bones:
        other = b_by_name.get(bone["name"])
        if other is None:
            mismatches.append(f"bone {bone['name']} missing")
            continue
        for key in ("pivot", "rotation"):
            if not close(bone.get(key, [0, 0, 0]), other.get(key, [0, 0, 0]), tolerance):
                mismatches.append(f"{bone['name']}.{key} {bone.get(key)} vs {other.get(key)}")
        if (bone.get("parent") or None) != (other.get("parent") or None):
            mismatches.append(f"{bone['name']}.parent {bone.get('parent')} vs {other.get('parent')}")
        mine, theirs = bone.get("cubes", []), other.get("cubes", [])
        if len(mine) != len(theirs):
            mismatches.append(f"{bone['name']} has {len(mine)} cubes vs {len(theirs)}")
        for index, (c1, c2) in enumerate(zip(mine, theirs)):
            for key, default in (("origin", [0, 0, 0]), ("size", [0, 0, 0]), ("pivot", [0, 0, 0]), ("rotation", [0, 0, 0]), ("uv", [0, 0])):
                if not close(c1.get(key, default), c2.get(key, default), tolerance):
                    mismatches.append(f"{bone['name']}[{index}].{key} {c1.get(key)} vs {c2.get(key)}")
            floors = ([math.floor(v) for v in c1.get("size", [0, 0, 0])], [math.floor(v) for v in c2.get("size", [0, 0, 0])])
            if floors[0] != floors[1]:
                mismatches.append(f"{bone['name']}[{index}].box_uv_size {floors[0]} vs {floors[1]}")
            inflate_a = float(c1.get("inflate", bone.get("inflate", 0)))
            inflate_b = float(c2.get("inflate", other.get("inflate", 0)))
            if abs(inflate_a - inflate_b) > tolerance:
                mismatches.append(f"{bone['name']}[{index}].inflate {inflate_a} vs {inflate_b}")
            if bool(c1.get("mirror")) != bool(c2.get("mirror")):
                mismatches.append(f"{bone['name']}[{index}].mirror {c1.get('mirror')} vs {c2.get('mirror')}")
    names = {bone["name"] for bone in a_bones}
    mismatches += [f"bone {name} only in {b_path}" for name in b_by_name if name not in names]
    return mismatches


def close(a, b, tolerance):
    a, b = list(a), list(b)
    return len(a) == len(b) and all(abs(float(x) - float(y)) <= tolerance for x, y in zip(a, b))


def tidy_numbers(node, places=5):
    if isinstance(node, float):
        rounded = round(node, places)
        return int(rounded) if rounded == int(rounded) and abs(rounded) < 1e15 else rounded
    if isinstance(node, list):
        return [tidy_numbers(item, places) for item in node]
    if isinstance(node, dict):
        return {key: tidy_numbers(value, places) for key, value in node.items()}
    return node


def tidy_geo(in_path, out_path, places=5, identifier=None):
    with open(in_path) as handle:
        geometry = tidy_numbers(json.load(handle), places)
    if identifier:
        for entry in geometry.get("minecraft:geometry", []):
            entry["description"]["identifier"] = identifier
    text = layout.text_like(out_path, geometry, layout.geo_text, default_newline="")
    with open(out_path, "w") as handle:
        handle.write(text)


def write_textures(project_path, out_dir, only=None):
    with open(project_path) as handle:
        textures = json.load(handle).get("textures", [])
    written = []
    for texture in textures:
        name = texture["name"] if texture["name"].endswith(".png") else texture["name"] + ".png"
        if only and name not in only and name[:-4] not in only:
            continue
        with open(os.path.join(out_dir, name), "wb") as handle:
            handle.write(base64.b64decode(texture["source"].split(",", 1)[1]))
        written.append(name)
    return written
