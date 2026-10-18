# Architecture Decision Records

These records capture **why** Dogs Unleashed is built the way it is. They are not specifications: the acceptance criteria for a specific piece of work live on the issue that owns it, and what the mod offers a player lives in [`../../README.md`](../../README.md).

A record is historical. When a decision changes, add a new record superseding the old one rather than editing it, so the reasoning behind the original choice is not lost.

Shared engineering standards across all the mods in this family live in [`../standards.md`](../standards.md).

| Record | Decision |
|---|---|
| [0001](0001-a-linter-forbids-comments-rather-than-a-formatter-stripping-them.md) | A linter forbids comments rather than a formatter stripping them |
| [0002](0002-pendants-are-placed-from-the-collar-texture-and-ward-at-target-time.md) | Pendants are placed from the collar texture and ward at target time |
| [0003](0003-dog-armour-is-painted-onto-each-breeds-own-torso-cubes.md) | Dog armour is painted onto each breed's own torso cubes |
| [0004](0004-the-pendant-charm-is-moved-clear-of-the-posed-body.md) | The pendant charm is moved clear of the posed body |
| [0005](0005-fur-is-hand-made-per-rig-and-named-in-its-rig-profile.md) | Fur is hand-made per rig and named in its rig profile |
| [0006](0006-shared-overlay-effects-are-painted-once-and-projected-onto-each-rig.md) | Shared overlay effects are painted once and projected onto each rig |
| [0007](0007-dire-wolves-grow-by-scaling-profile-slots-and-spawn-only-as-whole-packs.md) | Dire wolves grow by scaling profile slots and spawn only as whole packs |
| [0008](0008-dire-wolf-riding-lives-in-a-mount-base-class-and-high-jumps-widen-pathfinding.md) | Dire wolf riding lives in a mount base class, and high jumps widen pathfinding |
| [0009](0009-new-per-dog-state-is-a-component-not-an-entity-field.md) | New per-dog state is a component, not an entity field |

## Writing A New Record

Cover the context, the decision, and the consequences, including the drawbacks accepted. A record that lists only benefits is not a decision record, it is an advertisement.

Record reasoning that is not obvious from reading the code. If the next person would arrive at the same choice anyway, it does not need a record.
