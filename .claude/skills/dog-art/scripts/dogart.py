#!/usr/bin/env python3
"""Headless renders, UV maps, texture checks and Blockbench export for GeckoLib dog rigs. Standard library only.

Commands:
  render     geo png out.png [--view front|front34|left|right|back|back34|top | --yaw D --pitch D] [--scale N] [--spin 180] [--asleep] [--fur-stage natural|shaggy|overgrown]
  turntable  geo png out.png [--views left,front34,front,top] [--scale N] [--spin 180] [--asleep] [--fur-stage ...]
  faces      geo png [--profile rig.json]
  uvmap      geo png out.png [--profile rig.json] [--zoom N]
  palette    png
  check      geo png [--profile rig.json] [--palette file|png] [--fail-unpainted] [--strict] [--verbose]
  diff       a.png b.png [out.png] [--fail-on-change]
  patch      base.png block.txt out.png --at U V
  tobbmodel  geo png out.bbmodel [--name N]
  geocompare a.geo.json b.geo.json [--tolerance T]
  animexport project.bbmodel out.animation.json [--step S] [--bones a,b [--drop c] [--clips d,e]]
  animimport project.bbmodel in.animation.json out.bbmodel [--replace]
  animlayer  in.animation.json out.animation.json --clips a,b [--over sit,idle,walk]
  tidygeo    in.geo.json out.geo.json [--places N] [--identifier ID]
  textures   project.bbmodel out_dir [--only name ...]

The rig profile beside the geo (rig_profiles/<name>.json) is read by default. Bones in its eyes slot are
the eyelids, hidden inside the head at rest. --asleep moves them to the pose the sleep clip holds them in and
keeps the open render's frame, so an open and an --asleep render diff pixel for pixel. Fur bones follow --fur-stage the way FurLook does in the client.
"""
import argparse
import json
import os
import sys
from collections import defaultdict

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import anim
import bake
import bbmodel
import checks
import layer
import layout
import pngio
import render

SLOT_COLOURS = {
    "head": (255, 99, 71), "mouth": (255, 165, 0), "eyes": (255, 255, 0),
    "neck": (154, 205, 50), "torso": (60, 179, 113), "tail": (0, 206, 209),
    "ear_left": (30, 144, 255), "ear_right": (138, 43, 226),
    "leg_front_left": (255, 105, 180), "leg_front_right": (205, 92, 92),
    "leg_back_left": (210, 180, 140), "leg_back_right": (176, 196, 222),
    "fur_shaggy": (240, 230, 140), "fur_overgrown": (189, 183, 107),
}
UNSLOTTED_COLOUR = (128, 128, 128)


def profile_for(args):
    path = getattr(args, "profile", None) or bake.profile_path_for(args.geo)
    return bake.load_profile(path)


def rig(args, hide=True):
    profile = profile_for(args)
    hidden = bake.hidden_bones(profile, getattr(args, "asleep", False), getattr(args, "fur_stage", "natural")) if hide else ()
    poses = {}
    if getattr(args, "asleep", False):
        poses = bake.closed_eyes(profile, bake.animation_path_for(args.geo))
        open_lids = sorted(set(profile.get("eyes", [])) - set(poses))
        if open_lids:
            print(f"no sleep pose found for {', '.join(open_lids)}, so they stay open", file=sys.stderr)
    description, cubes = bake.cubes_of(args.geo, profile, hidden, poses)
    return description.get("texture_width", 64), description.get("texture_height", 64), cubes


def cmd_render(args):
    tex_w, tex_h, cubes = rig(args)
    frame = rig(argparse.Namespace(**{**vars(args), "asleep": False}))[2] if args.asleep else None
    yaw, pitch = render.view_angles(args.view, args.spin) if args.view else (args.yaw + args.spin, args.pitch)
    w, h, image = render.render_view(cubes, pngio.read_png(args.png), tex_w, tex_h, yaw, pitch, args.scale, frame=frame)
    pngio.write_png(args.out, w, h, image)
    print(f"wrote {args.out} {w}x{h} yaw={yaw} pitch={pitch}")


def cmd_turntable(args):
    tex_w, tex_h, cubes = rig(args)
    texture = pngio.read_png(args.png)
    panels = [render.render_view(cubes, texture, tex_w, tex_h, *render.view_angles(v, args.spin), args.scale) for v in args.views.split(",")]
    w, h, image = render.strip(panels)
    pngio.write_png(args.out, w, h, image)
    print(f"wrote {args.out} {w}x{h} views={args.views}")


