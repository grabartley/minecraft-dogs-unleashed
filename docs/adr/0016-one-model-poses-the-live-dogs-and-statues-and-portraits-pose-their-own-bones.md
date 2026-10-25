# ADR 0016: One model poses the live dogs, and statues and portraits pose their own bones

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

GeckoLib keeps one set of bones per model file, and every renderer that loads that file through `GeckoLibCache` gets the same bones. A `GeoModel` poses those bones for each animatable just before it is drawn. To save work, it skips posing an animatable it has already posed in the same frame, and it remembers that per `GeoModel` object, not per set of bones.

Many things in this mod draw the same rig. A pure golden retriever, a cross breed standing on the golden rig, an undead golden, a golden statue and a golden portrait all use the golden bones. Each dog type used to register its renderer with its own `DogModel`, and statues and portraits used their own models too. So one set of bones was posed by several `GeoModel` objects, and none of them knew what the others had done.

That is harmless while each entity is drawn once a frame. Shader packs draw every entity twice a frame, once for shadows and once for the picture. A cross breed was posed in the shadow pass, a sitting pure golden or a statue posed the shared bones after it, and in the main pass GeckoLib skipped the cross because its own model had already posed it that frame. The cross was drawn in the golden's sitting pose. It looked like it slid along sitting and never showed its walk. Without shaders nothing went wrong, so it only showed up for players running a shader pack.

## Decision

Every set of bones has exactly one `GeoModel` that poses it.

All live dogs share one `DogModel`. That covers every pure breed, every cross breed and every undead dog. The client creates it once and passes it to the renderer of every dog entity type through `DogRenderer.sharing`, from one list in the client setup. GeckoLib's own same-frame skip then sees every dog that poses the shared bones, and works as it was designed to.

Statues and portraits hold a fixed pose, so they never touch the live bones. `PosedDogModel`, which both of them extend, takes its bones from `PrivateRigBones`. That is a private copy of each rig, built from the same model file and rebuilt when resources reload.

Caches keyed by a rig's bones (`GrowthRigScale`, `RigSlotSizes`, the layer caches) compare models by identity, so a private copy never shares an entry with the live bones it was built from.

## Consequences

A new dog entity type must be added to the shared list in the client setup. Registering it with its own `DogModel` would bring the bug back for that type the moment a player turns on shaders. A new thing that draws a dog rig in a fixed pose should extend `PosedDogModel`, so it poses its own copy.

`GeoModel` keeps one animation clock, and it is now shared by every dog type, not just every dog of one type. Controllers time clips from their own start, so nothing changes in practice. With several rigs on screen, GeckoLib swaps the active bone set more often, which is a small cost.

Statues and portraits hold an extra copy of each rig they draw. The copy is built the first time a rig is drawn as a statue or portrait after a reload, which reads one small model file.

This only uses GeckoLib's public behaviour: one model poses one set of bones, and `getBakedModel` decides which bones a model poses. It does not depend on how GeckoLib's same-frame skip works inside, so a GeckoLib update should not undo it.
