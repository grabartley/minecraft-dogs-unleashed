import math


def is_keyed(channel):
    if not isinstance(channel, dict) or not channel:
        return False
    try:
        [float(time) for time in channel]
    except ValueError:
        return False
    return True


def clip_length(clip):
    length = clip.get("animation_length")
    if length:
        return float(length)
    times = [float(time) for track in clip.get("bones", {}).values() for channel in track.values() if is_keyed(channel) for time in channel]
    return max(times, default=0.0)


def tile(channel, period, length):
    if not is_keyed(channel) or period <= 0:
        return channel
    keys = sorted((float(time), value) for time, value in channel.items())
    tiled = {}
    for cycle in range(math.ceil(length / period) + 1):
        for time, value in keys:
            at = time + cycle * period
            if at <= length + 1e-6:
                tiled.setdefault(round(at, 4), value)
    return {f"{time:g}": value for time, value in sorted(tiled.items())}


def inherited(clip, base):
    own = {(bone, name) for bone, track in clip.get("bones", {}).items() for name in track}
    return sorted((bone, name) for bone, track in base.get("bones", {}).items() for name in track if (bone, name) not in own)


def layered(clip, base):
    length = clip_length(clip)
    period = clip_length(base)
    bones = {bone: dict(track) for bone, track in clip.get("bones", {}).items()}
    for bone, name in inherited(clip, base):
        bones.setdefault(bone, {})[name] = tile(base["bones"][bone][name], period, length)
    composite = {key: value for key, value in clip.items() if key != "bones"}
    composite["bones"] = bones
    return composite


def composites(animations, clips, bases):
    out = {}
    for name in clips:
        for base in bases:
            out[f"{name}@{base}"] = layered(animations[name], animations[base])
    return out
