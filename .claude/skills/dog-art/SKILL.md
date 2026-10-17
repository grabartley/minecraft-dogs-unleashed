---
name: dog-art
description: Author, review and gate dog rigs, coat textures, fur bones and overlays for Dogs Unleashed without launching the game. Headless textured renders, UV face maps, texture checks and pixel diffs over the shipped GeckoLib rigs. Use for any change under geo, textures/entity, animations or rig_profiles, for recolours and coat variants, and before any dog art goes to review.
---

# Dog Art

Dog art is the mod's storefront. The bar is premium: a new coat has to sit beside the shipped
ones without looking like it came from a different mod, and a rig change must leave every shipped
breed pixel-identical unless the issue says otherwise. This skill makes that measurable and fast.
Nothing in it needs the game, numpy or Pillow: one script on the system Python.

```bash
D=.claude/skills/dog-art/scripts/dogart.py
A=src/client/resources/assets/dogs-unleashed
```

Small props and flat item sprites are not this skill: use `geo-prop` and `item-sprite` from the
minecraft-modding plugin. Blockbench is still the right tool for free-orbit review and organic
sculpting from scratch, and the only tool the artist uses, so everything here reads and writes the
same files Blockbench does.

## The Tool

| Command | What it answers | Speed |
|---|---|---|
| `render geo png out --view left` | What does the silhouette and texture look like from here | 0.2 s |
| `turntable geo png out` | Left, three-quarter, front and top in one strip | 0.5 s |
| `faces geo png --profile rig.json` | Which texture pixels are the snout top, the left ear, the belly | instant |
| `uvmap geo png out --profile rig.json` | The same, drawn over the texture, colour per rig slot | instant |
| `palette png` | Every colour, grouped by hue, sorted by tone, with counts | instant |
| `check geo png [--palette file] [--fail-unpainted] [--strict]` | Holes on mapped faces, paint no face reads, density outliers, palette drift | instant |
| `diff a.png b.png [out.png] [--fail-on-change]` | How many pixels changed, and where | instant |
| `patch base.png block.txt out.png --at U V` | Paint a palette-mapped text block onto a texture | instant |

Views: `front`, `front34`, `left`, `right`, `back`, `back34`, `top`, or `--yaw`/`--pitch`. The
renderer applies bone and cube pivots and rotation, inflate and mirror the way GeckoLib bakes them,
in rest pose, with a block-style face brightness ramp. It is not the game. It is close enough to
kill a wrong proportion, a flipped face or a hole in seconds.

The dachshund rig faces the other way (its body yaw offset is 180 in `UnleashedDogBreed`), so pass
`--spin 180` to get its face in the `front` view.

## Workflow

1. **Scope the change and pick its gate** from the table below before touching a file.
2. **Read the rig first.** `faces --profile` tells you the exact pixel rectangle of every face,
   grouped by rig profile slot, and `uvmap` shows it. Never guess where the snout is on the sheet.
   The texture may be a different size from the geo's `texture_width`; `faces` prints the scale.
3. **Author as text.** Texture edits are `patch` blocks written in a scratch directory, in the
   `item-sprite` palette-mapped format (`x=#RRGGBB` lines, then one character per texel, `.` for
   transparent, space to leave a texel alone). Rig edits are JSON edits to the `.geo.json`. Commit
   the PNG and the JSON, never the block text.
4. **Look at it.** `turntable` on every edit, then `render --view left --pitch 0` for the silhouette
   alone. Open the PNG. Reading the JSON or the block text is not looking: every bad shape in this
   repo's history read fine as numbers.
5. **Check it.** `check` for holes and stray paint, `diff` against `main` for anything that must
   not change:
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
| Rig edit on a shipped breed | Before and after `turntable` at the same scale, overlaid or side by side | `diff` zero on textures, rig profile test green |
| Fur bones for ADR 0005 | `turntable` per fur stage with and without the new bones | base rig cubes untouched, `diff` zero on the coat textures |
| Trimmed or botched overlay | Overlay rendered over two different coats of the breed | overlay follows the base UV exactly, `faces` rectangles match |
| Moving a breed onto a new rig or pipeline | Measured A/B in game: same pose, same distance, same coat, pixel diff of the screenshots | identical until the issue says otherwise |
| New breed | Everything above plus the `add-dog-breed` checklist | the four shipped sheets untouched |

A migration proves parity first and new capability second. Never validate a new look against the
assumption that produced it: the reference is always the shipped asset on `main` or the game.

## Rig And Texture Facts You Need

- Coordinates: in the geo file, `+x` is the dog's own left, `-z` is forward. Rig profiles name
  left and right the same way. The renderer draws the mirrored frame GeckoLib bakes, so a mark you
  paint on the left ear shows on the dog's left in its output. Side-specific marks are still
  confirmed in game before they ship: render `husky_blackwhite_bluehazel` front-on and compare
  with a `scripted-qa` close-up.
- Sheets are 128x128 at about 5 texels per visible unit for the beagle, golden retriever and shiba
  inu, 128x128 at 2.5 for the dachshund, and 64x64 at 1 for the husky. New art matches the breed
  it sits on. A new breed uses 128x128 at 5.
- That density comes from the rig idiom: cubes are authored large with a big negative `inflate`
  (down to -4.7), so a 6-unit cube draws 2.2 units wide with 6 texels across it. The small rigs
  are 6 to 10 units tall and scaled up by `RenderTransforms` in `UnleashedDogBreed`. Keep the idiom
  when adding cubes to a rig; a cube with no inflate on a small rig lands at a fifth of the texel
  density and looks blurry beside its neighbours. `check` flags that as `DENSITY`.
- Never rename bones. Armour, pendants, fur and carried items find bones through the rig profile,
  and `RigProfilesTest` fails on a name the rig no longer has. Add fur as child bones of the bones
  they grow from so they follow every animation (ADR 0005), then list them in the profile.
- Rigs do not share a skeleton, a UV layout or a texture sheet, and no change may introduce one
  (epic 413 rules that out). Cross-breed looks come from pigment and part swaps, not mesh blending.
- Collar textures are a single flat white tinted at render time. Overlays are translucent
  PNGs over the coat's own UV.
- Several shipped faces are partly or fully transparent on purpose: eyelid and eye cubes are
  overlays over the head, and mirrored tuft cubes share one patch. `check` reports those as holes
  and unpainted faces without failing. `--strict` makes holes and stray paint fail and is for new
  sheets, and `--fail-unpainted` is for a sheet that must have every face painted.

`reference/style-guide.md` holds the measured numbers behind these rules, the colour findings and
the proportions of every shipped breed.

## Blockbench

Open the rig in Blockbench when you need to orbit it, when a shape is organic rather than boxy,
and always once before hand-off. `geo-prop` documents the file-dialog and texture-assignment tricks
and the x mirror Blockbench shows; they apply unchanged. Export, then re-run `faces` and `check`,
because every face rectangle moves when a cube's size does.

## Known Limits

- Rest pose only. Animation files are not read, so a pose-dependent clip (an ear through a cheek
  when sitting) only shows in game.
- Face brightness is the block ramp, not entity lighting. Judge tone relationships, not absolute
  brightness.
- Fractional cube sizes leave partly covered edge texels. `check` ignores them; the game shows a
  sliver of whatever is there, so paint the full rounded rectangle.

## Related

- `add-dog-breed`: the file checklist for a whole new breed.
- `geo-prop` and `item-sprite` in the minecraft-modding plugin: props and flat sprites.
- `scripted-qa`: in-game evidence for the PR.
- ADR 0003 (armour painted onto each rig's torso) and ADR 0005 (fur hand-made per rig).
