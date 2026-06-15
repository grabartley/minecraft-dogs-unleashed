---
name: build
description: Build or implement a feature for the Dogs Unleashed mod, optionally from a GitHub issue. Use when asked to build, implement, or ship scoped work and keep project board status updated.
---

# Build

## Critical Rules

1. Always tie build work to a GitHub issue.
2. Run the `worktree` skill first before any issue moves, coding, or validation.
3. Keep issue project status in sync during execution.
4. Any new gameplay code or behavior changes must include extensive unit tests in the same PR.
5. Unit tests MUST map to a single specific class. Test class name MUST match the class under test plus a "Test"
suffix (e.g. `BeagleCoatRolls.java` -> `BeagleCoatRollsTest.java`). A test that exercises `Foo` must be named
`FooTest`, never `BarRelatedThingTest`.
5a. If the change touches anything under `src/main/java/.../gametest/`, anything under
`src/main/resources/data/dogs-unleashed/gametest/`, or the `fabric-gametest` entrypoints in `fabric.mod.json`,
invoke the `gametest` skill BEFORE writing or editing code. The gametest framework has many sharp edges
(relative vs world coords, time-of-day drift, mock players that aren't ServerPlayerEntity, void floors in
`EMPTY_STRUCTURE`) and this rule prevents reintroducing already-fixed bugs.
6. Run the `pr` skill as part of build after validation passes.
7. Move issue to `QA testing` only after PR is opened and CI is running.
8. After PR creation and `QA testing` transition, always provide a detailed manual QA checklist to the developer.
9. If PR code changes after the PR is opened, check whether the PR description still matches the current branch state,
and update it if needed so it reflects the final state only.
10. Stop at `QA testing`, human performs final verification and moves to `Done`.

## Workflow

1. Run the `worktree` skill to create a fresh isolated branch worktree, then perform all implementation and validation
work inside that worktree.
2. Capture scope from the request.
3. If an issue number or URL is provided, read it first with gh:

- `gh issue view <number> --repo grabartley/minecraft-dogs-unleashed`
- Extract acceptance criteria, constraints, and references.

4. If no issue is provided, run the `create-issue` skill to create one before coding. Use the created issue as the
tracking artifact for all subsequent status moves.
5. Move the issue to `In progress`.
6. Implement the feature.
7. Run relevant automated tests and a local validation pass for changed behavior.
8. Run manual validation via `run-game-client` when gameplay behavior changes.
9. Invoke the `pr` skill for final checks, commit, push, and PR creation.
10. Wait for CI to start on the PR and report status.
11. Move issue to `QA testing` when the PR is ready for human verification.
12. Provide a detailed manual QA checklist that the developer can run step by step.
13. Every code change must also update any docs it invalidates. Audit `README.md`, in-repo docs under `docs/`, and the
	linked issue body before committing; ship doc edits in the same PR as the code change.

## Board Status Policy

- Use these exact status values from the Dogs Unleashed project board:
- `Backlog`: issue created, not started
- `Ready`: scoped and ready to start
- `In progress`: active implementation
- `QA testing`: implementation complete, awaiting human validation
- `Done`: human-only final move after QA signoff

- Required transitions for build flow:
- Start work: set to `In progress`
- After PR creation and QA handoff: set to `QA testing`
- Do not move to `Done` inside this skill

## Project Metadata

- Project number: `1` (`Minecraft Dogs Unleashed`)
- Project id: `PVT_kwHOAQYbq84BOF-N`
- Status field id: `PVTSSF_lAHOAQYbq84BOF-Nzg85WjI`
- `Backlog` option id: `f75ad846`
- `Ready` option id: `61e4505c`
- `In progress` option id: `47fc9ee4`
- `QA Testing` option id: `df73e18b`
- `Done` option id: `98236657`

## Related Skills

- `worktree`, required first step for isolated branch setup
- `create-issue`, used when build work starts without an existing GitHub issue
- `pr`, required for commit, push, and PR creation during build flow
- `run-game-client`, use for manual gameplay validation before QA handoff
- `gametest`, REQUIRED before any code under `src/main/java/.../gametest/`,
`src/main/resources/data/dogs-unleashed/gametest/`, or `fabric-gametest`
entrypoints in `fabric.mod.json` is added or modified. Encodes patterns
from a full audit of the suite and prevents the entire class of bugs
documented in PR #211, #214, #220, #221, #223.
