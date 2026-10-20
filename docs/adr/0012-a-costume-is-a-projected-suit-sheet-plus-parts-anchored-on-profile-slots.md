# ADR 0012: A costume is a projected suit sheet plus parts anchored on profile slots

- **Status:** Accepted
- **Date:** 2026-10-20

## Context

Mob costumes cover a dog's whole body and must fit every breed, adults and puppies, crosses, undead dogs and the dire wolf at every growth stage. There are seventeen costumes planned and six rigs today, with more breeds to come. Painting every costume on every rig by hand would be over a hundred textures, and every new breed would need another seventeen. Most costumes also have parts that stick out of the body, such as a spider's legs or a bee's wings, which a texture cannot show.

ADR 0006 already projects one normalized sheet onto each rig's own texture layout, with per-rig overrides in the rig profile. Hats and mob heads already sit on a head bone found from the rig profile rather than from code per breed.

## Decision

A costume's look is named after its item. For an item `<namespace>:<name>`:

- The body suit is an overlay sheet at `<namespace>:textures/entity/costume/<name>.png`, in the 192 by 144 layout of ADR 0006. It is projected onto the dog's rig and drawn over the coat. A rig that needs a hand-made fix names an atlas-sized texture for that sheet and slot under `sheet_overrides` in its rig profile.
- Parts that stick out are an optional GeckoLib model at `<namespace>:geo/costume/<name>.geo.json` with its texture at `<namespace>:textures/entity/costume/<name>_parts.png`. Each top-level bone is named `costume_` plus a rig profile slot, such as `costume_torso`, `costume_head` or `costume_leg_front_left`. Every bone in the model starts with `costume_`, so none can share a name with a dog bone.
- A part is modelled around a reference box one block wide on every axis, centred on the model origin, with the dog's front facing north. At render time that box is stretched over the rest-pose bounds of the slot's bones on the dog's rig, turned for rigs built facing the other way, and attached to the slot's shallowest bone, so the part follows that bone in every animation. A part surface that wraps the body sits just outside the reference box, never on it, so it does not fight the coat for the same pixels.

The suit is drawn after the fur and sheet overlays and before armour, and the collar is drawn again over it. Parts are drawn with their anchor bone. Pendants, cosmetics and mob heads draw later, so they sit over the costume. While armour is worn the costume is not drawn at all.

## Consequences

An artist paints one sheet and, if needed, one part model per costume, and every breed shows it, including breeds added later. A breed that needs special treatment on one slot gets an override without affecting the others.

The suit inherits the limits of ADR 0006: faces stretch their cell, so suit patterns should survive stretching, and the eye texels should stay clear so the undead eye glow still shows. Parts stretch with the slot, so a part on a long, low torso is longer and lower than the same part on a tall one. Parts are placed from rest-pose bounds, so they do not adjust to a pose beyond following their anchor bone. A slot made only of flat cubes flattens its parts along that axis. A part anchored on a bone the renderer hides, such as an ear under a hat or fur that is trimmed away, is hidden with it. A costume with no suit sheet logs one warning per rig and draws only its parts.
