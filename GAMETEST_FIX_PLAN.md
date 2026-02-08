# Dog Grave Gametest Fix Plan

## Investigation Summary

After analyzing the codebase and test environment, I've identified the root causes:

### Key Findings:

1. **Test Structure Issue**: The grave placement test manually calls `world.setBlockState()` then `ModBlocks.DOG_GRAVE.onPlaced()`, but this flow differs from real BlockItem.place() behavior
2. **Working Reference**: Dog death grave spawning (spawnGrave) uses `setBlockState()` + direct setter calls and works
3. **No Similar Tests**: DogBedBlock tests don't test ItemStack→BlockEntity transfer, only direct setter calls
4. **Component System Works**: The `dogGraveItemRetainsData` test passes, proving components work on ItemStacks

## Root Causes

### Tests 1-3 (Placement/Breaking):
**Problem**: Block entity data isn't persisting when transferred via onPlaced
**Why**: The test simulates placement incorrectly - it calls onPlaced manually, but the block entity might not be fully initialized, or there's a timing issue with chunk loading/tick processing

### Test 4 (Untamed Dog):
**Problem**: Grave spawning for untamed dog
**Why**: Need to verify `isTamed()` is actually false, or check if super.onDeath() does something unexpected

### Test 5 (Tamed Dog):
**Problem**: UUID doesn't match
**Why**: Block entity data set in spawnGrave() isn't persisting, similar to tests 1-3

## Fix Strategy

### Phase 1: Simplify Block Entity Data Transfer ✅

Instead of complex onPlaced logic with components, use the SAME pattern as spawnGrave (which works):

**Current (broken)**: ItemStack components → onPlaced → BlockEntity setters
**New (working)**: BlockItem override → Direct BlockEntity access → setters

**Implementation**:
1. Override `DogGraveItem.postPlacement()` to handle data transfer
2. Or use `BlockItem.writeBlockEntityNbt()` with NBT instead of components
3. Or keep onPlaced but add explicit chunk/tick synchronization

### Phase 2: Fix Breaking Item Drops 🔧

The breaking tests fail because `world.breakBlock()` doesn't trigger the right flow.

**Current Flow (broken)**:
```
world.breakBlock(pos, drop=true, player)
→ onBreak (my custom logic)
→ Block.dropStacks (loot tables - none exist)
```

**Issues**:
- My onBreak drops manually
- Block.dropStacks also tries to drop (but no loot table)
- Something is dropping when it shouldn't

**Fix Options**:

A. **Use Loot Tables** (Proper Minecraft Way):
- Create loot table: `data/dogs-unleashed/loot_table/blocks/dog_grave.json`
- Use copy_components function to transfer block entity data
- Remove manual dropping from onBreak

B. **Fix Manual Dropping** (Current Approach):
- Keep onBreak manual drops
- Override `getDroppedStacks()` to return empty (prevent loot table drops)
- Ensure pickaxe check works correctly

**Recommendation**: Option A (loot tables) is the Minecraft standard and more reliable

### Phase 3: Fix Dog Death Tests 🐕

**Test 4 (untamed)**:
- Add debug check: verify `dog.isTamed()` returns false before death
- Check if super.onDeath() spawns graves
- Verify onDeath is actually being called

**Test 5 (tamed)**:
- spawnGrave() code looks correct
- Issue is likely block entity persistence (same as test 1)
- Once Phase 1 fix applies, this should work

### Phase 4: Rewrite Tests for Correctness 📝

Current tests manually call `setBlockState()` + `onPlaced()`, which isn't how real placement works.

**Better Test Pattern**:
```java
// Use BlockItem.place() through ItemPlacementContext
ItemStack stack = new ItemStack(ModItems.DOG_GRAVE);
stack.set(ModComponents.DOG_GRAVE_NAME, "TestName");

// Simulate actual placement
ItemPlacementContext ctx = new ItemPlacementContext(
	new ItemUsageContext(world, player, Hand.MAIN_HAND, stack, hit));

BlockItem item = (BlockItem) stack.getItem();
item.place(ctx);

// Then verify
DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(pos);
context.assertTrue(grave.getDogName().equals("TestName"));
```

**OR** follow spawnGrave pattern (direct setters):
```java
world.setBlockState(pos, ModBlocks.DOG_GRAVE.getDefaultState());

DogGraveBlockEntity grave = (DogGraveBlockEntity) world.getBlockEntity(pos);
grave.setDogUuid(uuid);
grave.setDogName(name);
grave.setFlowerColor(color);

// Then verify persistence
context.runAtTick(10, () -> {
	DogGraveBlockEntity reloaded = (DogGraveBlockEntity) world.getBlockEntity(pos);
	context.assertTrue(uuid.equals(reloaded.getDogUuid()));
});
```

## Recommended Implementation Order

1. **Quick Win**: Fix Test 4 (untamed dog) - just verify isTamed() check
2. **Core Fix**: Implement loot tables for proper item dropping (Tests 2-3)
3. **Data Persistence**: Fix onPlaced or use BlockItem override (Test 1)
4. **Verify**: Test 5 should work once data persistence is fixed
5. **Refactor**: Update tests to use proper placement patterns

## Files to Modify

1. `/src/main/java/com/grahambartley/item/DogGraveItem.java` - Override postPlacement or use NBT
2. `/src/main/resources/data/dogs-unleashed/loot_table/blocks/dog_grave.json` - CREATE loot table
3. `/src/main/java/com/grahambartley/block/DogGraveBlock.java` - Remove manual drops, simplify
4. `/src/main/java/com/grahambartley/gametest/DogGraveGameTest.java` - Fix test patterns
5. `/src/main/java/com/grahambartley/entity/UnleashedDogEntity.java` - Verify onDeath logic

## Expected Outcome

- All 5 tests pass ✅
- Proper Minecraft-standard implementation (loot tables)
- Data persists correctly through placement/breaking
- Tests accurately reflect real gameplay behavior
