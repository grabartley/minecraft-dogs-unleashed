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
3. `bbmodel_export_bedrock_geometry` to the repo's geo path. That is the same geometry the GeckoLib
   plugin's export writes.
4. `python3 $D geocompare` the new geo against `main` to see every bone and cube that moved, then
   the gates below.

If a breed has no project yet, make one from the shipped export: `python3 $D tobbmodel geo png
out.bbmodel`. Every shipped rig round-trips through that and the MCP export with zero mismatches.

Three rules hold for every rig change: the project is always copied into `~/blockbench/<rig>/`
before it is edited, the repo receives only the exported geo and the PNGs, and no `.bbmodel` is
ever committed. `.gitignore` refuses them.

## The Script

| Command | What it answers | Speed |
|---|---|---|
| `render geo png out --view left` | A quick textured look at an export from one angle, with no engine download | 0.2 s |
| `turntable geo png out` | Left, three-quarter, front and top in one strip, for before and after pairs | 0.5 s |
| `faces geo png --profile rig.json` | Which texture pixels are the snout top, the left ear, the belly | instant |
| `uvmap geo png out --profile rig.json` | The same, drawn over the sheet in a colour per rig slot | instant |
| `palette png` | Every colour, grouped by hue, sorted by tone, with counts | instant |
| `check geo png [--palette file] [--fail-unpainted] [--strict]` | Holes on mapped faces, paint no face reads, faces off the sheet, density outliers, palette drift | instant |
| `diff a.png b.png [out.png] [--fail-on-change]` | How many visible pixels changed, and where | instant |
| `patch base.png block.txt out.png --at U V` | Paint a palette-mapped text block onto a sheet | instant |
| `tobbmodel geo png out.bbmodel` | A Blockbench project of a shipped rig with its texture embedded | instant |
| `geocompare a.geo.json b.geo.json` | Bone-by-bone, cube-by-cube numeric diff of two rigs | instant |

It needs nothing but the system Python. The geometry follows GeckoLib 4.7's `BakedModelFactory`
exactly: x mirrored, bone and cube x and y angles negated, box UV sizes floored, the first side
patch on the model's +x face, the bottom face laid out front to back, mirrored cubes swapping their
side patches and flipping every face. `scripts/test_dogart.py` pins each of those against values
read by hand from the GeckoLib source, and `geocompare` against the MCP export proves the same
conventions on the real rigs. Run the tests with:

```bash
python3 -m unittest discover -s .claude/skills/dog-art/scripts -p "test_dogart.py"
```

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
   `$SCRATCH`, in the `item-sprite` palette-mapped format (`x=#RRGGBB` lines, then one character
   per texel, `.` for transparent, space to leave a texel alone), applied to the PNG and put back
   into the project with `update_texture`; freehand painting happens in the Blockbench app on the
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
   portrait scale all change how a coat reads, and only the game shows them.
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
| Moving a breed onto a new rig or pipeline | Measured A/B in game: same pose, same distance, same coat, pixel diff of the screenshots | identical until the issue says otherwise |
| New breed | Everything above plus the `add-dog-breed` checklist | every shipped breed's sheets and geo untouched |

A migration proves parity first and new capability second. Never validate a new look against the
assumption that produced it: the reference is always the shipped asset on `main` or the game.

## Rig And Texture Facts You Need

- In the geo file `+x` is the dog's own left and `-z` is forward. Rig profiles name left and right
  the same way. Blockbench and the game mirror x, so the first side patch of a box UV is the dog's
  right flank and the third is its left. The renderer draws that mirrored frame, so a mark painted
  into the left ear's patch shows on the dog's left in its output and in game.
- Sheets are 128x128 at about 5 texels per visible unit for the beagle, golden retriever and shiba
  inu, 128x128 at 2.5 for the dachshund, and 64x64 at 1 for the husky. New art matches the breed
  it sits on. A new breed uses 128x128 at 5.
- That density comes from the rig idiom: cubes are authored large with a big negative `inflate`.
  The beagle head is an 8-unit cube with inflate -3, so it draws 2 units wide with 8 texels across
  it, 4 per unit. The husky is native scale with no inflate trick. The small rigs are 6 to 10
  units tall and scaled up by `RenderTransforms` in `UnleashedDogBreed`. Keep the idiom when adding
  cubes to a rig; a cube with no inflate on a small rig lands at a fraction of the texel density and
  looks blurry beside its neighbours. `check` flags that as `DENSITY`.
- Zero-width cubes are flat planes and the artist uses them: the shiba inu carries six `fur`
  planes behind the rump that draw at every fur stage because its profile lists no fur slots.
  `FurLook` hides a bone only when the profile names it under `fur_shaggy` or `fur_overgrown`.
- Never rename bones. Armour, pendants, fur and carried items find bones through the rig profile,
  and `RigProfilesTest` fails on a name the rig no longer has. Add fur as child bones of the bones
  they grow from so they follow every animation (ADR 0005), then list them in the profile.
- Rigs do not share a skeleton, a UV layout or a texture sheet, and no change may introduce one
  (epic 413 rules that out). Cross-breed looks come from pigment and part swaps, not mesh blending.
- Collar textures are a single flat white tinted at render time. Overlays are translucent PNGs
  over the coat's own UV.
- Several shipped faces are partly or fully transparent on purpose: eyelid and eye cubes are
  overlays over the head, and mirrored tuft cubes share one patch. `check` reports those as holes
  and unpainted faces without failing. `--strict` makes holes, stray paint and off-sheet faces
  fail and is for new sheets. `--fail-unpainted` is for a sheet that must have every face painted.
- GeckoLib floors fractional cube sizes before laying out box UV, so a 4.3-wide face reads 4
  texels. Paint the floored rectangle.

`reference/style-guide.md` holds the measured numbers behind these rules, the colour findings and
the proportions of every shipped breed.

## Known Limits

- Rest pose only. Animation files are not read, so a pose-dependent clip (an ear through a cheek
  when sitting) only shows in game or in `bbmodel_render` with a clip.
- Face brightness is the block ramp, not entity lighting. Judge tone relationships, not absolute
  brightness.
- `tobbmodel` carries geometry and one texture, not animations. The artist's own project is the
  source for those.

## Related

- `add-dog-breed`: the file checklist for a whole new breed.
- `geo-prop` and `item-sprite` in the minecraft-modding plugin: props and flat sprites.
- `scripted-qa`: in-game evidence for the PR.
- ADR 0003 (armour painted onto each rig's torso) and ADR 0005 (fur hand-made per rig).
