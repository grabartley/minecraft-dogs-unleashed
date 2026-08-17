package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import java.util.Collection;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class DogEquipmentGameTest implements FabricGameTest {

  private static final String BATCH = "dog-equipment";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final int TICK_LIMIT = 20;
  private static final int DEATH_TICK_LIMIT = 40;
  private static final int DEATH_ASSERT_TICK = 20;
  private static final int HELD_STACK_COUNT = 3;
  private static final int WORN_ARMOUR_DAMAGE = 5;
  private static final int FIRST_HOTBAR_SLOT_INDEX = DogEquipmentScreenHandler.DOG_SLOT_COUNT + 27;

  @CustomTestProvider
  public Collection<TestFunction> directEquipFillsArmourSlotPerBreed() {
    return generatePerBreed(
        "directequipfillsarmourslot", TICK_LIMIT, DogEquipmentGameTest::testDirectEquipFillsArmour);
  }

  @CustomTestProvider
  public Collection<TestFunction> shearsRemoveArmourPerBreed() {
    return generatePerBreed(
        "shearsremovearmour", TICK_LIMIT, DogEquipmentGameTest::testShearsRemoveArmour);
  }

  @CustomTestProvider
  public Collection<TestFunction> untamedDogRejectsEquipPerBreed() {
    return generatePerBreed(
        "untamedrejectsequip", TICK_LIMIT, DogEquipmentGameTest::testUntamedDogRejectsEquip);
  }

  private static void testDirectEquipFillsArmour(
      final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, data, owner);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOLF_ARMOR, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isOf(Items.WOLF_ARMOR),
        "Right-clicking with wolf armour should fill the armour slot");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(),
        HELD_STACK_COUNT - 1,
        "Equipping should consume exactly one item from the held stack");
    context.complete();
  }

  private static void testShearsRemoveArmour(final TestContext context, final DogTestData data) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, data, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "Shearing should empty the armour slot");
    context.assertTrue(
        inventoryHolds(owner, Items.WOLF_ARMOR, 0),
        "Shearing should return the armour to the owner");
    context.complete();
  }

  private static void testUntamedDogRejectsEquip(
      final TestContext context, final DogTestData data) {
    final ServerPlayerEntity player = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data, DOG_POS);
    dog.setAiDisabled(true);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOLF_ARMOR));

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "An untamed dog must not accept equipment from any interaction");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void armourSlotAcceptsCanineArmourOnly(final TestContext context) {
    context.assertTrue(
        DogEquipmentSlot.ARMOUR.canHold(new ItemStack(Items.WOLF_ARMOR)),
        "The armour slot must accept vanilla wolf armour");
    assertArmourSlotRejects(context, Items.IRON_HORSE_ARMOR);
    assertArmourSlotRejects(context, Items.DIAMOND_HORSE_ARMOR);
    assertArmourSlotRejects(context, Items.LEATHER_HORSE_ARMOR);
    assertArmourSlotRejects(context, Items.IRON_CHESTPLATE);
    assertArmourSlotRejects(context, Items.SADDLE);
    assertArmourSlotRejects(context, Items.BONE);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void pendantAndCosmeticSlotsRejectEverythingWhileTagsAreEmpty(final TestContext context) {
    for (final DogEquipmentSlot slot :
        new DogEquipmentSlot[] {DogEquipmentSlot.PENDANT, DogEquipmentSlot.COSMETIC}) {
      context.assertFalse(
          slot.canHold(new ItemStack(Items.WOLF_ARMOR)),
          slot + " must reject wolf armour while its tag is empty");
      context.assertFalse(
          slot.canHold(new ItemStack(Items.GOLD_INGOT)),
          slot + " must reject untagged items while its tag is empty");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void equippingOccupiedSlotReturnsThePreviousItem(final TestContext context) {
    final ServerPlayerEntity owner = survivalOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    final ItemStack worn = new ItemStack(Items.WOLF_ARMOR);
    worn.setDamage(WORN_ARMOUR_DAMAGE);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, worn);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOLF_ARMOR));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).getDamage(),
        0,
        "The freshly equipped armour should be the undamaged one");
    context.assertTrue(
        inventoryHolds(owner, Items.WOLF_ARMOR, WORN_ARMOUR_DAMAGE),
        "Swapping an occupied slot must return the previous item instead of deleting it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everySlotSurvivesAnNbtRoundTrip(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.PENDANT, new ItemStack(Items.GOLD_INGOT));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSMETIC, new ItemStack(Items.PAPER));

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    final UnleashedDogEntity reloaded =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);

    assertSlotHolds(context, reloaded, DogEquipmentSlot.ARMOUR, Items.WOLF_ARMOR);
    assertSlotHolds(context, reloaded, DogEquipmentSlot.PENDANT, Items.GOLD_INGOT);
    assertSlotHolds(context, reloaded, DogEquipmentSlot.COSMETIC, Items.PAPER);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void equipmentSurvivesTheSummonTeleportPath(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.PENDANT, new ItemStack(Items.GOLD_INGOT));

    final BlockPos destination = context.getAbsolutePos(OWNER_POS);
    final UnleashedDogEntity summoned =
        dog.teleportToWorld(context.getWorld(), Vec3d.ofBottomCenter(destination));

    assertSlotHolds(context, summoned, DogEquipmentSlot.ARMOUR, Items.WOLF_ARMOR);
    assertSlotHolds(context, summoned, DogEquipmentSlot.PENDANT, Items.GOLD_INGOT);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DEATH_TICK_LIMIT)
  public void deathDropsEveryOccupiedSlot(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.PENDANT, new ItemStack(Items.GOLD_INGOT));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSMETIC, new ItemStack(Items.PAPER));

    dog.kill();

    context.runAtTick(
        DEATH_ASSERT_TICK,
        () -> {
          assertDroppedExactlyOne(context, Items.WOLF_ARMOR);
          assertDroppedExactlyOne(context, Items.GOLD_INGOT);
          assertDroppedExactlyOne(context, Items.PAPER);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void wildDogsSpawnWithEverySlotEmpty(final TestContext context) {
    final UnleashedDogEntity wild = DogTestHelper.spawnDog(context, DogTestData.HUSKY, DOG_POS);
    wild.setAiDisabled(true);

    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      context.assertTrue(
          wild.getEquipmentHolder().getStack(slot).isEmpty(),
          "A naturally spawned dog must have an empty " + slot);
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shiftClickMovesArmourFromTheHotbarIntoTheArmourSlot(final TestContext context) {
    final ServerPlayerEntity owner = serverOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    owner.getInventory().setStack(0, new ItemStack(Items.WOLF_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    handler.quickMove(owner, FIRST_HOTBAR_SLOT_INDEX);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isOf(Items.WOLF_ARMOR),
        "Shift-clicking wolf armour should route it into the armour slot");
    context.assertTrue(
        owner.getInventory().getStack(0).isEmpty(),
        "Shift-clicking should clear the source hotbar slot");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shiftClickMovesArmourBackOutOfTheArmourSlot(final TestContext context) {
    final ServerPlayerEntity owner = serverOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    handler.quickMove(owner, 0);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "Shift-clicking out of the armour slot should empty it");
    context.assertTrue(
        owner.getInventory().contains(new ItemStack(Items.WOLF_ARMOR)),
        "Shift-clicking out of the armour slot should hand the armour back to the player");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void shiftClickLeavesUnacceptedItemsOutOfTheDogSlots(final TestContext context) {
    final ServerPlayerEntity owner = serverOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    owner.getInventory().setStack(0, new ItemStack(Items.IRON_HORSE_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    handler.quickMove(owner, FIRST_HOTBAR_SLOT_INDEX);

    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      context.assertTrue(
          dog.getEquipmentHolder().getStack(slot).isEmpty(),
          slot + " must stay empty for an unaccepted item");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void screenStaysUsableForTheOwnerAndClosesForEveryoneElse(final TestContext context) {
    final ServerPlayerEntity owner = serverOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    context.assertTrue(
        handler.canUse(owner), "The owner standing beside the dog may use the screen");

    final ServerPlayerEntity stranger = serverOwnerAt(context, OWNER_POS);
    context.assertFalse(
        handler.canUse(stranger), "A non-owner must never hold the equipment screen");

    dog.kill();
    context.assertFalse(handler.canUse(owner), "The screen must close once the dog dies");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void takingArmourOutOfTheScreenClearsTheEntitySlot(final TestContext context) {
    final ServerPlayerEntity owner = serverOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, DogTestData.HUSKY, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.ARMOUR, new ItemStack(Items.WOLF_ARMOR));
    final DogEquipmentScreenHandler handler = openHandler(owner, dog);

    handler.onSlotClick(0, 0, SlotActionType.PICKUP, owner);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty(),
        "Picking the armour up must clear the entity slot, not just the screen copy");
    context.assertTrue(
        handler.getCursorStack().isOf(Items.WOLF_ARMOR),
        "The picked-up armour should end up on the cursor");
    context.complete();
  }

  private static DogEquipmentScreenHandler openHandler(
      final ServerPlayerEntity owner, final UnleashedDogEntity dog) {
    return new DogEquipmentScreenHandler(1, owner.getInventory(), dog);
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final DogTestData data, final PlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, data, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static ServerPlayerEntity survivalOwnerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = serverOwnerAt(context, relativePos);
    owner.changeGameMode(GameMode.SURVIVAL);
    return owner;
  }

  private static ServerPlayerEntity serverOwnerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    placeAt(context, owner, relativePos);
    return owner;
  }

  private static boolean inventoryHolds(
      final PlayerEntity player, final Item item, final int damage) {
    final PlayerInventory inventory = player.getInventory();
    for (int index = 0; index < inventory.size(); index++) {
      final ItemStack stack = inventory.getStack(index);
      if (stack.isOf(item) && stack.getDamage() == damage) {
        return true;
      }
    }
    return false;
  }

  private static void placeAt(
      final TestContext context, final PlayerEntity player, final BlockPos relativePos) {
    final BlockPos absolutePos = context.getAbsolutePos(relativePos);
    player.refreshPositionAndAngles(
        absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0f, 0f);
  }

  private static void assertArmourSlotRejects(final TestContext context, final Item item) {
    context.assertFalse(
        DogEquipmentSlot.ARMOUR.canHold(new ItemStack(item)),
        "The armour slot must reject " + item + ", which is not canine animal armour");
  }

  private static void assertSlotHolds(
      final TestContext context,
      final UnleashedDogEntity dog,
      final DogEquipmentSlot slot,
      final Item expected) {
    context.assertTrue(
        dog.getEquipmentHolder().getStack(slot).isOf(expected),
        slot
            + " should still hold "
            + expected
            + " but held "
            + dog.getEquipmentHolder().getStack(slot));
  }

  private static void assertDroppedExactlyOne(final TestContext context, final Item expected) {
    final int dropped =
        context
            .getWorld()
            .getEntitiesByClass(
                ItemEntity.class,
                context.getTestBox().expand(6),
                item -> item.getStack().isOf(expected))
            .size();
    context.assertEquals(dropped, 1, "Exactly one " + expected + " should drop on death");
  }

  private Collection<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
                    "dogequipmentgametest." + behavior + "." + data.breed().serializedId(),
                    ARENA,
                    tickLimit,
                    /* setupTicks */ 0L,
                    /* required */ true,
                    context -> body.run(context, data)))
        .toList();
  }

  @FunctionalInterface
  private interface PerBreedBody {
    void run(TestContext context, DogTestData data);
  }
}
