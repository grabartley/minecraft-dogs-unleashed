---
description: Add a new feature to the Dogs Unleashed Minecraft mod following project conventions
---

# Add Feature Skill

This skill is the **primary entrypoint** for adding any new feature to the Dogs Unleashed mod. It provides the workflow and references specialized skills for specific feature types.

## Feature Development Workflow

### 1. Create a New Branch

**CRITICAL**: Always start from an up-to-date main branch.

```bash
git checkout main
git pull origin main
git checkout -b feat/your-feature-name
```

Branch naming conventions:
- `feat/` - New features (e.g., `feat/pet-tracking-system`)
- `fix/` - Bug fixes (e.g., `fix/collar-rendering`)
- `refactor/` - Code refactoring (e.g., `refactor/entity-base-class`)

### 2. Implement the Feature

Make your code changes following the project's architecture:

- **Server/Common code**: `src/main/java/com/grahambartley/`
- **Client-only code**: `src/client/java/com/grahambartley/`
- **Resources**: `src/main/resources/` and `src/client/resources/`

**Important Architecture Rules**:
- Never put rendering code in `src/main` - it will crash dedicated servers
- Never put entity logic in `src/client` - it won't sync properly
- Use GeckoLib for all animated entities

### 3. Run Automated Tests

Before any manual testing, ensure automated tests pass.

**Java 21 is required.** Use your preferred version manager:

**jenv:**
```bash
jenv local 21
```

**SDKMAN:**
```bash
sdk use java 21-amzn
```

**Manual JAVA_HOME:**
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home
```

Then run the tests:

```bash
# Apply code formatting
./gradlew spotlessApply

# Run unit tests
./gradlew test

# Run game tests (headless)
./gradlew runGametestServer

# Verify build
./gradlew clean build
```

### 4. Manual Testing (REQUIRED)

**CRITICAL**: Before pushing any change that impacts functionality, you MUST run the game client and verify the changes work correctly.

Use the **run-game-client** skill to:
1. Launch the Minecraft client with the mod loaded
2. Manually test all affected features
3. Verify visual elements render correctly
4. Test user interactions work as expected
5. **Wait for developer approval** before proceeding

**Java 21 is required.** Use your preferred version manager:

**jenv:**
```bash
jenv local 21
./gradlew runClient
```

**SDKMAN:**
```bash
sdk use java 21-amzn
./gradlew runClient
```

**Manual JAVA_HOME:**
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-21.jdk/Contents/Home
./gradlew runClient
```

See `.ai/skills/run-game-client.md` for full manual testing details.

### 5. Commit and Push

Only after receiving developer approval from manual testing.

This project uses **[Conventional Commits](https://www.conventionalcommits.org/)** for all commit messages:

```
<type>: <description>

[optional body]
```

**Commit Types:**
| Type | Description | Example |
|------|-------------|---------|
| `feat` | New feature | `feat: add pet tracking system` |
| `fix` | Bug fix | `fix: correct collar rendering for baby dogs` |
| `docs` | Documentation only | `docs: update README with new breeds` |
| `style` | Code style (formatting) | `style: apply Spotless formatting` |
| `refactor` | Code refactoring | `refactor: extract common entity logic` |
| `test` | Adding or fixing tests | `test: add game tests for new breed` |
| `chore` | Maintenance tasks | `chore: update dependencies` |

```bash
# Stage changes
git add -A

# Commit with conventional commit message
git commit -m "feat: your feature description"

# Push to remote
git push -u origin feat/your-feature-name
```

### 6. Create Pull Request

Use the **create-pr** skill to create a properly formatted PR:

```bash
gh pr create --base main --title "feat: Your Feature Title" --body "Description"
```

See `.ai/skills/create-pr.md` for full PR creation details.

## Specialized Skills Reference

### Adding a New Dog Breed

If your feature involves adding a new dog breed, use the **add-dog-breed** skill instead of implementing from scratch.

See `.ai/skills/add-dog-breed.md` for:
- Entity class creation
- Model and renderer setup
- Spawn configuration
- Required game tests
- Asset file requirements

### Creating Pull Requests

Always use the **create-pr** skill when ready to submit your changes.

See `.ai/skills/create-pr.md` for:
- Branch naming conventions
- Commit message format
- PR description template
- Review process

### Manual Testing

Always use the **run-game-client** skill before pushing functionality changes.

See `.ai/skills/run-game-client.md` for:
- Java version setup
- Common testing tasks
- Hot reloading tips
- Troubleshooting

## Pre-Push Checklist

Before pushing ANY change that affects functionality:

- [ ] `./gradlew spotlessApply` - Code is formatted
- [ ] `./gradlew test` - Unit tests pass
- [ ] `./gradlew runGametestServer` - Game tests pass
- [ ] `./gradlew clean build` - Build succeeds
- [ ] **Manual testing completed** - Ran game client via run-game-client skill
- [ ] **Developer approval received** - Showed changes to developer and got blessing

## Feature Types Quick Reference

| Feature Type | Primary Skill | Additional Skills |
|--------------|---------------|-------------------|
| New dog breed | add-dog-breed | create-pr, run-game-client |
| New item | add-feature | create-pr, run-game-client |
| New GUI/screen | add-feature | create-pr, run-game-client |
| Bug fix | add-feature | create-pr, run-game-client |
| Refactoring | add-feature | create-pr |

## Common Patterns

### Adding New Networking

For client-server communication:

1. Define payload records in `ModNetworking.java` (server-side)
2. Register payloads and server receivers in `ModNetworking.java`
3. Create `ModNetworkingClient.java` for client receivers
4. Register client receivers in `DogsUnleashedClient.java`

### Adding New GUI Screens

For new user interfaces:

1. Create screen class in `src/client/java/.../screen/`
2. Add keybinding in `ModKeyBindings.java` if needed
3. Register keybinding in `DogsUnleashedClient.java`
4. Add language entries for UI text

### Adding Persistent Data

For data that persists across game sessions:

1. Use `PersistentState` for world-level data
2. Use NBT for entity-level data
3. Store in `src/main/` (server-side)

## Troubleshooting

### Build Fails
```bash
./gradlew clean build --info
```

### Tests Fail
```bash
./gradlew test --info
./gradlew runGametestServer --info
```

### Client Crashes
1. Check for client/server code separation issues
2. Verify all assets exist at correct paths
3. Check console output for stack traces

### Entity Not Visible
1. Verify renderer registration in `DogsUnleashedClient.java`
2. Check asset paths in model class
3. Press F3+T in-game to reload textures