def pupil_faces(cubes, texel, scale_u, scale_v):
    x, y = texel
    return [f"{cube['bone']}[{cube['index']}].{face}" for cube in cubes for face, _, patch in cube["faces"]
            if bake.inside(bake.exact_rect(patch, scale_u, scale_v), x + 0.5, y + 0.5)]


def cmd_faces(args):
    tex_w, tex_h, cubes = rig(args, hide=False)
    width, height, _ = pngio.read_png(args.png)
    scale_u, scale_v = width / tex_w, height / tex_h
    print(f"texture {width}x{height} png, geo uv grid {tex_w}x{tex_h}, {scale_u:g} px per uv unit")
    for texel in bake.pupils(profile_for(args)):
        print(f"pupil px {texel[0]},{texel[1]} on {', '.join(pupil_faces(cubes, texel, scale_u, scale_v)) or 'no face'}")
    by_slot = defaultdict(list)
    for cube in cubes:
        by_slot[cube["slot"] or "(unslotted)"].append(cube)
    for slot in sorted(by_slot, key=lambda s: (s == "(unslotted)", s)):
        print(f"\n[{slot}]")
        for cube in by_slot[slot]:
            flags = (" mirror" if cube["mirror"] else "") + (" rotated" if cube["rotated"] else "")
            print(f"  {cube['bone']}[{cube['index']}] size={fmt(cube['size'])} visual={fmt(cube['visual'])} inflate={cube['inflate']:g}{flags}")
            for face, _, patch in cube["faces"]:
                x0, y0, x1, y1 = bake.pixel_rect(patch, scale_u, scale_v)
                print(f"      {face:5} px x{x0}-{x1} y{y0}-{y1} ({x1 - x0}x{y1 - y0})")


def fmt(values):
    return "x".join(f"{v:g}" for v in values)


def cmd_uvmap(args):
    tex_w, tex_h, cubes = rig(args, hide=False)
    width, height, pixels = pngio.read_png(args.png)
    scale_u, scale_v = width / tex_w, height / tex_h
    zoom = args.zoom
    image = pngio.blank(width * zoom, height * zoom, (40, 40, 44, 255))
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
            x0, y0, x1, y1 = bake.pixel_rect(patch, scale_u, scale_v)
            outline(image, x0 * zoom, y0 * zoom, x1 * zoom - 1, y1 * zoom - 1, colour + (255,))
    for x, y in bake.pupils(profile_for(args)):
        outline(image, x * zoom, y * zoom, (x + 1) * zoom - 1, (y + 1) * zoom - 1, (255, 0, 255, 255))
    pngio.write_png(args.out, width * zoom, height * zoom, image)
    print(f"wrote {args.out}, pupils outlined in #FF00FF")
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


def cmd_palette(args):
    _, _, pixels = pngio.read_png(args.png)
    ramps = checks.palette_ramps(pixels)
    total = sum(count for entries in ramps.values() for _, count in entries)
    print(f"{sum(len(e) for e in ramps.values())} colours over {total} opaque texels")
    for bucket in sorted(ramps, key=lambda b: -sum(c for _, c in ramps[b])):
        print(f"\n{bucket}: {len(ramps[bucket])} tones")
        for (r, g, b), count in ramps[bucket]:
            print(f"  #{r:02X}{g:02X}{b:02X} lum={checks.luminance((r, g, b)):5.1f} x{count}")


def cmd_check(args):
    tex_w, tex_h, cubes = rig(args, hide=False)
    palette = checks.read_palette(args.palette) if args.palette else None
    report = checks.check_sheet(cubes, tex_w, tex_h, pngio.read_png(args.png), palette)
    print(f"{args.png}: {len(cubes)} cubes, {len(report['holes'])} faces with holes, {len(report['unpainted'])} unpainted faces, "
          f"{report['stray']} stray texels, {len(report['off_sheet'])} faces off the sheet")
    for line in report["holes"]:
        print(f"  HOLE {line}")
    for line in report["off_sheet"]:
        print(f"  OFF-SHEET {line}")
    if args.verbose or args.fail_unpainted:
        for line in report["unpainted"]:
            print(f"  UNPAINTED {line}")
    if "median_density" in report:
        lo, hi = report["density_range"]
        print(f"  texel density median {report['median_density']:.2f} px per visible unit, range {lo:.2f} to {hi:.2f}")
        for density, label in report["density"]:
            print(f"  DENSITY {label} {density:.2f} px per unit")
    if report["drift"]:
        print(f"  DRIFT {len(report['drift'])} colours outside the palette:")
        for (r, g, b), n in sorted(report["drift"].items(), key=lambda e: -e[1])[:20]:
            print(f"    #{r:02X}{g:02X}{b:02X} x{n}")
    groups = checks.problem_groups(report, args.strict, args.fail_unpainted)
    print("OK" if not groups else f"FAIL ({groups} problem groups)")
    return 1 if groups else 0


