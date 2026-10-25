---
name: dog-art
description: Author, review and gate dog rigs, coat textures, fur bones and overlays for Dogs Unleashed. Blockbench project files are the source, the repo's geo and textures are exports, and one stdlib Python script renders, maps, checks and diffs them without launching the game. Use for any change under geo, textures/entity or rig_profiles, for recolours and coat variants, and before any dog art goes to review.
---

# Dog Art

Dog art is the mod's storefront. The bar is premium: a new coat has to sit beside the shipped
ones without looking like it came from a different mod, and a rig change must leave every shipped
breed pixel-identical unless the issue says otherwise. This skill makes that measurable and fast.

```bash
D=.claude/skills/dog-art/scripts/dogart.py
A=src/client/resources/assets/dogs-unleashed
SCRATCH=<the session scratch directory>
```

Small props and flat item sprites are not this skill: use `geo-prop` and `item-sprite` from the
minecraft-modding plugin.

## Where The Art Lives

The artist works in Blockbench and exports each rig with the GeckoLib plugin. The repo holds the
exports: `geo/<breed>.geo.json`, `textures/entity/<breed>_<coat>.png`, `animations/`. The
Blockbench projects live outside the repo in `~/blockbench/<breed>/<breed>.bbmodel`, one folder per
rig, and that is where every rig change is made.

Anything three-dimensional or advanced is done in Blockbench: new cubes, moved pivots, fur bones,
tufts, planes, rotations, animation poses, and judging a shape by eye. The `bbmodel` MCP edits
those projects headless and exports geometry with Blockbench's own Bedrock codec, and its
`bbmodel_render` and `bbmodel_contact_sheet` draw with Blockbench's renderer, which is the
authority on how a rig looks. The script in this skill never writes geometry; it measures,
maps, checks and diffs the exports. The workflow is:

1. `bbmodel_info` on the project, then `bbmodel_edit` with a batch of operations. Coordinates are
   Blockbench's: x is mirrored against the geo file, rotations are degrees applied Z, Y, X.
2. `bbmodel_validate` for broken references, slivers and floating parts, `bbmodel_contact_sheet`
   or `bbmodel_render` to look at it with Blockbench's renderer, and `bbmodel_web_url` for a link
   that opens the project in the Blockbench web app for the artist.
