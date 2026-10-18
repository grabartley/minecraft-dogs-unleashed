package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class DogSleepSpotAssignmentGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "sleep-spot-assignment";
  private static final BlockPos REL_SPOT = new BlockPos(2, 2, 2);
  private static final Direction FACING = Direction.NORTH;

  private record SleepSpot(String name, Consumer<TestContext> placer) {}

  private static final List<SleepSpot> SPOTS =
      List.of(
          new SleepSpot(
              "bed",
              context -> context.setBlockState(REL_SPOT, ModBlocks.DOG_BED.getDefaultState())),
          new SleepSpot("house", DogSleepSpotAssignmentGameTest::placeHouse));

  @BeforeBatch(batchId = BATCH)
  public void beforeBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @AfterBatch(batchId = BATCH)
  public void afterBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @CustomTestProvider
  public List<TestFunction> assigningASecondDogClearsTheFirstDogsLink() {
    return perSpot(
        "assigningaseconddogclearsthefirst",
        (context, spot) -> {
          spot.placer().accept(context);
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity first = tamedDog(context, owner, new BlockPos(1, 2, 4));
          final UnleashedDogEntity second = tamedDog(context, owner, new BlockPos(3, 2, 4));

          assign(context, owner, first);
          assign(context, owner, second);

          context.assertTrue(
              first.getSleepController().getAssignedBedPos().isEmpty(),
              "The first dog still points at the " + spot.name() + " another dog now owns");
          context.assertTrue(
              context
                  .getAbsolutePos(REL_SPOT)
                  .equals(second.getSleepController().getAssignedBedPos().orElse(null)),
              "The second dog was not assigned to the " + spot.name());
          context.assertTrue(
              second.getUuid().equals(holder(context).getAssignedDogUuid()),
              "The " + spot.name() + " does not hold the second dog");
          context.complete();
        });
  }

  @CustomTestProvider
  public List<TestFunction> deathFreesTheSpot() {
    return perSpot(
        "deathfreesthespot",
        (context, spot) -> {
          spot.placer().accept(context);
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity dog = tamedDog(context, owner, new BlockPos(1, 2, 4));
          assign(context, owner, dog);

          dog.damage(context.getWorld().getDamageSources().generic(), 999.0f);

          context.assertFalse(
              holder(context).hasAssignedDog(), "The " + spot.name() + " kept its dead dog");
          context.complete();
        });
  }

  @CustomTestProvider
  public List<TestFunction> aStaleDogsDeathKeepsTheNewDogsSpot() {
    return perSpot(
        "astaledogsdeathkeepsthenewdogsspot",
        (context, spot) -> {
          spot.placer().accept(context);
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity stale = tamedDog(context, owner, new BlockPos(1, 2, 4));
          final UnleashedDogEntity current = tamedDog(context, owner, new BlockPos(3, 2, 4));
          assign(context, owner, current);
          stale.getSleepController().setAssignedBedPos(context.getAbsolutePos(REL_SPOT));

          stale.damage(context.getWorld().getDamageSources().generic(), 999.0f);

          context.assertTrue(
              current.getUuid().equals(holder(context).getAssignedDogUuid()),
              "A dog the " + spot.name() + " no longer holds cleared it when it died");
          context.complete();
        });
  }

  @FunctionalInterface
  private interface PerSpotBody {
    void run(TestContext context, SleepSpot spot);
  }

  private static List<TestFunction> perSpot(final String behaviour, final PerSpotBody body) {
    return SPOTS.stream()
        .map(
            spot ->
                new TestFunction(
                    BATCH,
                    "dogsleepspotassignmentgametest." + behaviour + "." + spot.name(),
                    ARENA,
                    40,
                    0L,
                    true,
                    context -> body.run(context, spot)))
        .toList();
  }

  private static void placeHouse(final TestContext context) {
    final BlockState origin =
        ModBlocks.DOG_HOUSE
            .getDefaultState()
            .with(DogHouseBlock.FACING, FACING)
            .with(DogHouseBlock.PART, DogHousePart.ORIGIN);
    for (final DogHousePart part : DogHousePart.values()) {
      context.setBlockState(
          REL_SPOT.add(DogHouseLayout.offsetFromOrigin(part, FACING)),
          origin.with(DogHouseBlock.PART, part));
    }
  }

  private static UnleashedDogEntity tamedDog(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos relPos) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, relPos);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static void assign(
      final TestContext context, final ServerPlayerEntity owner, final UnleashedDogEntity dog) {
    DogSleepSpotAssignment.setPendingAssignment(owner.getUuid(), dog.getUuid());
    context.useBlock(REL_SPOT, owner);
  }

  private static AssignedDogHolder holder(final TestContext context) {
    return context.getBlockEntity(REL_SPOT);
  }
}
