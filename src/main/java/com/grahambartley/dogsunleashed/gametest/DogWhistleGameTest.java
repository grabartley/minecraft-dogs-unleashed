package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Covers the Dog Whistle end to end against a real server: blowing it recalls the target dog,
 * sneaking cycles which dog it points at, and the target survives on the stack.
 */
public final class DogWhistleGameTest implements FabricGameTest {

  private static final BlockPos PLAYER_CORNER = new BlockPos(1, 2, 1);
  private static final BlockPos FAR_CORNER = new BlockPos(5, 2, 5);
  private static final double RECALL_DISTANCE = 4.0;

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void blowingTheWhistleBringsTheTargetDogToTheOwner(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final UUID dogId = dog.getUuid();
    final ItemStack whistle = giveWhistle(owner, dogId);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    // A summon relocates by recreating the entity, so the pre-summon reference is a corpse.
    final Entity recalled = context.getWorld().getEntity(dogId);
    context.assertTrue(recalled != null, "The recalled dog should still exist in the world");
    context.assertTrue(
        recalled.squaredDistanceTo(owner) <= RECALL_DISTANCE * RECALL_DISTANCE,
        "Blowing the whistle should bring the dog to its owner, but it was "
            + Math.sqrt(recalled.squaredDistanceTo(owner))
            + " blocks away");
    context.assertTrue(
        dogId.equals(whistle.get(ModComponents.WHISTLE_TARGET_PET)),
        "The whistle should still be pointed at the dog it just recalled");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void blowingAWhistleWithNoTargetRecallsTheNearestDog(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner, null);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getUuid().equals(whistle.get(ModComponents.WHISTLE_TARGET_PET)),
        "A whistle with no stored target should adopt the dog it called");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void blowingTheWhistleStartsTheCooldown(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    giveWhistle(owner, null);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        owner.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE),
        "A blown whistle should be on cooldown so repeat clicks cannot re-summon every tick");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void sneakingCyclesTheTargetToAnotherDog(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity first =
        spawnRegisteredDog(context, owner, new BlockPos(3, 2, 1), "Scout");
    final UnleashedDogEntity second =
        spawnRegisteredDog(context, owner, new BlockPos(5, 2, 1), "Bailey");
    final ItemStack whistle = giveWhistle(owner, first.getUuid());
    owner.setSneaking(true);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    final UUID target = whistle.get(ModComponents.WHISTLE_TARGET_PET);
    context.assertTrue(
        second.getUuid().equals(target) || first.getUuid().equals(target),
        "Cycling should land on one of the owner's dogs, but the target was " + target);
    context.assertTrue(
        !first.getUuid().equals(target),
        "Cycling should move the target off the dog it started on");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void sneakingDoesNotRecallTheDog(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final BlockPos before = dog.getBlockPos();
    giveWhistle(owner, dog.getUuid());
    owner.setSneaking(true);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        before.equals(dog.getBlockPos()),
        "Choosing a target should not also summon it, but the dog moved to " + dog.getBlockPos());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void aWhistleWithNoPetsLeavesItsTargetUnset(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final ItemStack whistle = giveWhistle(owner, null);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        whistle.get(ModComponents.WHISTLE_TARGET_PET) == null,
        "An owner with no dogs should leave the whistle unpointed");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void theTargetSurvivesAStackCopy(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner, dog.getUuid());

    final ItemStack copy = whistle.copy();

    context.assertTrue(
        dog.getUuid().equals(copy.get(ModComponents.WHISTLE_TARGET_PET)),
        "The target component should travel with the stack, but the copy held "
            + copy.get(ModComponents.WHISTLE_TARGET_PET));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void aDeceasedTargetIsReplacedOnTheNextBlow(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity alive = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final UUID deceasedId = UUID.randomUUID();
    registerRecord(context, owner, deceasedId, FAR_CORNER, "Ghost", false);
    final ItemStack whistle = giveWhistle(owner, deceasedId);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        alive.getUuid().equals(whistle.get(ModComponents.WHISTLE_TARGET_PET)),
        "A whistle pointed at a dog that has died should move to a living one");
    context.complete();
  }

  private static ServerPlayerEntity placeOwner(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absolute = context.getAbsolutePos(PLAYER_CORNER);
    owner.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    return owner;
  }

  private static ItemStack giveWhistle(ServerPlayerEntity owner, UUID target) {
    final ItemStack whistle = new ItemStack(ModItems.DOG_WHISTLE);
    if (target != null) {
      whistle.set(ModComponents.WHISTLE_TARGET_PET, target);
    }
    owner.setStackInHand(Hand.MAIN_HAND, whistle);
    owner.getItemCooldownManager().remove(ModItems.DOG_WHISTLE);
    return whistle;
  }

  private static UnleashedDogEntity spawnRegisteredDog(
      TestContext context, ServerPlayerEntity owner, BlockPos relativePos, String name) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, relativePos);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    registerRecord(context, owner, dog.getUuid(), relativePos, name, true);
    return dog;
  }

  private static void registerRecord(
      TestContext context,
      ServerPlayerEntity owner,
      UUID petId,
      BlockPos relativePos,
      String name,
      boolean alive) {
    final ServerWorld world = context.getWorld();
    PetManager.get(world.getServer())
        .registerPet(
            new PetData(
                petId,
                owner.getUuid(),
                UnleashedDogBreed.HUSKY,
                name,
                10.0f,
                10.0f,
                context.getAbsolutePos(relativePos),
                world.getRegistryKey().getValue().toString(),
                alive));
  }
}
