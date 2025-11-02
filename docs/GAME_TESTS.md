# Game Tests

Game tests run in an actual Minecraft server environment to test entity behavior, spawning, AI, and interactions.

## Running Game Tests

### Locally (Interactive)
```bash
./gradlew runGametest
```
This starts a Minecraft server with game tests enabled. The server will run tests when triggered.

### Locally (Headless for CI/CD testing)
```bash
./gradlew runGametestServer
```
This runs in headless mode (--nogui) suitable for automated testing.

### In CI/CD
Game tests run automatically in GitHub Actions on every push to main and on pull requests.

The workflow runs: `./gradlew runGametestServer --no-daemon --stacktrace`

Results are published to `build/gametest-results.xml`

## Writing Game Tests

1. Create test class in `src/main/java/com/grahambartley/gametest/`
2. Implement `FabricGameTest` interface
3. Add methods annotated with `@GameTest`
4. Register in `fabric.mod.json` under `fabric-gametest` entrypoint
5. Use `TestContext` for assertions

### Example

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

## TestContext Methods

- `expectEntityAt(EntityType, BlockPos)` - verify entity exists at position
- `assertTrue(condition, message)` - assert condition is true
- `assertFalse(condition, message)` - assert condition is false
- `runAtTick(tick, Runnable)` - schedule action at specific tick
- `complete()` - mark test as passed
- `throwPositionedException(message, pos)` - fail test with position info
- `getWorld()` - get test world (ServerWorld)

## Game Test Parameters

```java
@GameTest(
templateName = FabricGameTest.EMPTY_STRUCTURE,  // Use empty 3x3x3 structure
tickLimit = 100,                                 // Max ticks before timeout (default: 100)
required = true                                  // Fail build if test fails (default: true)
)
```

## Test Discovery

Tests are auto-discovered via `@GameTest` annotation - no manual registration needed.

## CI/CD Reports

Game test results are published as JUnit reports in GitHub Actions:
- Test report artifact: `gametest-results.xml`
- Viewable in the "Checks" tab of pull requests
