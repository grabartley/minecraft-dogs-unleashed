---
description: Run and write tests for the Dogs Unleashed mod (unit tests, game tests, manual testing)
tags:
- testing
- junit
- gametest
- ci-cd
---

# Run Tests Skill

This skill covers all testing aspects for the Dogs Unleashed mod, including unit tests, game tests, and manual testing workflows.

## Testing Philosophy

Since Minecraft mod code requires runtime initialization, testing strategy focuses on:
- **Configuration validation**: Testing that values are within valid ranges
- **Constant verification**: Ensuring mod IDs, paths, and identifiers are correct
- **Logic testing**: Validating calculations and utility methods with full coverage
- **Game tests**: Testing entity behavior, spawning, AI, and interactions in actual game environment
- **Manual testing**: Verifying visual elements and user interactions work correctly in-game

## Test Types Overview

| Test Type | Location | Purpose | Run Command |
|-----------|----------|---------|-------------|
| Unit Tests (JUnit 5) | `src/test/java/` | Logic, config validation | `./gradlew test` |
| Game Tests | `src/main/java/.../gametest/` | Entity behavior, spawning | `./gradlew runGametestServer` |
| Manual Tests | In-game client | Visual, interaction testing | `./gradlew runClient` |

## Unit Tests (JUnit 5)

### Running Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run with code coverage
./gradlew check

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Writing Unit Tests

**Testing configuration values:**
```java
@Test
@DisplayName("Husky health should be higher than vanilla wolf")
void testHuskyHealth() {
	double huskyHealth = 25.0;
	double wolfHealth = 20.0;
	assertTrue(huskyHealth > wolfHealth);
}
```

**Using Mockito for dependencies:**
```java
@Test
@DisplayName("Method should handle null input")
void testNullInput(@Mock SomeClass mock) {
	when(mock.getValue()).thenReturn(null);
	assertThrows(IllegalArgumentException.class, () -> {
		// test code
	});
}
```

### Test Strategy Notes

- **Cannot instantiate Minecraft entities** - Tests cannot use game classes requiring runtime context
- **Use value assertions** - Test configuration values instead of object instantiation
- **Mock dependencies** - Use Mockito when testing utility methods
- **Avoid registry triggers** - Don't trigger Minecraft's registry system in tests

### Example Test Classes

- `HuskyEntityTest.java` - Testing entity attribute configuration values
- `HuskyModelTest.java` - Testing resource path formats
- `HuskyRendererTest.java` - Testing renderer configuration values
- `EntityUtilsTest.java` - Testing utility methods with Mockito mocks
- `DogsUnleashedTest.java` - Testing mod constants and identifiers

### Code Coverage

- **Coverage reporting** - JaCoCo generates reports for informational purposes
- **No minimum threshold** - Most Minecraft code requires game runtime
- **Focus areas** - Tests focus on testable logic like utilities and configuration
- **HTML Report**: `build/reports/jacoco/test/html/index.html`
- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml`

Coverage is a tool for visibility, not a strict gate. The build focuses on:
- Testing what can be tested (utilities, helper methods)
- Validating configuration values are sensible
- Ensuring code formatting and test execution pass
- Manual in-game testing for Minecraft-dependent features

## Game Tests

Game tests run in an actual Minecraft server environment to test entity behavior, spawning, AI, and interactions.

### Running Game Tests

**Locally (Interactive):**
```bash
./gradlew runGametest
```
This starts a Minecraft server with game tests enabled. The server will run tests when triggered.

**Locally (Headless for CI/CD testing):**
```bash
./gradlew runGametestServer
```
This runs in headless mode (`--nogui`) suitable for automated testing.

**In CI/CD:**
Game tests run automatically in GitHub Actions on every push to main and on pull requests.

The workflow runs: `./gradlew runGametestServer --no-daemon --stacktrace`

Results are published to `build/gametest-results.xml`

### Writing Game Tests

1. Create test class in `src/main/java/com/grahambartley/gametest/`
2. Implement `FabricGameTest` interface
3. Add methods annotated with `@GameTest`
4. Register in `fabric.mod.json` under `fabric-gametest` entrypoint
5. Use `TestContext` for assertions

**Example:**

```java
public final class MyEntityGameTest implements FabricGameTest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void entitySpawns(final TestContext context) {
		final BlockPos spawnPos = new BlockPos(0, 1, 0);
		final ServerWorld world = context.getWorld();

		final MyEntity entity = ModEntities.MY_ENTITY.create(world);
		if (entity == null) {
			context.throwPositionedException("Failed to create entity", spawnPos);
			return;
		}

		entity.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
		world.spawnEntity(entity);

