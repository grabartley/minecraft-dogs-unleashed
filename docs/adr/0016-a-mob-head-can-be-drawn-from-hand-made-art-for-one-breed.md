# ADR 0016: A mob head can be drawn from hand-made art for one breed

- **Status:** Accepted
- **Date:** 2026-10-23

## Context

Mob heads are vanilla blocks and skulls. The renderer stretches them over the dog's head from the head bounds in the rig profile. That fits every breed, but a stretched vanilla block cannot match a drawing, and the artist wants to draw the carved pumpkin on each breed's own head.

## Decision

Art for a head on a breed is an optional GeckoLib model at `geo/cosmetic/<head item>/<breed>.geo.json` with its texture at `textures/entity/cosmetic/<head item>/<breed>.png`. The breed is the dog's rig source breed, so a cross uses the art of the breed whose rig it stands on.

The model is drawn in the same space as the dog's rig. Each top-level bone has the name of a dog bone and no rotation of its own, and it is drawn on that bone. It gets the same transforms as the dog's own cubes on that bone, so the art follows the head in every animation and sits under any rotated parent the way it did in the artist's project. The artist builds the art in the Blockbench project next to the dog and exports only the head bone and what hangs off it. Nothing is fitted or stretched.

A breed with art for the worn head draws that art and not the vanilla block. A breed without art keeps the stretched vanilla block, so breeds get art one at a time.

## Consequences

The art shows exactly as drawn, and adding a breed means adding two files. The art is tied to one rig, so a change to that rig's head or ears can leave the art in the wrong place and the artist has to check it. The ears stay hidden by the same rule as for the vanilla block, which covers the ears the art covers on the dachshund. A breed whose ears stick out of the art would need its own rule. Only the dachshund has art so far, and its head bone has no rotated parent. A breed with one needs its art checked against a reference before it ships.
