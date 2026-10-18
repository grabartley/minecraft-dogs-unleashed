# ADR 0004: The pendant charm is moved clear of the posed body

- **Status:** Accepted
- **Date:** 2026-10-17

## Context

[ADR 0002](0002-pendants-are-placed-from-the-collar-texture-and-ward-at-target-time.md) places the charm on the collar using the model's rest pose, and the charm then moves with the collar bone. That is right while the dog stands. When the beagle, shiba inu and golden retriever sit, their sit animation swings the torso up over the throat, so the front of the collar and the charm end up inside the chest. The collar is hidden too, so there is no visible collar point to move the charm to.

Fixing the animations would change how those dogs sit. A per-breed offset would drift with every rig or animation change, which is what ADR 0002 set out to avoid.

## Decision

Each frame, after the animation has posed the bones, the charm is checked against the dog's own posed geometry. If the middle of the charm's front face is inside a body cube, the charm is moved straight forward until it is clear.

Which cubes count as body is decided once per model, from the rest pose: a bone whose centre is on the far side of the collar from the mouth is body, and everything else is head. Only body cubes push the charm. If a push would land the charm inside the head, for example under the chin of a sleeping dog, the charm stays where the collar put it.

Armour is a hard plate, so it is checked differently. When the dog wears armour, the charm is also checked against the armour shell, which is each armoured torso cube grown by the same amount the armour is drawn with. Here the back of the charm must be clear, so the whole charm sits on the front of the armour. The collar's own bone counts for this check too, since the armour covers it. The charm moves by whichever push is larger.

## Consequences

The charm stays visible on a sitting dog of any breed with no breed-specific code, and any new breed or animation gets the same treatment. Poses where the charm was already visible are left exactly as they were. A dog in armour shows the whole charm on the front of the armour instead of half of it sunk into the plate.

The cost is a small amount of work every frame for each dog wearing a pendant: rebuilding the body and head boxes from the posed bones. Dogs without a pendant pay nothing.

On a sitting dog the charm sits in front of the chest rather than on a visible collar, since the collar is hidden there. A charm that would only clear by passing through the head stays hidden, which matches how the collar itself looks in that pose.
