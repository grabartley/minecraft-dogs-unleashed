# ADR 0006: Shared overlay effects are painted once and projected onto each rig

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

The five breeds use five different texture layouts. A look that should appear on every breed, such as rot on an undead dog, had to be painted five times, once per layout, and again for every breed added later. Undead dogs got around this with a runtime recolour of the coat, which works for a tint but cannot add new marks.

ADR 0005 named each rig's body parts in its rig profile, and the slot regions work found where each slot's cube faces sit on that rig's texture. With both, the mod knows which texels of every rig belong to the head's north face, the tail's top face, and so on.

## Decision

An effect is painted once, on a normalized overlay sheet, and projected onto each rig at load time.

The sheet is 192 by 144 pixels. It is split into 48 pixel squares, four across, one per body slot in this order: torso, mouth, head, eyes, neck, tail, left ear, right ear, front left leg, front right leg, back left leg, back right leg. Fur slots are left out, because fur is hand made per rig (ADR 0005). Each square is split into six cells, 16 wide and 24 tall, three across and two down, one per face direction in this order: down, up, north, south, west, east. North is the dog's front, south its back, east its left and west its right. Directions are taken after the renderer turns the rig, so a rig built facing the other way, such as the dachshund, still takes its front from the north cell.

At load, every face of every cube in a slot is filled from the cell for that slot and that face's direction. The cell is stretched over the face's rectangle on the rig's texture, top left to top left, with nearest neighbour sampling. The result is an atlas-sized texture for that rig and sheet. It is cached and rebuilt after a resource reload. It is drawn as a translucent layer over the coat, the same way the trimmed and botched fur overlays are drawn.

When the projection does not look right on one slot of one rig, that rig's profile can name a hand-made, atlas-sized texture for that sheet and slot under `sheet_overrides`. The override is used for that slot's texels in place of the projection whenever it exists. Other slots still come from the sheet.

A small extra cube in a slot can take another slot's cells instead. The profile lists it under `overlay_slots`, keyed by bone name, with the slot to take cells from. This was added later for the shiba inu's cheeks, which took the head's front cell and showed its face opening. The bone keeps its own slot for everything else.

Code turns an effect on by registering its sheet with a condition on the dog. No effect is registered yet, so every dog looks exactly as it did before.

## Consequences

An artist paints one sheet and every breed, including breeds added later, shows it. A breed that needs special treatment on one part gets an override without affecting the others.

The projection knows nothing about how a face is oriented on the texture beyond its rectangle, so a mirrored face shows its cell mirrored. Two slots that share the same texels on a rig, such as ears that reuse one texture, show whichever slot comes later in the order above. The same goes for faces within one slot, and for the eyes over the head wherever a rig paints them on the same texels. The undead eye glow is drawn before the sheet, so a sheet that paints over a rig's pupils hides the glow; sheets should leave the eye texels clear. Only bones listed in a body slot of the rig profile are painted, so a cube left out of every slot keeps its plain coat under an effect. Stretching a 16 by 24 cell over a face of a different shape distorts fine detail, so sheets should favour patterns that survive stretching, such as blotches and speckles, over shapes that need to stay round.

Armour keeps its own per-rig painter (ADR 0003), because it is drawn on an inflated copy of the torso rather than on the coat.
