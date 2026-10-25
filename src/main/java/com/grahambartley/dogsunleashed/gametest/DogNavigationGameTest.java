package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogHouseTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import com.grahambartley.dogsunleashed.item.WhistlePointTarget;
import com.grahambartley.dogsunleashed.item.WhistlePointing;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogNavigationGameTest implements FabricGameTest {

  private static final String YARD = "dogs-unleashed:dog_yard";
  private static final String BATCH = "dog-navigation";
  private static final int TICK_LIMIT = 600;
  private static final int FLOOR_Y = 2;
  private static final int WALL_TOP_Y = 4;
  private static final int ROOM_MIN = 2;
  private static final int ROOM_MAX_X = 10;
  private static final int ROOM_MAX_Z = 16;
  private static final BlockPos REL_DOORWAY = new BlockPos(ROOM_MIN, FLOOR_Y, 9);
  private static final BlockPos REL_DOG = new BlockPos(8, FLOOR_Y, 9);
  private static final BlockPos REL_OWNER = new BlockPos(4, FLOOR_Y, 5);
  private static final BlockPos REL_BED = new BlockPos(13, FLOOR_Y, 9);
  private static final BlockPos REL_HOUSE = new BlockPos(14, FLOOR_Y, 8);
  private static final BlockPos REL_GARDEN_FLOOR = new BlockPos(13, FLOOR_Y - 1, 9);

  private record SleepSpot(String name, BlockPos relPos, Consumer<TestContext> placer) {}

  private static final List<SleepSpot> SPOTS =
      List.of(
          new SleepSpot(
              "bed",
              REL_BED,
              context -> context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState())),
          new SleepSpot(
              "house",
              REL_HOUSE,
              context -> DogHouseTestHelper.placeHouse(context, REL_HOUSE, Direction.EAST)));

  @BeforeBatch(batchId = BATCH)
  public void beforeBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    DogSleepSpotAssignment.clearPendingAssignments();
    SleepBatchFixture.pinNight(world);
  }

  @AfterBatch(batchId = BATCH)
  public void afterBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @CustomTestProvider
  public List<TestFunction> goToBedWalksOutOfTheRoomAndRoundToTheSpot() {
    return perSpot(
        "gotobedwalksroundtothespot",
        (context, spot) -> {
          final UnleashedDogEntity dog = dogInRoom(context, spot);
          context.runAtTick(
              5,
              () ->
                  context.assertTrue(
                      dog.getSleepController()
                          .commandToSleep(context.getAbsolutePos(spot.relPos())),
                      "The dog refused Go to Bed at night"));
          expectAsleepBeforeTheEnd(context, dog, spot);
        });
  }

  @CustomTestProvider
  public List<TestFunction> autoSleepWalksOutOfTheRoomAndRoundToTheSpot() {
    return perSpot(
        "autosleepwalksroundtothespot",
        (context, spot) -> {
          final UnleashedDogEntity dog = dogInRoom(context, spot);
          dog.getSleepController().setAssignedBedPos(context.getAbsolutePos(spot.relPos()));
          expectAsleepBeforeTheEnd(context, dog, spot);
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWhistledDogWalksOutOfTheRoomAndRoundToTheSpot(final TestContext context) {
    final UnleashedDogEntity dog = dogInRoom(context);
    final Optional<Vec3d> marker =
        WhistlePointing.order(
            dog,
            WhistlePointTarget.atBlock(context.getAbsolutePos(REL_GARDEN_FLOOR), Direction.UP));
    context.assertTrue(marker.isPresent(), "The whistle found no spot beside the garden floor");
    final BlockPos spot = dog.getCommandController().getAnchorPos();
    context.runAtEveryTick(
        () -> {
          if (dog.getBlockPos().getSquaredDistance(spot) <= 1.0
              && !dog.getCommandController().isHeadingToAnchor()) {
            context.complete();
          }
        });
    context.runAtTick(
        TICK_LIMIT - 1,
        () ->
            context.assertTrue(
                dog.getBlockPos().getSquaredDistance(spot) <= 1.0,
                "The whistled dog never walked out of the room and round to its spot, it stopped at "
                    + dog.getBlockPos().subtract(context.getAbsolutePos(BlockPos.ORIGIN))));
  }

  private static UnleashedDogEntity dogInRoom(final TestContext context, final SleepSpot spot) {
    spot.placer().accept(context);
    return dogInRoom(context);
  }

  private static UnleashedDogEntity dogInRoom(final TestContext context) {
    buildRoom(context);
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final Vec3d ownerPos = Vec3d.ofBottomCenter(context.getAbsolutePos(REL_OWNER));
    owner.refreshPositionAndAngles(ownerPos.x, ownerPos.y, ownerPos.z, 0.0f, 0.0f);
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, REL_DOG);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setInvulnerable(true);
    return dog;
  }

  private static void buildRoom(final TestContext context) {
    for (int y = FLOOR_Y; y <= WALL_TOP_Y; y++) {
      for (int z = ROOM_MIN; z <= ROOM_MAX_Z; z++) {
        context.setBlockState(new BlockPos(ROOM_MIN, y, z), Blocks.STONE);
        context.setBlockState(new BlockPos(ROOM_MAX_X, y, z), Blocks.STONE);
      }
      for (int x = ROOM_MIN; x <= ROOM_MAX_X; x++) {
        context.setBlockState(new BlockPos(x, y, ROOM_MIN), Blocks.STONE);
        context.setBlockState(new BlockPos(x, y, ROOM_MAX_Z), Blocks.STONE);
      }
    }
    context.setBlockState(REL_DOORWAY, Blocks.AIR);
    context.setBlockState(REL_DOORWAY.up(), Blocks.AIR);
  }

  private static void expectAsleepBeforeTheEnd(
      final TestContext context, final UnleashedDogEntity dog, final SleepSpot spot) {
    context.runAtEveryTick(
        () -> {
          if (dog.getSleepController().isSleepingInBed()) {
            context.complete();
          }
        });
    context.runAtTick(
        TICK_LIMIT - 1,
        () ->
            context.assertTrue(
                dog.getSleepController().isSleepingInBed(),
                "The dog never walked out of the room and round to its "
                    + spot.name()
                    + ", it stopped at "
                    + dog.getBlockPos().subtract(context.getAbsolutePos(BlockPos.ORIGIN))));
  }

  private static List<TestFunction> perSpot(
      final String testName, final BiConsumer<TestContext, SleepSpot> body) {
    return SPOTS.stream()
        .map(
            spot ->
                GeneratedGameTest.of(
                    BATCH,
                    "dognavigationgametest." + testName + "." + spot.name(),
                    YARD,
                    TICK_LIMIT,
                    context -> body.accept(context, spot)))
        .toList();
  }
}