		context.expectEntityAt(ModEntities.MY_ENTITY, spawnPos);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
	public void entityBehaviorOverTime(final TestContext context) {
		// Spawn entity...

		context.runAtTick(20, () -> {
			// Verify behavior after 1 second (20 ticks)
			context.assertTrue(someCondition, "Expected behavior");
			context.complete();
		});
	}
}
```

### TestContext Methods

- `expectEntityAt(EntityType, BlockPos)` - verify entity exists at position
- `assertTrue(condition, message)` - assert condition is true
- `assertFalse(condition, message)` - assert condition is false
- `runAtTick(tick, Runnable)` - schedule action at specific tick
- `complete()` - mark test as passed
- `throwPositionedException(message, pos)` - fail test with position info
- `getWorld()` - get test world (ServerWorld)

### Game Test Parameters

```java
@GameTest(
	templateName = FabricGameTest.EMPTY_STRUCTURE,  // Use empty 3x3x3 structure
	tickLimit = 100,                                 // Max ticks before timeout (default: 100)
	required = true                                  // Fail build if test fails (default: true)
)
```

### Test Discovery

Tests are auto-discovered via `@GameTest` annotation - no manual registration needed beyond `fabric.mod.json` entrypoint.

### Registering Game Tests

Add your game test class to `fabric.mod.json`:

```json
"fabric-gametest": [
	"com.grahambartley.gametest.MyEntityGameTest",
	"com.grahambartley.gametest.AnotherGameTest"
]
```

## Manual Testing

**CRITICAL**: Before pushing ANY change that impacts functionality, you MUST run the game client and verify changes work correctly.

### Running the Game Client

Use the **run-game-client** skill to launch the Minecraft client with the mod loaded.

See `.ai/skills/run-game-client.md` for:
- Java version setup
- Common testing tasks
- Hot reloading tips
- Troubleshooting

**Quick command** (requires Java 21):
```bash
./gradlew runClient
```

### Manual Testing Checklist

When testing features in-game:
- [ ] Visual elements render correctly
- [ ] Entity animations play properly
- [ ] User interactions work as expected
- [ ] Sounds play at correct times
- [ ] Collar colors display correctly
- [ ] Breeding produces correct offspring
- [ ] Biome spawning works
- [ ] **Developer approval received** before pushing

## CI/CD Integration

Tests run automatically on every push and pull request via GitHub Actions.

### GitHub Actions Workflow

The workflow (`.github/workflows/gradle.yml`) runs:
1. Code formatting check (Spotless)
2. JUnit tests with coverage verification (`./gradlew check`)
3. Game tests (`./gradlew runGametestServer`)
4. Test result reporting (published to PR checks)
5. Coverage report publishing

### Test Reports

- **Test results** are published as GitHub Check annotations
- **Failed tests** show up directly in pull requests
- **Coverage reports** are added as PR comments
- **Detailed test summary** available in the Actions tab
- **Artifacts**: `gametest-results.xml` available as downloadable artifact

### Viewing CI Results

- **PR Checks**: Test results appear in the "Checks" tab of pull requests
- **PR Comments**: Coverage reports show up as automated comments
- **Actions Tab**: Full logs and test reports available in the repository's Actions tab
- **Local Reports**: `build/reports/tests/test/index.html` after running `./gradlew test`

## Complete Test Workflow

Before pushing any change:

```bash
# 1. Apply code formatting
./gradlew spotlessApply

# 2. Run unit tests
./gradlew test

# 3. Run game tests (headless)
./gradlew runGametestServer

# 4. Verify build
./gradlew clean build

# 5. Manual testing (if functionality changed)
./gradlew runClient
# Test in-game and wait for developer approval
```

## Test Structure

```
src/
├── main/
│   └── java/
│       └── com/grahambartley/
│           └── gametest/
│               ├── HuskyEntityGameTest.java
│               ├── DachshundEntityGameTest.java
│               └── UnleashedDogEntityGameTest.java
└── test/
	└── java/
		└── com/grahambartley/
			├── DogsUnleashedTest.java
			├── DogsUnleashedClientTest.java
			├── ModEntitiesTest.java
			├── ModItemsTest.java
			├── ModSpawnsTest.java
			├── entity/
			│   └── HuskyEntityTest.java
			├── model/
			│   └── DogModelTest.java
			└── render/
				└── DogRendererTest.java
```

## Troubleshooting

### Build Fails
```bash
./gradlew clean build --info
```

### Tests Fail
```bash
# Unit tests
./gradlew test --info

# Game tests
./gradlew runGametestServer --info
```

### Coverage Reports Not Generated
```bash
# Ensure check task runs (it includes coverage)
./gradlew check

# Manually generate coverage report
./gradlew jacocoTestReport
```

### Game Tests Timeout

- Increase `tickLimit` in `@GameTest` annotation
- Check for infinite loops in test code
- Ensure `context.complete()` is called

### Game Tests Not Discovered

- Verify class is registered in `fabric.mod.json` under `fabric-gametest`
- Ensure class implements `FabricGameTest`
- Check that methods are annotated with `@GameTest`

## Quick Reference

```bash
# Unit tests only
./gradlew test

# All checks (lint + unit tests + coverage)
./gradlew check

# Game tests (headless)
./gradlew runGametestServer

# Game tests (interactive)
./gradlew runGametest

# Full build
./gradlew clean build

# Manual testing
./gradlew runClient

# View coverage report
open build/reports/jacoco/test/html/index.html

# View unit test report
open build/reports/tests/test/index.html
```

## Current Test Stats

- **Total Unit Tests**: 24+ passing tests
- **Test Classes**: 9+ test classes
- **Game Test Classes**: Multiple breed-specific and common tests
- **Coverage**: Reported but not enforced (Minecraft code requires runtime)
