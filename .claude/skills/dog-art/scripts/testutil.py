import json
import os
import tempfile

import pngio


def temp_path(suffix):
    handle = tempfile.NamedTemporaryFile(suffix=suffix, delete=False)
    handle.close()
    return handle.name


def write_geo(cubes, bone=None, texture=64):
    body = {"name": "b", "pivot": [0, 0, 0], "cubes": cubes}
    body.update(bone or {})
    data = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.t", "texture_width": texture, "texture_height": texture}, "bones": [body]}]}
    path = temp_path(".geo.json")
    with open(path, "w") as handle:
        json.dump(data, handle)
    return path


def write_sheet(width, height, fill=(200, 120, 60, 255), clear=()):
    rows = [[(0, 0, 0, 0) if (x, y) in clear else fill for x in range(width)] for y in range(height)]
    path = temp_path(".png")
    pngio.write_png(path, width, height, rows)
    return path


def remove(*paths):
    for path in paths:
        if path and os.path.exists(path):
            os.unlink(path)
