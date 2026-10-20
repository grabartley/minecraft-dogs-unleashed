package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogScent;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.scent.ScentLandmark;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class DogScentHandoffGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-scent-handoff";
  private static final BlockPos OWNER_SPOT = new BlockPos(1, 2, 3);
  private static final BlockPos DOG_START = new BlockPos(1, 2, 1);
  private static final BlockPos MARK = new BlockPos(5, 2, 5);

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300, skyAccess = true)
  public void aTreasureMapLeadsTheDogToDigAtTheMark(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final BlockPos mark = context.getAbsolutePos(MARK);

    give(dog, owner, treasureMap(mark));

    context.runAtEveryTick(
        () -> {
          if (scent(dog).phase() == ScentPhase.DIGGING
              && dog.getBlockX() == mark.getX()
              && dog.getBlockZ() == mark.getZ()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 200)
  public void aFarMarkStillSetsTheDogWalking(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    final BlockPos mark = context.getAbsolutePos(DOG_START).add(1500, 0, 1500);
    final double start = Math.hypot(mark.getX() - dog.getX(), mark.getZ() - dog.getZ());

    give(dog, owner, treasureMap(mark));

    context.runAtEveryTick(
        () -> {
          if (Math.hypot(mark.getX() - dog.getX(), mark.getZ() - dog.getZ()) <= start - 2.0) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aMapWithoutAMarkIsNotAScent(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, owner, new ItemStack(Items.FILLED_MAP));

    context.assertFalse(scent(dog).isTracking(), "A map with no marker has nothing to follow");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void givingTheMapAgainStopsTracking(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);
    final ItemStack map = treasureMap(context.getAbsolutePos(MARK));

    give(dog, owner, map);
    context.assertTrue(scent(dog).isTracking(), "A treasure map should start tracking");
    give(dog, owner, map);

    context.assertFalse(scent(dog).isTracking(), "Giving the map again should stop it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aMapWithAnotherMarkSendsTheDogThereInstead(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);
    final BlockPos first = context.getAbsolutePos(MARK);
    final BlockPos second = first.add(40, 0, -25);

    give(dog, owner, treasureMap(first));
    give(dog, owner, treasureMap(second));

    context.assertTrue(scent(dog).isTracking(), "A map with a new mark should keep the dog going");
    context.assertTrue(
        new ScentLandmark(second.getX(), second.getZ()).equals(scent(dog).landmark()),
        "The dog should head for the new mark, not " + scent(dog).landmark());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aStrangerCannotSendSomeoneElsesDog(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final ServerPlayerEntity stranger = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);

    give(dog, stranger, treasureMap(context.getAbsolutePos(MARK)));

    context.assertFalse(scent(dog).isTracking(), "Another player's dog must ignore their map");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aMapMarkSurvivesASaveAndLoad(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);
    dog.setAiDisabled(true);
    final BlockPos mark = context.getAbsolutePos(MARK);
    give(dog, owner, treasureMap(mark));

    final NbtCompound saved = new NbtCompound();
    dog.writeCustomDataToNbt(saved);
    final UnleashedDogEntity loaded = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    loaded.setAiDisabled(true);
    loaded.readCustomDataFromNbt(saved);

    context.assertTrue(scent(loaded).isTracking(), "A saved map scent should load back");
    context.assertTrue(
        new ScentLandmark(mark.getX(), mark.getZ()).equals(scent(loaded).landmark()),
        "The loaded dog should head for the same mark, not " + scent(loaded).landmark());
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 100)
  public void aSherdWithNoRuinsNearbyGivesUp(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner);

    give(dog, owner, new ItemStack(Items.HOWL_POTTERY_SHERD));
    context.assertTrue(scent(dog).isTracking(), "A trail ruins sherd should start a scent");

    context.runAtEveryTick(
        () -> {
          if (!scent(dog).isTracking()) {
            context.complete();
          }
        });
  }

  private static ItemStack treasureMap(final BlockPos mark) {
    final ItemStack map = new ItemStack(Items.FILLED_MAP);
    map.set(
        DataComponentTypes.MAP_DECORATIONS,
        MapDecorationsComponent.DEFAULT.with(
            "+",
            new MapDecorationsComponent.Decoration(
                MapDecorationTypes.RED_X, mark.getX(), mark.getZ(), 180.0F)));
    return map;
  }

  private static void give(
      final UnleashedDogEntity dog, final ServerPlayerEntity player, final ItemStack stack) {
    player.setStackInHand(Hand.MAIN_HAND, stack);
    dog.interactMob(player, Hand.MAIN_HAND);
  }

  private static ServerPlayerEntity placeOwner(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absolute = context.getAbsolutePos(OWNER_SPOT);
    player.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    return player;
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    return dog;
  }

  private static DogScent scent(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogScent.class);
  }
}