3. `bbmodel_export_bedrock_geometry` into the rig's folder in `~/blockbench`, then
   `python3 $D tidygeo export.geo.json <repo geo path> --identifier geometry.unknown` (the shipped
   files' identifier). The MCP export writes float noise such as `7.999999999999999`, which
   GeckoLib floors to 7 when it lays out box UV, shifting every face of that cube. `tidygeo` rounds
   it away at 5 places, which keeps every number the shipped files hold, and writes the file in
   the layout it already has, so the diff shows only the bones that changed.
4. `python3 $D geocompare` the new geo against `main` to see every bone and cube that moved, then
   the gates below. It also reports any cube whose floored box UV size changed.
5. Animations live in the project too. The MCP has no animation export, so
   `python3 $D animexport project.bbmodel <repo animation path>` writes GeckoLib's JSON. Blockbench
   5.0 projects store rotation x, rotation y and position x keyframes with the opposite sign to the
   JSON, and the export flips them back. GeckoLib has no Catmull-Rom spline (its `catmullrom` is an
   ease per segment), so spline channels are baked into linear keys every `--step` seconds and
   simplified to stay within 0.2 of the curve. Judge poses with `bbmodel_render` on a clip, which
   draws the project convention correctly. The older breeds' projects started without clips;
   `python3 $D animimport project.bbmodel <repo animation path> out.bbmodel` loads the shipped
   clips into a copy of a project so they can be rendered and edited there. When only some bones'
   keys changed, `animexport ... --bones a,b [--drop old] [--clips c,d]` replaces just those tracks in the
   shipped file, only in the named clips when `--clips` is given (`--drop` then drops only inside
   those clips), and keeps the rest and its layout as they were.
   In game a clip rarely plays alone. The movement controller plays `sit`, `idle`, `walk`, `run`
   or `sleep` underneath, other controllers such as `tail_wag` sit between it and the tricks,
   and a later controller only replaces the bone channels its clip keys, so any channel a trick
   leaves unkeyed shows whatever plays below it. `head_tilt` runs after the tricks and overrides
   their head. Blockbench plays one clip at a time and hides this.
   `animlayer <animation file> out.json --clips beg,roll_over --over sit,idle,walk` writes each
   clip drawn over each base named in `--over` (`beg@sit` and so on) and lists the channels it
   inherits. The list is the real check; the composite starts each base at time 0, while in game
   the base keeps its own phase, so a composite render is one sample of what players see. Load it
   with `animimport` to render it (Molang channels in a base cannot be imported). A clip that must
   look the same over any base keys every channel the bases key.
6. Textures live in the project too. Every coat is a texture in the project, all on the same UV
   layout. The MCP only reads files under `~/blockbench`, so put edited sheets in the rig's
   `coats/` folder and add them with `bbmodel_add_texture`. Pass the model's top-level groups as
   `assign_to` for the coat the faces should show. There is no texture removal, so rename a sheet
   it replaces to say it is unused. `python3 $D textures project.bbmodel <repo textures folder>
   --only <coat> ...` writes the sheets back out.

If a breed has no project yet, make one from the shipped export: `python3 $D tobbmodel geo png
out.bbmodel`. Every shipped rig round-trips through that and the MCP export with zero mismatches.

Three rules hold for every rig change: the project is always copied into `~/blockbench/<rig>/`
before it is edited, the repo receives only the exported geo and the PNGs, and no `.bbmodel` is
ever committed. `.gitignore` refuses them.

## The Script

| Command | What it answers | Speed |
|---|---|---|
| `render geo png out --view left [--asleep] [--fur-stage shaggy]` | A quick textured look at an export from one angle, with no engine download | 0.2 s |
| `turntable geo png out` | Left, three-quarter, front and top in one strip, for before and after pairs | 0.5 s |
| `suit geo png sheet.png out.png [--profile rig.json] [--views ...] [--atlas atlas.png] [--spin 180]` | A costume suit sheet projected onto the rig the way the game does, over the coat, as a turntable strip | 1 s |
| `faces geo png --profile rig.json` | Which texture pixels are the snout top, the left ear, the belly | instant |
| `uvmap geo png out --profile rig.json` | The same, drawn over the sheet in a colour per rig slot | instant |
| `palette png` | Every colour, grouped by hue, sorted by tone, with counts | instant |
| `check geo png [--palette file] [--fail-unpainted] [--strict]` | Holes on mapped faces, paint no face reads, faces off the sheet, density outliers, palette drift | instant |
| `diff a.png b.png [out.png] [--fail-on-change]` | How many visible pixels changed, and where | instant |
| `patch base.png block.txt out.png --at U V` | Paint a palette-mapped text block onto a sheet | instant |
| `animimport project.bbmodel in.animation.json out.bbmodel` | A copy of the project carrying the shipped clips, sign-corrected for Blockbench 5 | instant |
| `tobbmodel geo png out.bbmodel` | A Blockbench project of a shipped rig with its texture embedded | instant |
| `geocompare a.geo.json b.geo.json` | Bone-by-bone, cube-by-cube numeric diff of two rigs, including floored box UV sizes | instant |
| `tidygeo in.geo.json out.geo.json [--identifier ID]` | Round an MCP export's float noise away and write it in the target file's layout | instant |
| `animexport project.bbmodel out.json [--step S] [--bones a,b] [--clips c,d]` | GeckoLib animation JSON from a project's clips, sign-corrected and spline-baked, or just some bones' tracks spliced into the shipped file | instant |
| `animlayer in.animation.json out.json --clips a,b [--over sit,idle,walk]` | Each clip as the game draws it over each base clip, and the base channels it inherits | instant |
| `textures project.bbmodel out_dir [--only coat ...]` | The coat sheets embedded in a project, written back as PNGs | instant |

It needs nothing but the system Python, and it is split by concern: `pngio.py`, `bake.py`,
`render.py`, `checks.py`, `bbmodel.py`, `anim.py`, `layer.py`, `layout.py`, `suit.py`, each with its own `test_*.py`, and `dogart.py` as the
command line. The geometry follows GeckoLib 4.7's `BakedModelFactory` exactly: x mirrored, bone
and cube x and y angles negated, box UV sizes floored, the first side patch on the model's +x
face, the bottom face laid out front to back, mirrored cubes swapping their side patches and
flipping every face, texels drawn when alpha reaches the cutout threshold of 26. `test_bake.py`
pins each of those per corner against values read from the GeckoLib source, `test_bbmodel.py`
pins the project face layout against what the MCP writes, and `geocompare` against the MCP
export proves the same conventions on the real rigs. Run the tests with:

```bash
python3 -m unittest discover -s .claude/skills/dog-art/scripts -p "test_*.py"
```

The rig profile beside the geo is read automatically. Its `eyes` bones are the eyelids, hidden
inside the head at rest; `--asleep` moves them to the pose the `sleep` clip holds them in, read
from the animation file beside the geo, and keeps the open render's frame. Diff an open and an
`--asleep` render of the same view to see exactly what closing the eyes changes. Fur bones follow `--fur-stage` the way `FurLook` does in
the client. Blockbench's renderer knows none of this, so a contact sheet of the project shows open
eyes; render the `sleep` clip to see them closed.

Review a costume suit sheet with `suit` on every rig before it goes in game. It fills each slot
face from the sheet cell for its direction, as `OverlayProjector` does, uses the rig's
`sheet_overrides`, and turns the directions by `--spin`, so pass `--spin 180` for the dachshund
here too. It shows the suit only, not the costume's parts model.

A suit's head front cell must stay clear from row 4 to row 11 across its full width. That band
lands on the eyes of every rig, and on the smallest faces each texel samples a whole column of
the cell, so even a one pixel rim at the side covers an eye. `OverlayProjectorTest` projects every
costume onto every rig and fails when a pupil, or the texel either side of it, is painted. A
small extra cube on the face, such as the shiba's cheeks, takes another slot's cells through the
profile's `suit_slots` in a suit only, and `suit` follows it.

Views: `front`, `front34`, `left`, `right`, `back`, `back34`, `top`, or `--yaw` and `--pitch`.
`left` is the dog's own left. The dachshund rig faces the other way (its body yaw offset is 180 in
`UnleashedDogBreed`), so pass `--spin 180` to get its face in the `front` view.

