package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class DogWhistleGameTest implements FabricGameTest {

  private static final BlockPos PLAYER_CORNER = new BlockPos(1, 2, 1);
  private static final BlockPos FAR_CORNER = new BlockPos(5, 2, 5);
  private static final double RECALL_DISTANCE = 4.0;

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void rightClickingYourDogBindsTheWhistleToIt(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner);

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.getUuid().equals(whistle.get(ModComponents.WHISTLE_TARGET_PET)),
        "The whistle should hold the dog it was clicked on, but held "
            + whistle.get(ModComponents.WHISTLE_TARGET_PET));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void bindingEngravesTheDogsNameForTheTooltip(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner);

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        "Scout".equals(whistle.get(ModComponents.WHISTLE_TARGET_NAME)),
        "The tooltip name should be the dog's, but was "
            + whistle.get(ModComponents.WHISTLE_TARGET_NAME));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void rightClickingAnotherDogMovesTheBinding(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity first =
        spawnRegisteredDog(context, owner, new BlockPos(3, 2, 1), "Scout");
    final UnleashedDogEntity second =
        spawnRegisteredDog(context, owner, new BlockPos(5, 2, 1), "Bailey");
    final ItemStack whistle = giveWhistle(owner);

    first.interactMob(owner, Hand.MAIN_HAND);
    second.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        second.getUuid().equals(whistle.get(ModComponents.WHISTLE_TARGET_PET)),
        "Binding a second dog should replace the first");
    context.assertTrue(
        "Bailey".equals(whistle.get(ModComponents.WHISTLE_TARGET_NAME)),
        "The engraved name should follow the new binding, but was "
            + whistle.get(ModComponents.WHISTLE_TARGET_NAME));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void blowingTheWhistleBringsTheBoundDogToTheOwner(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final UUID dogId = dog.getUuid();
    giveWhistle(owner);
    dog.interactMob(owner, Hand.MAIN_HAND);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    final Entity recalled = context.getWorld().getEntity(dogId);
    context.assertTrue(recalled != null, "The recalled dog should still exist in the world");
    context.assertTrue(
        recalled.squaredDistanceTo(owner) <= RECALL_DISTANCE * RECALL_DISTANCE,
        "Blowing the whistle should bring the bound dog to its owner, but it was "
            + Math.sqrt(recalled.squaredDistanceTo(owner))
            + " blocks away");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void blowingTheWhistleStartsTheCooldown(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    giveWhistle(owner);
    dog.interactMob(owner, Hand.MAIN_HAND);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        owner.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE),
        "A blown whistle should be on cooldown so repeat clicks cannot re-summon every tick");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void anUnboundWhistleSummonsNothing(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final BlockPos before = dog.getBlockPos();
    giveWhistle(owner);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        before.equals(dog.getBlockPos()),
        "An unbound whistle must not move a dog, but it went to " + dog.getBlockPos());
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void aWhistleCannotBindToSomeoneElsesDog(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final ServerPlayerEntity stranger = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog = spawnRegisteredDog(context, stranger, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner);

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        whistle.get(ModComponents.WHISTLE_TARGET_PET) == null,
        "Another player's dog must not answer your whistle");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void aBoundWhistleThatChangesHandsKeepsItsBinding(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final ItemStack whistle = giveWhistle(owner);
    dog.interactMob(owner, Hand.MAIN_HAND);

    final ItemStack copy = whistle.copy();

    context.assertTrue(
        dog.getUuid().equals(copy.get(ModComponents.WHISTLE_TARGET_PET))
            && "Scout".equals(copy.get(ModComponents.WHISTLE_TARGET_NAME)),
        "The binding should travel with the stack, but the copy held "
            + copy.get(ModComponents.WHISTLE_TARGET_NAME));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-whistle", tickLimit = 40)
  public void aWhistleBoundToADeceasedDogSummonsNothing(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnRegisteredDog(context, owner, FAR_CORNER, "Scout");
    final BlockPos before = dog.getBlockPos();
    giveWhistle(owner);
    dog.interactMob(owner, Hand.MAIN_HAND);
    PetManager.get(context.getWorld().getServer()).markPetDeceased(dog.getUuid(), false);

    ModItems.DOG_WHISTLE.use(context.getWorld(), owner, Hand.MAIN_HAND);

    context.assertTrue(
        before.equals(dog.getBlockPos()),
        "A whistle whose dog is recorded dead must not summon, but it went to "
            + dog.getBlockPos());
    context.complete();
  }

  private static ServerPlayerEntity placeOwner(TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absolute = context.getAbsolutePos(PLAYER_CORNER);
    owner.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    return owner;
  }

  private static ItemStack giveWhistle(ServerPlayerEntity owner) {
    final ItemStack whistle = new ItemStack(ModItems.DOG_WHISTLE);
    owner.setStackInHand(Hand.MAIN_HAND, whistle);
    owner.getItemCooldownManager().remove(ModItems.DOG_WHISTLE);
    return whistle;
  }

  private static UnleashedDogEntity spawnRegisteredDog(
      TestContext context, ServerPlayerEntity owner, BlockPos relativePos, String name) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, relativePos);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setCustomName(Text.literal(name));
    PetManager.get(world.getServer())
        .registerPet(
            new PetData(
                dog.getUuid(),
                owner.getUuid(),
                UnleashedDogBreed.HUSKY,
                name,
                10.0f,
                10.0f,
                context.getAbsolutePos(relativePos),
                world.getRegistryKey().getValue().toString(),
                PetLifeState.LIVING));
    return dog;
  }
}
