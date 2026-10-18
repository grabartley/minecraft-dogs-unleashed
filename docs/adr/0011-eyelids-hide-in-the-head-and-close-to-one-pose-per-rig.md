# ADR 0011: Eyelids hide in the head and close to one pose per rig

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

The dire wolf blinks while it stands idle. The other five breeds could not, because their eyelids were built a different way. Their lid bones sat at the root of the rig instead of under the head, so they did not move with it. Each clip hid them with its own position and scale values, and `sleep` swung them over the eyes with a rotation that only matched the head's sleeping pose. A blink would have needed a new set of numbers in every clip, and the lids would have slid off the face whenever the head moved during one. A few clips also missed: on `main`, the dachshund's shake and the golden retriever's sit leave a lid floating beside the head.

Any later clip that closes the eyes, such as a wink, closed eyes while being petted, or a drowsy look, would have hit the same problem.

## Decision

Every rig sets up its eyelids the same way:

- Each eye has its own lid, `eyelid_left` and `eyelid_right`. Both are children of the head and are listed under `eyes` in the rig profile.
- At rest the lids sit inside the head, so the eyes are open. A clip that does not close the eyes needs no lid keys.
- Closed is one pose per rig: a position offset, plus a scale where a lid is parked at a different size than it shows. Every clip that closes the eyes uses that pose. `sleep` holds it and `idle` blinks to it once a loop. A clip may still pose the lids some other way on purpose, as the dire wolf's `growl` tilts them into a scowl.
- A closed lid stays inside the head everywhere except its front, which lies on the face over its eye. It may touch the edge of the head but never reach past it.
- Closing changes only the eyes. On every coat the closed lid covers both painted pupils, and wherever it differs from the face behind it, that texel is within two texels of a pupil. Each coat paints its own closed eye on the lid.

The beagle and dachshund used to close with one face-sized lid whose art repainted the whole face, so a blink changed the forehead and the top and sides of the head. They now have a small lid per eye, lined up with the head's texel grid and painted with each coat's own closed-eye texels from the old art. The golden retriever and shiba inu lids poked past the top and side of the head when closed, and now stop at its edge. The dire wolf already met every rule.

## Consequences

Any clip can close the eyes by keying the lids to the rig's closed pose, with no per-clip parking values, and a wink only needs to key one lid. `RigProfilesTest` fails when a lid is not a head child, pokes out of the head at rest, closes past the head anywhere but the front, or blinks to a different pose than it sleeps in. `UndeadDogEyesTest` fails when, on any coat, the closed lids miss a pupil or change anything but the eyes.

Short idle loops were repeated whole to about four seconds so a blink comes once every few seconds rather than every second. The rest of the idle motion is unchanged.

Sleeping beagles, dachshunds, golden retrievers and shiba inus look slightly different from before: only their eyes are closed, and the rest of the face keeps its own fur.

The beagle and dachshund lids are parked larger than they show and shrink as they close. At blink speed this cannot be seen, but a slow eye close on those two rigs would show it.
