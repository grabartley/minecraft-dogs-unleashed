# ADR 0014: A cross stands on one parent's rig and takes colour, ears and shape from the rest

- **Status:** Accepted
- **Date:** 2026-10-21

## Context

A mixed breed dog used to render exactly as its largest breed share, so a beagle and husky cross looked like a plain beagle. Blending the meshes was tried and reverted, because the rigs have different bones, cube counts and texture layouts. A 21 cube beagle cannot be morphed into a 66 cube golden retriever.

## Decision

A cross stands on the rig of its largest breed share and borrows three things from its other breeds, each weighted by that breed's share of the genome.

**Colour.** Every coat ships a four colour palette: main, secondary, marking, and nose and pads. Each coat texel inside a profile slot region is matched to the nearest colour in its own palette, then shifted by the gap between that colour and the same entry in each other breed's palette, times that breed's share. Shifting rather than replacing keeps the shading painted into the coat. Eyelids and pupils are left alone. Which coat of the other breed to borrow is picked from the dog's coat and genome, so it never changes for one dog. The tinted coat is built once per combination and cached, and the undead and statue effects work on top of it.

**Ears.** Ears never blend. Each rig profile names its own ear shape, and may list extra ear pairs for other shapes. A cross picks one breed to take its ears from, with a chance equal to that breed's share. If that breed has a different shape and the rig carries a pair for it, the rig's own ears are hidden and that pair shows instead. Extra pairs are hidden on every other dog.

**Shape.** Each rig's head, torso, legs and tail are measured against the dog's height, with the legs measured as the gap between the belly and the ground. The head, torso, leg and tail slots are then scaled toward the other breeds' measurements, through the same slot scaling and ground fit as dire wolf growth (ADR 0007). Each ratio is capped at a quarter either way, and the whole model is shrunk or grown so its height stays the same.

Pure breeds take none of these paths, so they render exactly as before.

## Consequences

Crosses read as mixes without any shared mesh, and a new breed joins in with no code change once it ships palettes and its profile names its ear shape.

The tint moves colours along fixed palette entries, so it cannot move a marking to a new place: a cross keeps its rig's pattern in the other breed's colours. Matching texels to the nearest palette colour can put a texel that sits between two colours on the wrong side, which shows as speckling on soft gradients. Slot scaling stretches whole parts uniformly, so a torso measured by its longest side changes length and girth together. Ear swaps need the art for the extra pairs, and until a rig carries them its crosses keep that rig's own ears.
