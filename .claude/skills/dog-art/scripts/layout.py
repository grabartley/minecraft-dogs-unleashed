import json
import re

NUMBERS = re.compile(r"\[\s*([-0-9.eE+\s,\"]+?)\s*\]")


def inline_numbers(text):
    return NUMBERS.sub(lambda m: "[" + re.sub(r"\s*,\s*", ", ", m.group(1).strip()) + "]", text)


def animation_text(data, newline="\n"):
    return inline_numbers(json.dumps(data, indent="\t", ensure_ascii=False)) + newline


def geo_text(data, newline=""):
    text = inline_numbers(json.dumps(data, indent="\t"))
    for geometry in data.get("minecraft:geometry", []):
        for bone in geometry.get("bones", []):
            for cube in bone.get("cubes", []):
                expanded = inline_numbers(json.dumps(cube, indent="\t")).split("\n")
                match = re.search(r"\n(\t+)" + re.escape(expanded[1].strip()), text)
                depth = len(match.group(1)) - 1
                block = "\n".join(line if i == 0 else "\t" * depth + line for i, line in enumerate(expanded))
                text = text.replace(block, json.dumps(cube, separators=(", ", ": ")), 1)
    return text + newline


def expanded_text(data, newline="\n"):
    return json.dumps(data, indent="\t", ensure_ascii=False) + newline


def text_like(path, data, writer, default_newline="\n"):
    try:
        with open(path) as handle:
            raw = handle.read()
    except FileNotFoundError:
        raw = ""
    if not raw.strip():
        return writer(data, default_newline)
    newline = "\n" if raw.endswith("\n") else ""
    shipped = json.loads(raw)
    for style in (writer, expanded_text):
        if style(shipped, newline) == raw:
            return style(data, newline)
    return writer(data, newline)


def splice_tracks(shipped, exported, bones, dropped=()):
    for name, clip in shipped["animations"].items():
        tracks = clip.setdefault("bones", {})
        for bone in [b for b in tracks if b in bones or b in dropped]:
            del tracks[bone]
        for bone in bones:
            track = exported.get("animations", {}).get(name, {}).get("bones", {}).get(bone)
            if track:
                tracks[bone] = track
    return shipped
