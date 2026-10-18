import json

LOOP_MODES = {"loop": True, "hold": "hold_on_last_frame"}
CHANNELS = ("rotation", "position", "scale")
FLIPPED_IN_V5 = {"rotation": (True, True, False), "position": (True, False, False), "scale": (False, False, False)}


def load_project(path):
    with open(path) as handle:
        return json.load(handle)


def is_v5(project):
    version = str(project.get("meta", {}).get("format_version", "4"))
    return int(version.split(".")[0]) >= 5


def keyframe_value(keyframe):
    point = keyframe["data_points"][0]
    return [float(point[axis]) for axis in ("x", "y", "z")]


def to_geckolib(value, channel, v5):
    if not v5:
        return value
    return [-v if flip else v for v, flip in zip(value, FLIPPED_IN_V5[channel])]


def catmull_rom(p0, p1, p2, p3, t):
    t2, t3 = t * t, t * t * t
    return 0.5 * (2 * p1 + (p2 - p0) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (3 * p1 - p0 - 3 * p2 + p3) * t3)


def sample(keys, time):
    if time <= keys[0][0]:
        return list(keys[0][1])
    if time >= keys[-1][0]:
        return list(keys[-1][1])
    for index in range(len(keys) - 1):
        t1, v1, i1 = keys[index]
        t2, v2, i2 = keys[index + 1]
        if t1 <= time <= t2:
            alpha = (time - t1) / (t2 - t1) if t2 > t1 else 0.0
            if "catmullrom" not in (i1, i2):
                return [a + (b - a) * alpha for a, b in zip(v1, v2)]
            v0 = keys[index - 1][1] if index > 0 else v1
            v3 = keys[index + 2][1] if index + 2 < len(keys) else v2
            return [catmull_rom(a, b, c, d, alpha) for a, b, c, d in zip(v0, v1, v2, v3)]
    return list(keys[-1][1])


def channel_frames(keys, step, tolerance=0.2):
    if all(interpolation != "catmullrom" for _, _, interpolation in keys):
        return [(time, value) for time, value, _ in keys]
    start, end = keys[0][0], keys[-1][0]
    count = max(1, round((end - start) / step))
    times = sorted({round(start + (end - start) * i / count, 4) for i in range(count + 1)} | {round(t, 4) for t, _, _ in keys})
    return simplify([(time, sample(keys, time)) for time in times], tolerance)


def simplify(frames, tolerance):
    if len(frames) < 3:
        return frames
    first, last = frames[0], frames[-1]
    span = last[0] - first[0]
    worst, worst_index = 0.0, 0
    for index in range(1, len(frames) - 1):
        time, value = frames[index]
        alpha = (time - first[0]) / span if span else 0.0
        error = max(abs(v - (a + (b - a) * alpha)) for v, a, b in zip(value, first[1], last[1]))
        if error > worst:
            worst, worst_index = error, index
    if worst <= tolerance:
        return [first, last]
    return simplify(frames[:worst_index + 1], tolerance)[:-1] + simplify(frames[worst_index:], tolerance)


def export_animations(project, step=0.05, tolerance=0.2):
    v5 = is_v5(project)
    animations = {}
    for clip in project.get("animations", []):
        bones = {}
        for animator in clip.get("animators", {}).values():
            if animator.get("type", "bone") != "bone":
                continue
            channels = {}
            for channel in CHANNELS:
                keys = sorted(
                    (float(k["time"]), to_geckolib(keyframe_value(k), channel, v5), k.get("interpolation", "linear"))
                    for k in animator.get("keyframes", []) if k["channel"] == channel)
                if keys:
                    channels[channel] = {fmt(time): {"vector": [round(v, 4) for v in value]}
                                         for time, value in channel_frames(keys, step, tolerance)}
            if channels:
                bones[animator["name"]] = channels
        entry = {}
        if clip.get("loop") in LOOP_MODES:
            entry["loop"] = LOOP_MODES[clip["loop"]]
        entry["animation_length"] = float(clip["length"])
        entry["bones"] = bones
        animations[clip["name"]] = entry
    return {"format_version": "1.8.0", "animations": animations, "geckolib_format_version": 2}


def fmt(time):
    text = f"{time:.4f}".rstrip("0").rstrip(".")
    return text if "." in text else text + ".0"
