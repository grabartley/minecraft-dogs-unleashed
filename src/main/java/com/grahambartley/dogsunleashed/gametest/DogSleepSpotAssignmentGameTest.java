package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogHouseTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
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
  private static final int TICK_LIMIT = 40;
  private static final float LETHAL_DAMAGE = 999.0f;
  private static final BlockPos REL_SPOT = new BlockPos(2, 2, 2);
  private static final BlockPos REL_OTHER_BED = new BlockPos(0, 2, 0);
  private static final BlockPos REL_FIRST_DOG = new BlockPos(1, 2, 5);
  private static final BlockPos REL_SECOND_DOG = new BlockPos(5, 2, 5);

  private record SleepSpot(String name, Consumer<TestContext> placer) {}

  private static final List<SleepSpot> SPOTS =
      List.of(
          new SleepSpot(
              "bed",
              context -> context.setBlockState(REL_SPOT, ModBlocks.DOG_BED.getDefaultState())),
          new SleepSpot(
              "house",
              context -> DogHouseTestHelper.placeHouse(context, REL_SPOT, Direction.NORTH)));

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
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity first = tamedDog(context, owner, REL_FIRST_DOG);
          final UnleashedDogEntity second = tamedDog(context, owner, REL_SECOND_DOG);

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
  public List<TestFunction> assigningASecondDogWakesTheFirst() {
    return perSpot(
        "assigningaseconddogwakesthefirst",
        (context, spot) -> {
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity first = tamedDog(context, owner, REL_FIRST_DOG);
          final UnleashedDogEntity second = tamedDog(context, owner, REL_SECOND_DOG);
          assign(context, owner, first);
          first.getSleepController().startSleepingInBed(context.getAbsolutePos(REL_SPOT));

          assign(context, owner, second);

          context.assertFalse(
              first.getSleepController().isSleepingInBed(),
              "The first dog kept sleeping in the " + spot.name() + " it lost");
          context.complete();
        });
  }

  @CustomTestProvider
  public List<TestFunction> aStaleDogGivesUpATakenSpot() {
    return perSpot(
        "astaledoggivesupatakenspot",
        (context, spot) -> {
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity stale = staleDogBesideCurrent(context, owner);

          context.assertTrue(
              stale.getSleepController().releaseSpotIfTaken(context.getAbsolutePos(REL_SPOT)),
              "A dog kept its claim on the " + spot.name() + " another dog holds");
          context.assertTrue(
              stale.getSleepController().getAssignedBedPos().isEmpty(),
              "A dog kept its link to the " + spot.name() + " another dog holds");
          context.complete();
        });
  }

  @CustomTestProvider
  public List<TestFunction> aStaleDogAsleepInATakenSpotWakesAndLetsGo() {
    return perSpot(
        "astaledogasleepinatakenspotwakesandletsgo",
        (context, spot) -> {
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity stale = staleDogBesideCurrent(context, owner);
          stale.getSleepController().startSleepingInBed(context.getAbsolutePos(REL_SPOT));
          stale.setAiDisabled(false);

          context.runAtEveryTick(
              () -> {
                if (!stale.getSleepController().isSleepingInBed()
                    && stale.getSleepController().getAssignedBedPos().isEmpty()) {
                  context.complete();
                }
              });
        });
  }

  @CustomTestProvider
  public List<TestFunction> aStaleDogMovingElsewhereKeepsTheNewDogsSpot() {
    return perSpot(
        "astaledogmovingelsewherekeepsthenewdogsspot",
        (context, spot) -> {
          context.setBlockState(REL_OTHER_BED, ModBlocks.DOG_BED.getDefaultState());
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity stale = staleDogBesideCurrent(context, owner);
          final UUID current = holder(context).getAssignedDogUuid();

          DogSleepSpotAssignment.setPendingAssignment(owner.getUuid(), stale.getUuid());
          context.useBlock(REL_OTHER_BED, owner);

          context.assertTrue(
              current.equals(holder(context).getAssignedDogUuid()),
              "Moving a stale dog to another bed emptied the " + spot.name());
          context.complete();
        });
  }

  @CustomTestProvider
  public List<TestFunction> deathFreesTheSpot() {
    return perSpot(
        "deathfreesthespot",
        (context, spot) -> {
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity dog = tamedDog(context, owner, REL_FIRST_DOG);
          assign(context, owner, dog);

          DogTestHelper.damageEntity(dog, LETHAL_DAMAGE);

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
          final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
          final UnleashedDogEntity stale = staleDogBesideCurrent(context, owner);
          final UUID current = holder(context).getAssignedDogUuid();

          DogTestHelper.damageEntity(stale, LETHAL_DAMAGE);

          context.assertTrue(
              current.equals(holder(context).getAssignedDogUuid()),
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
                GeneratedGameTest.of(
                    BATCH,
                    "dogsleepspotassignmentgametest." + behaviour + "." + spot.name(),
                    ARENA,
                    TICK_LIMIT,
                    context -> {
                      spot.placer().accept(context);
                      body.run(context, spot);
                    }))
        .toList();
  }

  private static UnleashedDogEntity staleDogBesideCurrent(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity stale = tamedDog(context, owner, REL_FIRST_DOG);
    final UnleashedDogEntity current = tamedDog(context, owner, REL_SECOND_DOG);
    assign(context, owner, current);
    stale.getSleepController().setAssignedBedPos(context.getAbsolutePos(REL_SPOT));
    return stale;
  }

  private static UnleashedDogEntity tamedDog(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos relPos) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, relPos, owner.getUuid());
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
