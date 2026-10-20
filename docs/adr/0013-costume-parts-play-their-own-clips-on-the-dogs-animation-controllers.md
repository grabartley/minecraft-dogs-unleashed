# ADR 0013: Costume parts play their own clips on the dog's animation controllers

- **Status:** Accepted
- **Date:** 2026-10-20

## Context

Some costume parts move: a bee's wings buzz while the dog runs, and later costumes flap wings when the dog jumps or pulse patches when it barks. ADR 0012 attaches each part to a dog bone, so a part already follows that bone, which is enough for a spider's legs anchored on the dog's legs. It is not enough for a wing on the torso, which has to move on its own.

GeckoLib only animates bones that are in the model it is rendering. Keyframes for `costume_` bones added to each rig's animation file would be ignored, because the parts are a separate model. Writing them into every rig's file would also mean redoing every costume for every new breed.

## Decision

A costume's parts may have an animation file at `<namespace>:animations/costume/<name>.animation.json`. Its clips are named after the dog's own clips, such as `walk`, `run`, `sit` or `sleep`.

The parts model is animated with a second set of the dog's own animation controllers. The renderer keeps one animation manager per dog and costume, built from the dog's controller registration, in a map that holds the dog weakly. It cannot use the dog's own animation cache, because GeckoLib gives an entity a single manager whatever instance id is asked for, and sharing it would let the costume steal the dog's animation updates. Whatever clip a controller picks for the dog, the same controller in the costume's manager picks the same clip and plays it from the costume's file on the parts. A clip the costume does not have is skipped, so a costume only animates what it needs. A costume with no animation file is not animated beyond following its anchor bones.

## Consequences

An artist animates a costume once and it plays on every rig, including breeds added later, in step with the dog. Parts stay in sync with the dog's state because the same predicates choose both clips.

Triggered clips, such as the bark, reach only the dog's own manager, so a costume cannot react to them yet. A bark-driven part needs the trigger forwarded to the costume's manager as well. Each costume a dog has worn keeps a small animation state in the renderer until the dog is garbage collected.
