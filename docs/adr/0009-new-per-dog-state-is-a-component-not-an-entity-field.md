# ADR 0009: New per-dog state is a component, not an entity field

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

`UnleashedDogEntity` reached 698 lines after the dire wolf work, against a 700 line limit. Most of it was not behaviour. Each feature added a collaborator field, a public getter for it, and often a pair of package-private accessors that only forwarded to the data tracker. Every planned feature in the next epics needs per-dog state that ticks and saves, so each one would have grown the entity again.

The tracked data constants cannot leave the entity. `DataTracker.registerData` numbers ids after the superclass chain, and a holder class can be initialised before those superclasses, handing out ids that overflow the tracker.

## Decision

New per-dog state is a `DogComponent`: a class with package-private `tick`, `writeNbt` and `readNbt` hooks. The entity owns one `DogComponents` holder, created with every component in a fixed order. The server tick calls `tick` on all of them in that order, and `DogEntityNbt` calls `writeNbt` and `readNbt` at one point in the save. Callers reach a component by type, for example `dog.getComponents().get(DogCollar.class)`.

The treat buff, the collar colour and the spawner origin flag are the first components. Collaborators that only read or write tracked data use `dog.getDataTracker()` with the entity's package-private constants instead of forwarding methods on the entity. The bed and sleep accessors live on `DogSleepController`, and the play partner lives on `DogPlaySession`.

## Consequences

A new feature adds a class and one entry in the holder instead of a field, a getter and accessors on the entity. Save keys are unchanged, so worlds saved before the split load as they were.

Looking a component up by type is more verbose than a named getter, and a missing component is only found at runtime. The holder fails loudly when a component is missing or added twice, and the gametests reach every registered component.

The order components are created in is the order they tick, save and load, so a component whose load has side effects must not depend on state that `DogEntityNbt` reads after it. Components must live in the `entity` package: the hooks are package-private and the base class cannot be extended from outside it, so a component elsewhere fails to compile rather than silently never ticking or saving. The tracked data constants still live in the entity, so a component that syncs to the client still adds one line there.
