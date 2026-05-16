---
name: pr
description: Create a PR for the dogs-unleashed repo using a git worktree branched from latest main
---

## Workflow

1. `git fetch origin main` — get latest main
2. `git worktree add -b <branch-name> ../minecraft-dogs-unleashed-<branch-name> origin/main`
- Branch name: short kebab-case describing the change (e.g. `add-beagle-breed`)
3. `cd ../minecraft-dogs-unleashed-<branch-name>` — make all changes in the worktree
4. Stage: `git add <files>`
5. Format: `./gradlew spotlessApply`
6. Test: `./gradlew test`
7. Build: `./gradlew clean build`
8. Commit: `git commit -m "<type>: <short lowercase descriptive present tense message>"`
- Types: feat, fix, refactor, test, docs, chore
9. Push: `git push origin <branch-name>`
10. Create PR: `gh pr create --repo grabartley/minecraft-dogs-unleashed --base main --head <branch-name> --title "<title>" --body "<body>"`
	- Title: `<type>: <description>` (same style as commit message)
	- Body: one-liner summary, then "**What's included:**" with bullet points
11. After merge, clean up: `cd ../minecraft-dogs-unleashed && git worktree remove ../minecraft-dogs-unleashed-<branch-name>`

## Conventions

- Always branch from latest `main`, never from other branches
- Use a fresh worktree per PR — don't reuse worktrees across branches
- Worktree path: `../minecraft-dogs-unleashed-<branch-name>` (sibling directory)
- Branch names: kebab-case (e.g. `fix-mob-collision`, `add-beagle-breed`)
- Commit messages: `<type>: <lowercase description>`, no period at end
- Types: feat, fix, refactor, test, docs, chore
- PR descriptions: bullet points under "What's included:" header
- Pre-commit must complete successfully: format, test, build
- No emoji in commit messages or PR titles
