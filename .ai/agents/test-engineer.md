# Test Engineer Agent

This agent specializes in writing and maintaining gametests for the Dogs Unleashed mod.

**For Claude users:** This agent is available at `.claude/agents/minecraft-dogs-test-engineer.md`

**For Cursor users:** Reference this documentation when working on tests.

## When to Use This Agent

- Writing new gametests for features
- Refactoring existing test code
- Fixing failing gametests
- Adding test coverage for breeds
- Organizing test files

## Core Principles

1. **Use Shared Utilities**: All tests MUST use DogTestData and DogTestHelper
2. **Feature-Based Organization**: Group tests by feature, not breed
3. **Parameterized Pattern**: One helper method called by breed-specific wrappers
4. **No Duplication**: Extract common code to utilities immediately

## Quick Reference

### DogTestData Constants
```java
DogTestData.HUSKY           // health=25.0, speed=0.30, damage=5.0
DogTestData.DACHSHUND       // health=10.0, speed=0.25, damage=2.0
DogTestData.BEAGLE          // health=17.0, speed=0.29, damage=3.0
DogTestData.GOLDEN_RETRIEVER // health=24.0, speed=0.30, damage=4.0
DogTestData.SHIBA_INU       // health=18.0, speed=0.32, damage=3.5
```

### DogTestHelper Methods
```java
DogTestHelper.spawnDog(context, DogTestData.HUSKY)
DogTestHelper.spawnDog(context, DogTestData.HUSKY, pos)
DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY)
DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, pos)
DogTestHelper.damageEntity(dog, amount)
```

## Test Pattern

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

	// Repeat for all 5 breeds...

	private <T extends UnleashedDogEntity> void testDogFeatureWorks(
			TestContext context, DogTestData<T> data) {
		T dog = DogTestHelper.spawnDog(context, data);

		context.assertTrue(
			dog.someFeature(),
			data.breedId() + " should have feature");
		context.complete();
	}
}
```

## Test Organization

- **DogEntityCoreTest** - Spawning, taming, dimensions, NBT
- **DogEntityBehaviorTest** - Collar colors, breeding, items, shaking
- **DogEntitySoundTest** - Barking, howling, cooldowns
- **DogEntityBreedSpecificTest** - Breed attributes, baby creation

See `.ai/skills/run-tests.md` for complete testing documentation.

## Full Documentation

For complete agent documentation with all patterns and examples, see:
- Claude: `.claude/agents/minecraft-dogs-test-engineer.md`
- Skills: `.ai/skills/run-tests.md`
