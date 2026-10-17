# ADR 0003: Dog armour is painted onto each breed's own torso cubes

- **Status:** Accepted
- **Date:** 2026-10-17

## Context

Dog armour has five tiers of our own plus vanilla wolf armour, and has to look right on five breeds whose rigs share nothing: different bone names, cube counts and atlas sizes. Most torsos are also built from cubes shrunk by negative inflate, and each breed's whole model is scaled up at render time by its own amount. Hand-painting an armour texture per tier per breed is 30 textures today, and every new breed or rig edit would need more. Vanilla's own wolf armour renderer never runs on our dogs, because they render through GeckoLib.

## Decision

Armour is drawn as a slightly larger copy of each breed's torso cubes, with a texture painted at load time onto that breed's own UV layout.

Which bones count as the torso is a short per-breed list, the start of the rig profile idea. A breed can limit a bone to some of its cubes, as the golden retriever does to leave out its chest fluff. The painter maps every texel of every visible torso face back to the model, works out which edge of the face points down and which way runs along the body, and paints a whole-body pattern in that face's own pixel grid: a dark hem along the bottom, a rivet or stitch row above it, ribbed plates or leather on the flanks, a ridge along the spine, and a strap and buckle under the belly. Faces buried inside another torso cube are left clear, and where two cubes share a flat side only the first paints it, so nothing flickers. Each tier is a palette and a pattern, not a texture file.

The copy is pushed out by a quarter of a model unit, a little more than vanilla's wolf armour, so the real thickness on screen varies with each breed's render scale. Where a breed's legs sit wider than its torso, the sides are pushed out further, just enough to clear them, so legs never poke through while the dog breathes or walks.

Leather and vanilla wolf armour have a second, dyed layer painted on pixels the plain layer leaves empty, so the two layers never fight. Cracks are a third, translucent layer whose lines only grow as the armour wears.

Like vanilla wolf armour, worn armour takes the whole hit in place of the dog. So that the protection value of a tier still means something, the armour loses durability by the damage that would have got through its protection and toughness, the same sum vanilla uses for player armour. Better tiers last longer under the same hits.

## Consequences

A new breed gets armour that fits by naming its torso bones, with no new art. The look of a tier changes in one place for every breed.

The cost is that the armour is one texel per model unit on each breed's own atlas, so the detail available is whatever that breed's UV layout gives. The patterns are designed for that density. Dyes and cracks cost one extra draw each for the bones that wear armour.

Vanilla wolves still render our armour with the vanilla renderer, which uses one texture for every modded canine armour material, and vanilla wolves only accept `minecraft:wolf_armor` by right-click. Our tiers on vanilla wolves are left to a separate change.
