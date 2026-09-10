# Architecture Decision Records

These records capture **why** Dogs Unleashed is built the way it is. They are not specifications: the acceptance criteria for a specific piece of work live on the issue that owns it, and what the mod offers a player lives in [`../../README.md`](../../README.md).

A record is historical. When a decision changes, add a new record superseding the old one rather than editing it, so the reasoning behind the original choice is not lost.

Shared engineering standards across all the mods in this family live in [`../standards.md`](../standards.md).

| Record | Decision |
|---|---|
| [0001](0001-a-linter-forbids-comments-rather-than-a-formatter-stripping-them.md) | A linter forbids comments rather than a formatter stripping them |

## Writing A New Record

Cover the context, the decision, and the consequences, including the drawbacks accepted. A record that lists only benefits is not a decision record, it is an advertisement.

Record reasoning that is not obvious from reading the code. If the next person would arrive at the same choice anyway, it does not need a record.