## Workflow

1. **Scope the change and pick its gate** from the table below before touching a file.
2. **Read the rig first.** `faces --profile` gives the exact pixel rectangle of every face, grouped
   by rig profile slot, and `uvmap` draws it. Never guess where the snout is on the sheet. The
   sheet can be a different size from the geo's `texture_width`; `faces` prints the scale.
3. **Author in the project.** Rig edits go through `bbmodel_edit`. Textures beyond a flat sprite
   live in the project too: a mechanical recolour or marking fix is a `patch` block written in
   `$SCRATCH` (palette lines `x=#RRGGBB` or `x=transparent` first, then one character per texel,
   `.` for transparent, a space to leave a texel alone; once the first texel row starts every
   following line is a row, so the first row must paint something and `#` cannot be a palette key), applied to the PNG and put back into the project with
   `update_texture`; freehand painting happens in the Blockbench app on the
   same project (`blockbench_launch` opens it), then the PNG is read back out. Either way the
   texture is reviewed on the rig with `bbmodel_contact_sheet` before it ships. Commit the PNG and
   the exported geo, never the block text or the project.
4. **Look at it.** `bbmodel_contact_sheet` on the project for any rig change, `turntable` on the
   export for texture edits and before-and-after pairs, then `render --view left --pitch 0` for
   the silhouette alone. Open the PNG. Reading JSON or block text is not looking. The first
   `bbmodel_render` on a machine downloads Blockbench's render engine (about 130 MB), so say so
   before triggering it.
