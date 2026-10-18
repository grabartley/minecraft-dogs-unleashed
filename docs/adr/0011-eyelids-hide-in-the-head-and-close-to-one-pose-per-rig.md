# ADR 0011: Eyelids hide in the head and close to one pose per rig

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

The dire wolf blinks while it stands idle. The other five breeds could not, because their eyelids were built a different way. Their lid bones sat at the root of the rig instead of under the head, so they did not move with it. Each clip hid them with its own position and scale values, and `sleep` swung them over the eyes with a rotation that only matched the head's sleeping pose. A blink would have needed a new set of numbers in every clip, and the lids would have slid off the face whenever the head moved during one. A few clips also missed: on `main`, the dachshund's shake and the golden retriever's sit leave a lid floating beside the head.

Any later clip that closes the eyes, such as a wink, closed eyes while being petted, or a drowsy look, would have hit the same problem.

## Decision

Every rig sets up its eyelids the same way:

- The lid bones are children of the head and are listed under `eyes` in the rig profile. Paired lids are `eyelid_left` and `eyelid_right`. A rig whose two lids are one cube calls it `eyelids`.
- At rest the lids sit inside the head, so the eyes are open. A clip that does not close the eyes needs no lid keys.
- Closed is one pose per rig: a position offset, plus a scale where a lid is parked smaller than it shows. Every clip that closes the eyes uses that pose. `sleep` holds it and `idle` blinks to it once a loop. A clip may still pose the lids some other way on purpose, as the dire wolf's `growl` tilts them into a scowl.

The five older rigs were moved onto this setup without changing how they look asleep. Each closed pose was worked out from the old sleep poses with GeckoLib's own bone transforms, so the closed lids land where they did before, with the same texels. Lids that fit inside the head are parked at its centre. The beagle's lid is as big as its head, so it is parked at half size and scaled back up to close.

## Consequences

Any clip can close the eyes by keying the lids to the rig's closed pose, with no per-clip parking values. `RigProfilesTest` fails when a lid is not a head child, pokes out of the head at rest, or blinks to a different pose than it sleeps in. `UndeadDogEyesTest` fails when the closed lids miss the painted pupils.

Short idle loops were repeated whole to about four seconds so a blink comes once every few seconds rather than every second. The rest of the idle motion is unchanged.

The beagle and dachshund keep a single cube for both lids, because splitting it would need new texture space on every coat. Those two rigs cannot wink one eye until that art exists.

The beagle and dachshund closed poses include a scale, so their blink grows the lid out of the head over a tick rather than only sliding it. At blink speed this cannot be seen, but a slow eye close on those two rigs would show it.
