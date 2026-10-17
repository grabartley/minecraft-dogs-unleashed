# ADR 0002: Pendants are placed from the collar texture and ward at target time

- **Status:** Accepted
- **Date:** 2026-10-17

## Context

Pendants hang a small charm on a dog's collar and change how mobs and the owner are treated. Two parts of that need a choice that is not obvious from the code.

The first is where the charm goes. Every breed has its own rig, with its own neck bones and proportions, and more breeds are planned. A hand-tuned offset per breed would drift every time a rig changes, and puppies scale differently from adults. What every breed already has is a `<breed>_collar.png` overlay, which is the collar as the artist painted it.

The second is how a ward keeps mobs away. Clearing a mob's target after it picks one leaves a window where the mob's own targeting goal picks the same player again on the next tick. A live spider or vindicator still landed hits in testing.

## Decision

The charm position is worked out from the collar texture. On the first frame a breed's model is drawn, every front-facing face of the baked model is walked, the collar overlay's painted texels are mapped back through each face's UVs to model space, and the lowest painted point on the throat is taken. The charm is then centred across the width of that face, so it sits on the middle of the neck even when the painted band is uneven. The result is cached per baked model.

Wards are checked when a mob takes a target. A `MobEntity.setTarget` hook refuses a target that is a warding dog, or the owner of one, when the mob is in the ward's family and within 16 blocks of the dog. A scan every 10 ticks still clears targets taken before the ward applied, and clears the attack target memory of brain-driven mobs, which do not go through `setTarget`.

Owner-facing effects, warding and vitality, are resolved per world tick from the dogs and players in that world. They are not ticked by each dog, so two dogs wearing the same pendant never stack.

## Consequences

A new breed gets a correctly placed charm as soon as it has a collar overlay, with no code change. The cost is that the collar texture is now load-bearing: a breed with no collar paint on a front-facing face shows no charm. A broken collar texture is logged as a warning, not an error.

The `setTarget` hook runs a small entity search each time any mob takes a non-null target. That happens when targets change, not every tick, so the cost is small, but it is paid by every mob in the world.

A warded mob also does not fight back when the owner or dog attacks it. That is accepted, because a ward that breaks the moment you swing at a mob would not feel like a ward.