5. **Check it.** `check` for holes, stray paint and off-sheet faces, `diff` against `main` for
   anything that must not change:
   ```bash
   git show main:$A/textures/entity/beagle_tri1.png > "$SCRATCH/main.png"
   python3 $D diff "$SCRATCH/main.png" $A/textures/entity/beagle_tri1.png "$SCRATCH/diff.png" --fail-on-change
   ```
6. **Prove it in game** with `scripted-qa`: Pet Manager portrait, a close-up at two blocks and a
   view at ten blocks, each beside the same shot on `main`. World lighting, animation and the
   portrait scale all change how a coat reads, and only the game shows them. Some checks only
   mean something under the right conditions:
   - The head tilt only plays while the nearest player holds a treat or a taming item, so a tilt
     shot without one proves nothing.
   - A size claim against another mob (as big as a horse) is a measurement, not a look: put both
     at the same distance on a plain background and compare the back height in pixels.
   - Gear goes on every breed it changes. A pendant over armour must show its whole charm,
     back plate included, from the front and three-quarter views.
7. **Hand off.** The PR carries the renders or screenshots the gate asks for. Anything the artist
   corrects afterwards goes into `reference/review-log.md` as before, after and why.

## Gates By Change

| Change | Must show | Must stay |
|---|---|---|
| New coat variant on a shipped rig | `turntable` plus the three in-game shots beside a sibling coat | `check` clean of stray paint, no new holes |
| Recolour or marking fix | `diff` image showing only the intended region | every other texel identical |
| Rig edit on a shipped breed | `geocompare` against `main` listing only the intended cubes, before and after `turntable` at one scale | `diff` zero on textures, `RigProfilesTest` green |
| Fur bones for ADR 0005 | `turntable` per fur stage with and without the new bones | `geocompare` shows only added bones, `diff` zero on the coat textures |
| Trimmed or botched overlay | Overlay rendered over two different coats of the breed | overlay follows the base UV exactly, `faces` rectangles match |
| Eyelids or a closed-eye pose | Open and `--asleep` `render` of every coat from front, side and top, diffed: change only at the eyes | `RigProfilesTest` and `UndeadDogEyesTest` green, sleep compared to `main` |
| Moving a breed onto a new rig or pipeline | Measured A/B in game: same pose, same distance, same coat, pixel diff of the screenshots | identical until the issue says otherwise |
| New breed | Everything above plus the `add-dog-breed` checklist | every shipped breed's sheets and geo untouched |

A migration proves parity first and new capability second. Never validate a new look against the
assumption that produced it: the reference is always the shipped asset on `main` or the game.

## Rig And Texture Facts You Need

- In the geo file `+x` is the dog's own left and `-z` is forward. Rig profiles name left and right
  the same way. Blockbench and the game mirror x, so the first side patch of a box UV is the dog's
  right flank and the third is its left. The renderer draws that mirrored frame, so a mark painted
  into the left ear's patch shows on the dog's left in its output and in game.
- Sheets are 128x128 for the beagle, dachshund, golden retriever, shiba inu and husky. The husky's
  art sits in the top left 64x64 of its sheet. Texel density on the thinnest axis of a cube sits at
  a median of 2.5 for the beagle and dachshund, 4 for the shiba inu, 5 for the golden retriever
  and about 1 for the husky. New art matches the breed it sits on. A new breed uses 128x128 at 4
  to 5.
- That density comes from the rig idiom: cubes are authored large with a big negative `inflate`.
  The shiba inu head is an 8-unit cube with inflate -3, so it draws 2 units wide with 8 texels
  across it, 4 per unit. The husky is native scale with no inflate trick. The small rigs are 6 to
  10 units tall and scaled up by `RenderTransforms` in `UnleashedDogBreed`. Keep the idiom when adding
  cubes to a rig; a cube with no inflate on a small rig lands at a fraction of the texel density and
  looks blurry beside its neighbours. `check` flags that as `DENSITY`.
- Parts that only show in some clips are parked inside the head and moved out by those clips. The
  dire wolf's four fangs sit inside the snout and jaw and only `growl` slides them out. Every
  other clip leaves them hidden, so a rest-pose render must stay pixel-identical after adding a
  parked part.
