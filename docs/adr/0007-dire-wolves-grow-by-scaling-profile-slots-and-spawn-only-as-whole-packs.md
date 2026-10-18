# ADR 0007: Dire wolves grow by scaling profile slots and spawn only as whole packs

- **Status:** Accepted. Its spawning decision is superseded by [ADR 0010](0010-wild-packs-spawn-through-every-path-with-the-leader-bringing-the-pack.md)
- **Date:** 2026-10-18

## Context

The dire wolf is the first breed that changes size after it grows up. It goes through five stages, from a puppy half the size of a normal dog to an alpha 2.5 blocks tall, and each stage should read as a different animal: a pup has a big head and short legs, an alpha has long legs and heavy shoulders. The other breeds only switch between a baby scale and an adult scale.

GeckoLib bakes one model per geo file and shares its bones between every entity using it, so a per-entity change can never be written into a bone. Five separate rigs would multiply the art, the animations and every feature that reads the rig profile.

Dire wolves are also meant to be rare and to live only in packs. Vanilla biome spawning picks a group size and then places each member on its own, so a group of five to ten often lands as two or three wolves, or a single one.

## Decision

There is one dire wolf rig, modelled at the yearling's proportions. Each growth stage carries a scale for the head, torso, legs and tail. The renderer turns those into one scale per bone, divided by the parent's so children never scale twice, and applies it around each bone's pivot on the matrix stack while the model is drawn. It then lifts the body so longer or shorter legs keep the paws on the ground, and shrinks the whole model so its height matches the stage's hitbox. Animations are authored once and every stage plays them.

Stage stats are attribute modifiers on top of the preset's base values, saved with the entity's attributes, so a reload keeps both the stage and the current health. A yearling and an adult grow on a timer, the adult more slowly. A prime never grows on its own: it becomes an alpha only after winning battles beside its owner and eating a rare item, so alphas are earned rather than waited for.

A breed whose spawn settings are marked pack only is left out of vanilla biome spawning. The mod's own pack spawner places it, and it places the whole pack or nothing. It still runs for those breeds when the cap-independent spawner option is off, because it is the only way they appear.

The rules that differ for the dire wolf (taming chance, breeding only with its own kind, staged growth, pack-only spawning) are data on the breed preset, not checks for the dire wolf by name.

## Consequences

Every stage shares one rig, one set of textures, one set of animations and one rig profile, so armour, pendants, fur and collars work at every size with no extra art. Slot scales are approximate: they stretch whole parts rather than reshaping them, and the ground and height fit is calculated from the rest pose, so an extreme pose at an extreme stage can sit slightly off the ground.

The stage ladder is specific to the dire wolf. Another breed that needs growth stages would need its own ladder, or this one made generic.

With pack-only spawning, dire wolves never come from chunk generation or the vanilla creature cap. How often they appear depends on the pack spawner's timing, and a pack despawns like any wild spawner pack once no player is near.
