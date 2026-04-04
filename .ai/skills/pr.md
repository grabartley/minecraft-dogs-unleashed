---
description: Create a pull request following project conventions with proper branching and checks
---

# Create PR Skill

This skill guides creating pull requests on the Dogs Unleashed repository following project conventions.

## Branch Creation Rules

**CRITICAL: Always follow these rules when creating a PR:**

1. **Always create a new branch for new PRs** - Never reuse existing branches or commit directly to main/dev
2. **Always create new branches from up-to-date main** - Before creating a branch, ensure you're on main and it's synced with origin

```bash
# Proper branch creation workflow
git checkout main
git pull origin main
git checkout -b feat/your-feature-name
```

## Pre-PR Checklist

Before creating a PR, ensure the following steps are completed:

### 1. Code Quality

```bash
# Apply code formatting (Spotless)
./gradlew spotlessApply

# Run all checks (lint + tests)
./gradlew check
```

**Important**: All linting must pass before creating a PR. The CI/CD pipeline will fail if code is not properly formatted.

### 2. Run Tests

```bash
# Unit tests
./gradlew test

# Game tests (headless)
./gradlew runGametestServer
```

See `.ai/skills/run-tests.md` for comprehensive testing documentation.

### 3. Verify Build

```bash
./gradlew clean build
```

## Commit Message Convention

This project uses **Conventional Commits**. All commit messages must follow this format:

```
<type>: <description>

[optional body]

[optional footer(s)]
```

### Commit Types

| Type | Description | Example |
|------|-------------|---------|
| `feat` | New feature | `feat: Add Labrador breed` |
| `fix` | Bug fix | `fix: Correct collar rendering for baby dogs` |
| `docs` | Documentation only | `docs: Update README with new breeds` |
| `style` | Code style (formatting, no logic change) | `style: Apply Spotless formatting` |
| `refactor` | Code refactoring | `refactor: Extract common entity logic to base class` |
| `test` | Adding or fixing tests | `test: Add game tests for new breed` |
| `chore` | Maintenance tasks | `chore: Update dependencies` |
| `cicd` | CI/CD changes | `cicd: Update GitHub Actions workflow` |

### Examples

```bash
# Feature commit
git commit -m "feat: Add Golden Retriever breed"

# Bug fix
git commit -m "fix: Resolve null pointer in collar rendering"

# Multi-line commit with body
git commit -m "feat: Add dog bed item

Adds a new dog bed item that dogs can sleep on.
Includes crafting recipe and placement logic."
```

## Branch Strategy

### Branch Naming

Use descriptive branch names with the type prefix:

```bash
# Feature branches
git checkout -b feat/add-golden-retriever
git checkout -b feat/dog-bed-item

# Bug fix branches
git checkout -b fix/collar-rendering

# Refactor branches
git checkout -b refactor/entity-base-class
```

### Keeping Branch Up to Date

**Important**: Once a PR has been created, use `git merge` instead of `git rebase` to incorporate upstream changes. This preserves commit history and avoids force-push issues.

```bash
# Pull latest changes from main
git fetch origin
git merge origin/main

# Do NOT use rebase after PR creation
# git rebase origin/main  # AVOID THIS
```

### Before PR Creation (rebasing is OK)

```bash
# Before creating PR, you can rebase to clean up history
git fetch origin
git rebase origin/main
```

### After PR Creation (merge only)

```bash
# Use merge to incorporate new changes
git fetch origin
git merge origin/main

# Resolve any conflicts
# git add .
# git commit -m "chore: Merge main branch"
```

## PR Description Template

When creating a PR, use this template:

```markdown
## Summary

Brief description of what this PR does.

## Changes

- List of specific changes made
- Another change
- Another change

## Testing

- [ ] Unit tests pass (`./gradlew test`)
- [ ] Game tests pass (`./gradlew runGametestServer`)
- [ ] Linting passes (`./gradlew check`)
- [ ] Manually tested in-game

## Screenshots/Videos (if applicable)

Include any relevant screenshots or videos showing the feature/fix.

## Related Issues

Closes #XX (if applicable)
```

## Creating the PR

### Using GitHub CLI

```bash
# Create PR targeting main branch
gh pr create --base main --title "feat: Add {BreedName} breed" --body-file pr-body.md

# Or with inline body
gh pr create --base main --title "feat: Add Golden Retriever breed" --body "## Summary

Adds the Golden Retriever as a new dog breed.

## Changes

- Added GoldenRetrieverEntity class
- Added model, renderer, and collar layer
- Added spawn configuration for beach biomes
- Added unit and game tests

## Testing

- [x] Unit tests pass
- [x] Game tests pass
- [x] Linting passes
- [x] Manually tested in-game"
```

### Draft PRs

For work-in-progress, create a draft PR:

```bash
gh pr create --base main --title "feat: Add Golden Retriever breed" --draft
```

## PR Title Convention

PR titles should follow the same conventional commit format:

- `feat: Add Golden Retriever breed`
- `fix: Correct collar color persistence`
- `refactor: Extract animation controller to base class`
- `cicd: Update build workflow for game tests`

## Review Process

1. Ensure all CI checks pass
2. Request review from maintainers
3. Address any feedback with additional commits
4. Once approved, squash and merge (or merge commit based on preference)

## Common Issues

### Linting Failures

```bash
# Fix formatting issues
./gradlew spotlessApply
git add -A
git commit -m "style: Apply Spotless formatting"
```

### Test Failures

```bash
# Check test output
./gradlew test --info

# For game tests
./gradlew runGametestServer --info
```

### Merge Conflicts

```bash
# Fetch latest
git fetch origin

# Merge (preferred after PR creation)
git merge origin/main

# Resolve conflicts in files
# ... edit files ...

# Mark as resolved
git add <resolved-files>
git commit -m "chore: Resolve merge conflicts with main"
```

## Quick Reference Commands

```bash
# Start new feature (ALWAYS do this for new PRs)
git checkout main && git pull origin main && git checkout -b feat/your-feature

# Format code
./gradlew spotlessApply

# Run all checks
./gradlew check

# Run unit tests
./gradlew test

# Run game tests
./gradlew runGametestServer

# Build
./gradlew clean build

# Create PR
gh pr create --base main --title "type: description"

# Create draft PR
gh pr create --base main --title "type: description" --draft

# View PR status
gh pr status

# Merge latest changes (after PR creation)
git fetch origin && git merge origin/main
```
