package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogStrangerInteractionGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-stranger-interaction";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos PARTNER_POS = new BlockPos(1, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final BlockPos STRANGER_POS = new BlockPos(2, 2, 3);
  private static final int HELD_STACK_COUNT = 3;
  private static final float HURT_HEALTH = 4.0F;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void strangerCannotDyeTheCollar(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    final DyeColor before = dog.getComponents().get(DogCollar.class).getColor();
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BLUE_DYE, HELD_STACK_COUNT));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertEquals(
        before,
        dog.getComponents().get(DogCollar.class).getColor(),
        "A stranger's dye must not change the collar");
    context.assertEquals(
        HELD_STACK_COUNT,
        stranger.getStackInHand(Hand.MAIN_HAND).getCount(),
        "A stranger's dye must not be used up");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void ownerCanDyeTheCollar(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BLUE_DYE, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertEquals(
        DyeColor.BLUE,
        dog.getComponents().get(DogCollar.class).getColor(),
        "The owner's dye should turn the collar blue");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void strangerCannotPutTheDogInLove(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BEEF, HELD_STACK_COUNT));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertFalse(dog.isInLove(), "A stranger's meat must not put the dog in love");
    context.assertEquals(
        HELD_STACK_COUNT,
        stranger.getStackInHand(Hand.MAIN_HAND).getCount(),
        "A stranger's meat must not be eaten by a dog at full health");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void ownerCanPutTheDogInLove(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BEEF, HELD_STACK_COUNT));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(dog.isInLove(), "The owner's meat should put the dog in love");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void strangerCanStillHealAHurtDog(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    dog.setHealth(HURT_HEALTH);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BEEF, HELD_STACK_COUNT));

    dog.interactMob(stranger, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getHealth() > HURT_HEALTH,
        "A stranger's meat should still heal a hurt dog, health is " + dog.getHealth());
    context.assertFalse(dog.isInLove(), "Healing must not put the dog in love");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void puppyNeverBelongsToAStranger(TestContext context) {
    final ServerPlayerEntity owner = survivalPlayerAt(context, OWNER_POS);
    final ServerPlayerEntity stranger = survivalPlayerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DOG_POS);
    final UnleashedDogEntity partner = spawnOwnedDog(context, owner, PARTNER_POS);
    dog.lovePlayer(stranger);
    partner.lovePlayer(owner);

    final UnleashedDogEntity puppy =
        (UnleashedDogEntity) dog.createChild(context.getWorld(), partner);

    context.assertEquals(
        owner.getUuid(), puppy.getOwnerUuid(), "The puppy should belong to its parents' owner");
    context.assertTrue(puppy.isTamed(), "The puppy should be born tamed");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void puppyIsAdoptedByTheBreederWhoOwnsOneParent(TestContext context) {
    final ServerPlayerEntity firstOwner = survivalPlayerAt(context, OWNER_POS);
    final ServerPlayerEntity secondOwner = survivalPlayerAt(context, STRANGER_POS);
    final UnleashedDogEntity dog = spawnOwnedDog(context, firstOwner, DOG_POS);
    final UnleashedDogEntity partner = spawnOwnedDog(context, secondOwner, PARTNER_POS);
    dog.lovePlayer(secondOwner);
    partner.lovePlayer(secondOwner);

    final UnleashedDogEntity puppy =
        (UnleashedDogEntity) dog.createChild(context.getWorld(), partner);

    context.assertEquals(
        secondOwner.getUuid(),
        puppy.getOwnerUuid(),
        "The puppy should belong to the breeder who owns a parent");
    context.assertEquals(
        DogCommand.SIT, puppy.getCommand(), "An adopted newborn should be told to sit");
    context.complete();
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos relativePos) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, relativePos);
    dog.setAiDisabled(true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setTamed(true, true);
    dog.setHealth(dog.getMaxHealth());
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