- Every rig sets up its eyelids the same way (ADR 0011). Each eye has its own lid, `eyelid_left`
  and `eyelid_right`, children of the head and listed under `eyes` in the profile. At rest they sit
  inside the head, so a clip that does not close the eyes needs no lid keys. Closed is one pose per
  rig, a position offset plus a scale where a lid is parked at a different size than it shows,
  and every clip that closes the eyes uses it: `sleep` holds it and `idle` blinks to it. A closed
  lid stays inside the head except its front, which lies on the face over its eye, and on every
  coat it covers both pupils and changes nothing more than two texels from a pupil. Paint each
  coat's closed eye on the lid, lined up with the head's texel grid. A clip may pose the lids
  another way on purpose, as the dire wolf's `growl` tilts them into a scowl. `RigProfilesTest`
  and `UndeadDogEyesTest` check all of it on every rig and coat.
- Zero-width cubes are flat planes and the artist uses them: the shiba inu carries six `fur`
  planes behind the rump that draw at every fur stage because its profile lists no fur slots.
  `FurLook` hides a bone only when the profile names it under `fur_shaggy` or `fur_overgrown`.
- Never rename bones. Armour, pendants, fur and carried items find bones through the rig profile,
  and `RigProfilesTest` fails on a name the rig no longer has. Add fur as child bones of the bones
  they grow from so they follow every animation (ADR 0005), then list them in the profile.
- Rigs do not share a skeleton, a UV layout or a texture sheet, and no change may introduce one
  (epic 413 rules that out). Cross-breed looks come from pigment and part swaps, not mesh blending.
- A profile's `pupils` are two texels on the coat sheet, not bones: the painted pupils on the
  head's face, where undead dogs glow. `faces` prints which face each one lands on and `uvmap`
  outlines them in magenta. Moving the eyes in a repaint means moving these two texels with
  them; `UndeadDogEyesTest` fails when a pupil leaves the head or misses the dark eye pixels.
- In game, `SlotRegions` derives each slot's texel rectangles from the baked rig. That is the
  authority for code that paints onto a slot; `faces` is the authoring view of the same thing,
  and both read the same GeckoLib layout.
- Collar textures are a single flat white tinted at render time. A collar must wrap the neck as
  a band you can see from the side and back, not a patch on the chest, and it must be painted on
  the front of the throat: the charm hangs from the lowest painted collar texel on a forward
  face. Overlays are translucent PNGs
  over the coat's own UV.
- Several shipped faces are partly or fully transparent on purpose: eyelid and eye cubes are
  overlays over the head, and mirrored tuft cubes share one patch. `check` reports those as holes
  and unpainted faces without failing. `--strict` makes holes, stray paint and off-sheet faces
  fail and is for new sheets. `--fail-unpainted` is for a sheet that must have every face painted.
- Author every cube with whole-number sizes and reach the visible size with `inflate`. GeckoLib
  floors fractional sizes before laying out box UV, but Blockbench and `bbmodel_render` do not, so
  a fractional cube looks right in game and shows see-through gaps and shifted faces in Blockbench.
  With one inflate per cube its visible sides differ by whole numbers; design within that, and
  anchor the faces that must line up (paws on the ground, the nose at the muzzle tip).

`reference/style-guide.md` holds the measured numbers behind these rules, the colour findings and
the proportions of every shipped breed.

## Known Limits

- Rest pose only, with the fur slots hidden the way the client does and the eyelids left out
  unless `--asleep`. The only animation read is the closed-eye pose for `--asleep`, so a pose-dependent clip (an ear through a cheek when
  sitting) only shows in game or in `bbmodel_render` with a clip.
- Face brightness is the block ramp, not entity lighting. Judge tone relationships, not absolute
  brightness.
- `tobbmodel` carries geometry and one texture, not animations. The artist's own project is the
  source for those.

## Related

- `add-dog-breed`: the file checklist for a whole new breed.
- `geo-prop` and `item-sprite` in the minecraft-modding plugin: props and flat sprites.
- `scripted-qa`: in-game evidence for the PR.
- ADR 0003 (armour painted onto each rig's torso) and ADR 0005 (fur hand-made per rig).
