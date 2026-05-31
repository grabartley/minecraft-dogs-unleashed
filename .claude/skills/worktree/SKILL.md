---
name: worktree
description: Set up a fresh git worktree from latest main for isolated implementation work. Use before build or PR workflows.
---

# Worktree

Use this skill first when starting implementation work.

## Workflow

1. `git fetch origin main`.
2. Pick a short kebab-case branch name, for example `add-beagle-bed-animation`.
3. Create worktree: `git worktree add -b <branch-name> ./.claude/worktrees/dogs-unleashed-<branch-name> origin/main`.
4. Create the worktree run directory if needed: `mkdir -p ./.claude/worktrees/dogs-unleashed-<branch-name>/run`.
5. Copy run config required for manual verification:
- `cp -a run/saves ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/world ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/options.txt ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/server.properties ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/eula.txt ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/config ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
- `cp -a run/usercache.json ./.claude/worktrees/dogs-unleashed-<branch-name>/run/`
6. Perform all coding, validation, commit, and PR steps from inside `./.claude/worktrees/dogs-unleashed-<branch-name>`.

## Conventions

- Always branch from latest `main`.
- Use a fresh worktree per branch, do not reuse active worktrees.
- Worktree path format: `./.claude/worktrees/dogs-unleashed-<branch-name>`.
- Keep branch names concise and kebab-case.
