# ADR 0005: Fur is hand-made per rig and named in its rig profile

- **Status:** Accepted
- **Date:** 2026-10-17

## Context

Fur growth needs two visible changes on five breeds whose rigs share nothing: a fuller silhouette as the coat grows, and a clipped look after a trim. ADR 0003 drew armour as a slightly larger copy of each breed's torso with a texture painted at load time, and the Dog Rig Profiles plan proposed generating fur shells the same way.

We built and tried that for fur. The generated looks went through four rounds: inflated copies with holes punched in them, a solid wool-like shell, a shell sculpted to grow more at the belly, legs and tail, and small generated tufts hanging off each cube. Each round looked better than the last, but none reached the quality the mod needs. Fur reads through its silhouette, and the rigs are built from cubes with large negative inflation and their own rotations, so generated geometry pointed the wrong way on necks and tails, formed ledges under the belly and turned tails into stacks of slabs. The only fur in the repository that looks right is the golden retriever's chest, which an artist built by hand from small stepped cubes.

Armour, pendants and fur also each kept their own table of bone names and cube numbers in Java. Every new feature added another table, and nothing failed when a rig changed.

## Decision

Fur is drawn by hand on each breed's own rig. The code only shows and hides it.

Each rig has a profile at `assets/dogs-unleashed/rig_profiles/<rig>.json`. It names that rig's torso and mouth bones, the bones of its shaggy and overgrown fur, and its trimmed and scuffed overlay textures. A profile only lists what its rig has, so the fur and overlay entries arrive with each breed's art. Code reads only the profile, so no bone names or cube numbers live in Java. Small detail cubes, such as paws and the golden's chest tufts, are told apart from body cubes by their size rather than by listing them.

Fur bones are extra cubes in the rig, parented to its existing bones so they follow every animation. The renderer skips fur bones whose stage the dog has not reached, per dog and per frame, without changing the shared baked model. The trimmed and scuffed looks are translucent overlays drawn over the coat.

Armour, pendants and carried items moved onto the same profile in this change. Armour takes its torso from the profile and was checked to pick exactly the same cubes as the old table on every rig. Pendants and carried items take the mouth from it.

## Consequences

Fur looks as good as the artist can make it, and a new breed gets fur by adding bones to its rig and listing them in its profile, with no code change. A test fails if a profile names a bone the rig does not have or an overlay that does not ship.

The cost is art time: shaggy and overgrown fur bones and two overlays for each of five breeds. Until a breed's art lands, its fur stages work in game (growth, trimming, the speed penalty, the drops and the advancement) but look like the natural coat.

Profiles are data the build does not compile, so a typo in a bone name is only caught by the profile test, not by the compiler.
