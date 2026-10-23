---
name: adopt-artist-art
description: Bring artist-made assets (models, animations, textures, .bbmodel files, PNGs) into the repo so they follow the code structure and look in game exactly as the artist made them. Use whenever the user hands over any artist-made asset, for a costume, cosmetic, prop, breed or anything else, even if the file is messy or not in the repo's format.
---

# Adopt Artist Art

The artist's drawing is the spec. The job is to change the code and file layout around it, never the drawing. Pixels, cube sizes, pivots, rotations, UVs and names stay as drawn. If something looks wrong or unclear in the drawing, ask the artist. Do not fix it.

## 1. Read the file without touching it

- The `bbmodel` MCP only opens files under `~/blockbench`. Copy the artist's file in under a new folder, for example `~/blockbench/<asset>/`, and leave the original where it is.
- Work out what the file holds before deciding anything: `bbmodel_info`, `bbmodel_outline`, then `bbmodel_get_node` with `target` set to a uuid for each group and cube you care about. Note bounds, origins, rotations, inflate and whether each cube uses box UV or per-face UV.
- Files are often a whole rig with one new group inside it, or the art is built in the rig's own space. Find out which bone it hangs under and what that bone's pivot is. Compare the rig in the file with the repo's rig for that breed (`geo/<breed>.geo.json`). The geo file's pivot x is the negative of the Blockbench origin x, so a flipped sign is correct. If the rig in the file differs from the repo's, stop and ask.
- Render it with `bbmodel_render` and `bbmodel_contact_sheet` first, so you know what the artist meant before you change anything.

Animations: keep keyframes, times, easing and bone names exactly as drawn. Check each animated bone exists on the target rig, export the clip through the MCP, and compare the pose in game against `bbmodel_sample_pose` at the same times.

## 2. Cut the project down to the art, keep every value

- In the copy, remove everything that is not the art with `bbmodel_edit` `remove_node`, and keep the parent groups the art hangs under so its pivots stay valid. Never move, resize or re-UV what stays.
- The texture is embedded in the `.bbmodel`. It can have several layers. Ship the saved composite, which is the texture's `source` data URL decoded from base64, byte for byte. Do not repaint, resize, trim or recolour it. Decode it with `python3 -I` and the standard `base64` module.
- Export geometry with `bbmodel_export_bedrock_geometry` into the asset's folder in `~/blockbench`, then copy the export into the repo. The exporter mirrors x, applies inflate and keeps per-face UVs, which is how the repo's other geometry is made. Never hand-edit a geo file, an animation or a texture.

## 3. Fit it into the repo's structure

- Follow the existing asset paths and naming for the kind of thing it is. Mob head art is `geo/cosmetic/<head item>/<breed>.geo.json` and `textures/entity/cosmetic/<head item>/<breed>.png`. Costumes follow ADR 0012. Props follow `geo-prop`.
- Prefer data over code. A new breed or item should only need new files. A new rule goes in a small class with a matching `FooTest`, not into a class that is already big.
- Draw the art in the space it was drawn in. If it was drawn in the rig's space, attach it to the bone of the same name, with no rotation on the bone it attaches to. Child bones keep the artist's own rotations, such as the shiba pumpkin's 10 degree tilt. Do not scale, fit or move it. It then gets the same transforms as the rig's own cubes on that bone. If that bone has a rotated parent (the husky's neck is one), check the art against a reference before shipping, because it has not been proven for that case.
- Keep behaviour that was not asked for. Other breeds, other heads and other cosmetics must render as before. A breed without art keeps its current look.
- Update the docs the change invalidates, such as a PRD row when player-visible behaviour changes. The README only changes when the player-facing pitch changes.
- No code comments, no emoji, no em dashes. Translate any new lang key in every lang file.

## 4. Tests

- Pure decisions go in a helper with a parameterised test named after it. A layer or renderer that only draws has no pure part left, and the PR body must say so.
- Add a test that every shipped piece of art has its texture and that every bone it rides exists on that breed's rig. It catches a renamed bone later.
- Client tests run with `./gradlew clientTest --tests '*Name*'`. `./gradlew test` does not run them.

## 5. Prove it matches, with an independent reference

A check built from the code under test proves nothing. Use the artist's own file as the reference.

1. Make a scratch copy of the artist's original file under `~/blockbench` with the real coat painted into the texture, so the rig is visible next to the art. It is only for comparing and never ships. Pillow is fine for this scratch work under plain `python3`. Check first that the coat has no pixels where the art's UVs sit. Render it with `bbmodel_contact_sheet` from the front, back, left, right and top.
2. In the game, stage the same breed with the art and take the same views with a narrow field of view (30) from far enough away that perspective is small. Read each pair for proportions, how far the art covers the head, what sticks out of it, which side the stem or detail leans to, and where it meets the body.
3. Add controls in the same frame: the same breed without the art, and another breed that falls back to the old look. Nothing about them may change.
4. Compare the shipped PNG with the decoded `source` bytes from step 2 by hash. Lighting and filtering in game differ from Blockbench, so do not diff screenshots pixel by pixel across the two renderers.
5. Say in the PR what was compared and what was not (for example puppies, other poses).

## Game harness notes

- Use the `scripted-qa` skill. Use the `QA` world in `run/saves`. The worktree's `run/` is a copy.
- Add a temporary `/qa dress <tag> <item>` command that puts the item in the dog's cosmetic slot. Never commit the harness. Before committing, revert the `QaCommands.register()` line in `DogsUnleashed.java` and the `LiveQaDriver.register()` line in `DogsUnleashedClient.java`, and delete the temporary `qa` packages.
- A staged dog that you teleport must be un-staged first (`qa unstage <tag>`) or the next shot is refused.
- `cmd summon` with `Age:-24000` did not give a visibly smaller dog, so do not use it as proof of a puppy.
- Shell `cwd` can reset to the main checkout. Start every command with an explicit `cd` into the worktree.
- Stop the client with `qa.sh run stop` before ending the turn.

## Ask the artist when

- the rig in their file differs from the repo's rig,
- parts of the art poke through or sit inside the body in a way that looks unintended (for example eyes showing past the art),
- the texture has layers that look unfinished or hidden,
- the art would need to be scaled, fitted or changed to work in game.
