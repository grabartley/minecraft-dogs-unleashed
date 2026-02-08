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

**CRITICAL**: All new gametests MUST use the shared utilities pattern (see DogTestData and DogTestHelper).

1. Create test class in `src/main/java/com/grahambartley/gametest/`
2. Implement `FabricGameTest` interface
3. Use `DogTestData` and `DogTestHelper` utilities (never duplicate spawn logic)
4. Add methods annotated with `@GameTest`
5. Register in `fabric.mod.json` under `fabric-gametest` entrypoint
6. Use `TestContext` for assertions

**Gametest Organization Pattern:**

Organize tests by FEATURE, not by breed:
- `DogEntityCoreTest` - spawning, taming, dimensions, NBT
- `DogEntityBehaviorTest` - collar colors, breeding, items, shaking
- `DogEntitySoundTest` - barking, howling, cooldowns
- `DogEntityBreedSpecificTest` - breed attributes, baby creation

**Example using shared utilities:**

```java
public final class DogEntityFeatureTest implements FabricGameTest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void huskyFeatureWorks(TestContext context) {
		testDogFeatureWorks(context, DogTestData.HUSKY);
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void dachshundFeatureWorks(TestContext context) {
		testDogFeatureWorks(context, DogTestData.DACHSHUND);
	}

	// Repeat for all breeds...

	private <T extends UnleashedDogEntity> void testDogFeatureWorks(
			TestContext context, DogTestData<T> data) {
		// Use helper to spawn - NEVER duplicate spawn logic
		T dog = DogTestHelper.spawnDog(context, data);

		// Test logic here
		context.assertTrue(dog.someMethod(), data.breedId() + " should have feature");
		context.complete();
	}
}
```

**Key Best Practices:**

- **Use DogTestHelper.spawnDog()** - Never manually spawn entities
- **Use DogTestHelper.spawnTamedDog()** - For tamed dog tests
- **Use DogTestData constants** - HUSKY, DACHSHUND, BEAGLE, GOLDEN_RETRIEVER, SHIBA_INU
- **Parameterized pattern** - Write one helper method, call from breed-specific wrappers
- **succeedWhen() for async** - Use `context.succeedWhen(() -> condition)` for continuous checking
- **Registry checks** - Verify sounds/entities in actual registries, not just null checks
- **Feature grouping** - Group tests by what they test, not what breed

**Example with async behavior:**

```java
@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
public void huskyBarksWhenLowHealth(TestContext context) {
	testDogBarksWhenLowHealth(context, DogTestData.HUSKY);
}

private <T extends UnleashedDogEntity> void testDogBarksWhenLowHealth(
		TestContext context, DogTestData<T> data) {
	T dog = DogTestHelper.spawnDog(context, data);

	context.runAtTick(5, () -> {
		dog.setHealth(1.0f);
	});

	// Use succeedWhen for continuous checking until bark happens
	context.runAtTick(10, () -> {
		context.succeedWhen(() -> dog.getBarkCooldownTicks() > 0);
	});
}
```

### Gametest Utilities (MANDATORY)

**DogTestData** - Centralized breed test configurations
```java
// Pre-configured constants with expected attributes
DogTestData.HUSKY           // Expected: health=25.0, speed=0.30, damage=5.0
DogTestData.DACHSHUND       // Expected: health=10.0, speed=0.25, damage=2.0
DogTestData.BEAGLE          // Expected: health=17.0, speed=0.29, damage=3.0
DogTestData.GOLDEN_RETRIEVER // Expected: health=24.0, speed=0.30, damage=4.0
DogTestData.SHIBA_INU       // Expected: health=18.0, speed=0.32, damage=3.5

// Access breed data
data.entityType()           // EntityType<T>
data.factory()              // Function<World, T>
data.breedId()              // String identifier
data.expectedWidth()        // float (0.8f for all breeds)
data.expectedHeight()       // float (1.1f for all breeds)
data.expectedMaxHealth()    // double (breed-specific)
data.expectedMovementSpeed() // double (breed-specific)
data.expectedAttackDamage() // double (breed-specific)
data.expectedBarkSound()    // SoundEvent (breed-specific)
```

**DogTestHelper** - Spawn utilities (NEVER duplicate this logic)
```java
// Spawn untamed dog at default position (0, 1, 0)
DogTestHelper.spawnDog(context, DogTestData.HUSKY)

// Spawn untamed dog at specific position
DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(5, 1, 5))

// Spawn tamed dog at default position
DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY)

// Spawn tamed dog at specific position
DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(5, 1, 5))

// Damage entity with generic damage source
DogTestHelper.damageEntity(dog, 5.0f)
```

### TestContext Methods

- `expectEntityAt(EntityType, BlockPos)` - verify entity exists at position
- `assertTrue(condition, message)` - assert condition is true
- `assertFalse(condition, message)` - assert condition is false
- `runAtTick(tick, Runnable)` - schedule action at specific tick
- `complete()` - mark test as passed
- `succeedWhen(Supplier<Boolean>)` - continuously check condition until true (for async behavior)
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
│               ├── util/
│               │   ├── DogTestData.java      # Breed test configurations
│               │   └── DogTestHelper.java    # Spawn utilities
│               ├── DogEntityCoreTest.java           # Core mechanics
│               ├── DogEntityBehaviorTest.java       # Interactive behaviors
│               ├── DogEntitySoundTest.java          # Sound systems
│               ├── DogEntityBreedSpecificTest.java  # Breed attributes
│               ├── DogBedBlockGameTest.java         # Dog bed tests
│               ├── DogSleepBehaviorGameTest.java    # Sleep system
│               └── DogGraveGameTest.java            # Grave system
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