def cmd_diff(args):
    changed, image = checks.diff_sheets(pngio.read_png(args.a), pngio.read_png(args.b))
    if args.out:
        pngio.write_png(args.out, len(image[0]), len(image), image)
    print(f"{changed} of {len(image) * len(image[0])} visible pixels differ")
    return 1 if changed and args.fail_on_change else 0


def cmd_patch(args):
    width, height, pixels = pngio.read_png(args.base)
    palette, rows = checks.parse_block(args.block)
    written = checks.apply_block(pixels, width, height, palette, rows, args.at[0], args.at[1])
    pngio.write_png(args.out, width, height, pixels)
    print(f"wrote {args.out}: {written} texels at {args.at[0]},{args.at[1]} over {len(rows)} rows")


def cmd_tobbmodel(args):
    project = bbmodel.project_from_geo(args.geo, args.png, args.name)
    bbmodel.write_project(project, args.out)
    print(f"wrote {args.out}: {len(project['groups'])} groups, {len(project['elements'])} cubes, texture {project['textures'][0]['name']}")


def cmd_geocompare(args):
    mismatches = bbmodel.compare_geo(args.a, args.b, args.tolerance)
    for line in mismatches:
        print(f"  {line}")
    print(f"{len(mismatches)} mismatches between {args.a} and {args.b}")
    return 1 if mismatches else 0


def cmd_tidygeo(args):
    bbmodel.tidy_geo(args.input, args.out, args.places, args.identifier)
    print(f"wrote {args.out}: numbers rounded to {args.places} places")


def cmd_animexport(args):
    exported = anim.export_animations(anim.load_project(args.project), args.step)
    if args.bones:
        bones = args.bones.split(",")
        with open(args.out) as handle:
            exported = layout.splice_tracks(json.load(handle), exported, bones, args.drop.split(",") if args.drop else (),
                                            args.clips.split(",") if args.clips else None)
        print(f"spliced {', '.join(bones)} into {args.out}")
    text = layout.text_like(args.out, exported, layout.animation_text)
    with open(args.out, "w") as handle:
        handle.write(text)
    print(f"wrote {args.out}: {', '.join(exported['animations'])}")


def cmd_textures(args):
    for name in bbmodel.write_textures(args.project, args.out_dir, args.only):
        print(f"wrote {os.path.join(args.out_dir, name)}")


def cmd_animimport(args):
    project = anim.load_project(args.project)
    with open(args.animations) as handle:
        try:
            skipped, eased = anim.import_animations(project, json.load(handle), args.replace)
        except ValueError as error:
            print(f"{error}; pass --replace to drop them", file=sys.stderr)
            return 1
    with open(args.out, "w") as handle:
        json.dump(project, handle)
    print(f"wrote {args.out}: {', '.join(clip['name'] for clip in project['animations'])}")
    if skipped:
        print(f"skipped bones the project lacks: {', '.join(skipped)}")
    if eased:
        print(f"catmullrom is a spline in Blockbench but an ease per segment in GeckoLib, so these play differently there: {', '.join(eased)}")


def cmd_animlayer(args):
    with open(args.animations) as handle:
        animations = json.load(handle)["animations"]
    clips = args.clips.split(",")
    bases = args.over.split(",")
    missing = [name for name in clips + bases if name not in animations]
    if missing:
        print(f"no such clips: {', '.join(missing)}", file=sys.stderr)
        return 1
    with open(args.out, "w") as handle:
        handle.write(layout.animation_text({"format_version": "1.8.0", "animations": layer.composites(animations, clips, bases)}))
    for name in clips:
        for base in bases:
            channels = layer.inherited(animations[name], animations[base])
            print(f"{name}@{base}: {', '.join(f'{bone}.{channel}' for bone, channel in channels) or 'nothing inherited'}")


