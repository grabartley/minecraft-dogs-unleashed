package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.screenhandler.DogEquipmentScreenHandler;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogCostumeGameTest implements FabricGameTest {

  private static final String BATCH = "dog-costumes";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final BlockPos STRANGER_POS = new BlockPos(2, 2, 3);
  private static final int TICK_LIMIT = 20;
  private static final int DEATH_TICK_LIMIT = 40;
  private static final int DEATH_ASSERT_TICK = 20;
  private static final Item COSTUME = Items.NAUTILUS_SHELL;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void costumeSlotTakesTaggedCostumesAndNothingElse(final TestContext context) {
    final ItemStack costume = new ItemStack(COSTUME);
    context.assertTrue(
        DogEquipmentSlot.COSTUME.canHold(costume), "A tagged costume should fit the costume slot");
    for (final DogEquipmentSlot slot : DogEquipmentSlot.values()) {
      if (slot != DogEquipmentSlot.COSTUME) {
        context.assertFalse(slot.canHold(costume), "A costume must not fit the " + slot + " slot");
      }
    }
    context.assertFalse(
        DogEquipmentSlot.COSTUME.canHold(new ItemStack(Items.WOLF_ARMOR)),
        "The costume slot must reject armour");
    context.assertFalse(
        DogEquipmentSlot.COSTUME.canHold(new ItemStack(Items.CARVED_PUMPKIN)),
        "The costume slot must reject cosmetics");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void ownerPutsACostumeOnByRightClickAndGetsTheOldOneBack(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final ItemStack old = new ItemStack(COSTUME);
    old.set(DataComponentTypes.CUSTOM_NAME, Text.literal("old"));
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, old);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME, 2));

    dog.interactMob(owner, Hand.MAIN_HAND);

    final ItemStack worn = dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME);
    context.assertTrue(
        worn.isOf(COSTUME) && !worn.contains(DataComponentTypes.CUSTOM_NAME),
        "Right-clicking with a costume should put the held one on");
    context.assertEquals(
        owner.getStackInHand(Hand.MAIN_HAND).getCount(), 1, "Exactly one costume should be used");
    context.assertTrue(
        owner.getInventory().main.stream()
            .anyMatch(
                stack -> stack.isOf(COSTUME) && stack.contains(DataComponentTypes.CUSTOM_NAME)),
        "The costume the dog wore should be handed back");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void puppiesCanWearACostume(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity puppy = spawnOwnedDog(context, owner);
    puppy.setBaby(true);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    puppy.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        puppy.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "A puppy should take a costume");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void strangerCannotPutACostumeOn(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isEmpty(),
        "A stranger must not dress someone else's dog");
    context.assertTrue(
        stranger.getStackInHand(Hand.MAIN_HAND).isOf(COSTUME),
        "The stranger should keep the costume");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void strangerCannotTakeACostumeOff(final TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(COSTUME));

    dog.interactMob(stranger, Hand.MAIN_HAND);
    dog.interactMob(stranger, Hand.OFF_HAND);

    context.assertTrue(
        dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "The dog should keep its costume");
    context.assertEquals(
        stranger.getStackInHand(Hand.MAIN_HAND).getCount(),
        1,
        "A stranger must not swap the costume for their own");
    context.assertFalse(
        new DogEquipmentScreenHandler(1, stranger.getInventory(), dog).canUse(stranger),
        "A stranger must not reach the costume through the gear screen");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void costumeSurvivesAReload(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        reloaded.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME).isOf(COSTUME),
        "The costume should still be worn after a reload");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DEATH_TICK_LIMIT)
  public void costumeDropsWhenTheDogDies(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.getEquipmentHolder().setStack(DogEquipmentSlot.COSTUME, new ItemStack(COSTUME));

    dog.kill();

    context.runAtTick(
        DEATH_ASSERT_TICK,
        () -> {
          context.assertEquals(
              DogTestHelper.droppedStacksOf(context, COSTUME).size(),
              1,
              "Exactly one costume should drop on death");
          context.complete();
        });
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static ServerPlayerEntity survivalPlayerAt(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    final BlockPos absolutePos = context.getAbsolutePos(relativePos);
    player.refreshPositionAndAngles(
        absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5, 0f, 0f);
    return player;
  }
}
