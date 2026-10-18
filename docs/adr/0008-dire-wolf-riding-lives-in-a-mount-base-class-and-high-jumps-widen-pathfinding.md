# ADR 0008: Dire wolf riding lives in a mount base class, and high jumps widen pathfinding

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

Primes and alphas can be ridden. Vanilla drives a ridden mob through methods on the entity itself: who controls it, how the rider's input becomes movement, how fast it goes and where the rider sits. These have to be overrides, so they cannot live in a collaborator the way the dog's other features do. The dog entity was already close to the 700 line limit.

Primes and alphas also jump 1.5 and 2 blocks. A stronger jump alone does nothing for a mob that walks by itself: vanilla land pathfinding plans climbs of about one block and treats fences as walls, so it never asks the dog to make the jump.

## Decision

The mount overrides live in `DogMountEntity`, an abstract base class between `TameableEntity` and the dog. It also owns the two values the client needs for riding: the stamina fraction and whether sprinting is locked out. Each override hands straight to `DogRiding`, which holds the rules: who may ride, steering, the eased ride speed, the leap, the seat and stamina. The stamina arithmetic is a pure class, `DireWolfStamina`. This is the same layering vanilla uses for horses.

The rider's client moves the mount, as in vanilla. Sprinting is vanilla's own: the mount says it can carry a sprinting rider, so double-tapping forward or holding the sprint key sets the rider's sprint flag, and vanilla sends that flag to the server. The jump charges on the rider's client like a horse's and is applied there, since that side moves the mount. Stamina is worked out on the server and synced as a fraction, so the HUD bar is right even when the server's stamina settings differ from the client's.

The jump height is a value on each growth stage, applied through the jump strength attribute, so riding and walking use the same jump. A small mixin on `LandPathNodeMaker` raises the climb height it plans for, but only for a dog whose stage jumps high, and the dog's navigation is told it may cross fences when the stage clears them. Vanilla's own move control then makes the jump.

## Consequences

The dog entity stays under the line limit, and riding reads as one place in the code. The cost is one more level in the class hierarchy. Any breed could become rideable by giving it a rideable stage, but only the dire wolf has one.

The pathfinding mixin touches a vanilla class every mob uses. It only changes the result for a dog with a high jump, and the gametests check that adults, which keep the normal jump, still find no way up a 1.5 block step. Fall damage is unchanged, so a prime will still not plan a drop that would hurt it.