def add_rig_options(parser):
    parser.add_argument("--profile", help="rig profile; defaults to rig_profiles/<name>.json beside the geo")
    parser.add_argument("--asleep", action="store_true", help="close the eyes with the pose the sleep clip holds them in")
    parser.add_argument("--fur-stage", choices=bake.FUR_STAGES, default="natural")
    parser.add_argument("--scale", type=float, default=8)
    parser.add_argument("--spin", type=float, default=0, help="extra yaw, 180 for a rig that faces +z")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    commands = parser.add_subparsers(dest="command", required=True)

    p = commands.add_parser("render")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--view", choices=sorted(render.VIEWS))
    p.add_argument("--yaw", type=float, default=320)
    p.add_argument("--pitch", type=float, default=18)
    add_rig_options(p)
    p.set_defaults(func=cmd_render)

    p = commands.add_parser("turntable")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--views", default="left,front34,front,top")
    add_rig_options(p)
    p.set_defaults(func=cmd_turntable)

    p = commands.add_parser("faces")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("--profile")
    p.set_defaults(func=cmd_faces)

    p = commands.add_parser("uvmap")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--profile")
    p.add_argument("--zoom", type=int, default=4)
    p.set_defaults(func=cmd_uvmap)

    p = commands.add_parser("palette")
    p.add_argument("png")
    p.set_defaults(func=cmd_palette)

    p = commands.add_parser("check")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("--profile")
    p.add_argument("--palette")
    p.add_argument("--fail-unpainted", action="store_true")
    p.add_argument("--strict", action="store_true", help="also fail on holes, stray texels and faces off the sheet")
    p.add_argument("--verbose", action="store_true")
    p.set_defaults(func=cmd_check)

    p = commands.add_parser("diff")
    p.add_argument("a")
    p.add_argument("b")
    p.add_argument("out", nargs="?")
    p.add_argument("--fail-on-change", action="store_true")
    p.set_defaults(func=cmd_diff)

    p = commands.add_parser("patch")
    p.add_argument("base")
    p.add_argument("block")
    p.add_argument("out")
    p.add_argument("--at", type=int, nargs=2, required=True)
    p.set_defaults(func=cmd_patch)

    p = commands.add_parser("tobbmodel")
    p.add_argument("geo")
    p.add_argument("png")
    p.add_argument("out")
    p.add_argument("--name")
    p.set_defaults(func=cmd_tobbmodel)

    p = commands.add_parser("geocompare")
    p.add_argument("a")
    p.add_argument("b")
    p.add_argument("--tolerance", type=float, default=1e-4)
    p.set_defaults(func=cmd_geocompare)

    p = commands.add_parser("tidygeo")
    p.add_argument("input")
    p.add_argument("out")
    p.add_argument("--places", type=int, default=5)
    p.add_argument("--identifier", help="geometry identifier to write, such as the shipped file's own")
    p.set_defaults(func=cmd_tidygeo)

    p = commands.add_parser("animexport")
    p.add_argument("project")
    p.add_argument("out")
    p.add_argument("--step", type=float, default=0.05, help="seconds between baked keys for catmullrom channels")
    p.add_argument("--bones", help="comma-separated bones whose tracks replace the ones in the existing out file; every other track stays")
    p.add_argument("--drop", help="comma-separated bones to remove from the existing out file, such as renamed ones")
    p.add_argument("--clips", help="comma-separated clips to splice into; every other clip stays as it is")
    p.set_defaults(func=cmd_animexport)

    p = commands.add_parser("animlayer", help="clips as the game draws them over a base clip, for review in Blockbench")
    p.add_argument("animations")
    p.add_argument("out")
    p.add_argument("--clips", required=True, help="comma-separated clips that play over the base")
    p.add_argument("--over", default="sit,idle,walk", help="comma-separated base clips the movement controller plays underneath")
    p.set_defaults(func=cmd_animlayer)

    p = commands.add_parser("textures")
    p.add_argument("project")
    p.add_argument("out_dir")
    p.add_argument("--only", nargs="*", help="texture names to write; every texture by default")
    p.set_defaults(func=cmd_textures)

    p = commands.add_parser("animimport")
    p.add_argument("project")
    p.add_argument("animations")
    p.add_argument("out")
    p.add_argument("--replace", action="store_true", help="drop clips the project already has")
    p.set_defaults(func=cmd_animimport)

    args = parser.parse_args(argv)
    return args.func(args) or 0


if __name__ == "__main__":
    sys.exit(main())
