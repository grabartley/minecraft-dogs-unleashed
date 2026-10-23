# ADR 0016: A mob head can be drawn from hand-made art for one breed

- **Status:** Accepted
- **Date:** 2026-10-23

## Context

Mob heads are vanilla blocks and skulls. The renderer stretches them over the dog's head from the head bounds in the rig profile. That fits every breed, but a stretched vanilla block cannot match a drawing, and the artist wants to draw the carved pumpkin on each breed's own head.

## Decision

Art for a head on a breed is an optional GeckoLib model at `geo/cosmetic/<head item>/<breed>.geo.json` with its texture at `textures/entity/cosmetic/<head item>/<breed>.png`. The breed is the dog's rig source breed, so a cross uses the art of the breed whose rig it stands on.

The model is drawn in the same space as the dog's rig. Each top-level bone has the name of a dog bone and is drawn on that bone, so the art follows it in every animation. The artist builds the art in the Blockbench project next to the dog and exports only the head bone and what hangs off it. Nothing is fitted or stretched. Where the dog's bone has a rest rotation, the art is turned back by it so it lands where it was drawn.

A breed with art for the worn head draws that art and not the vanilla block. A breed without art keeps the stretched vanilla block, so breeds get art one at a time.

## Consequences

The art shows exactly as drawn, and adding a breed means adding two files. The art is tied to one rig, so a change to that rig's head or ears can leave the art in the wrong place and the artist has to check it. Ears are still hidden by the stretched fit of the vanilla block, which is the same set the art covers on the breeds drawn so far. A breed whose ears stick out of the art would need its own rule.
