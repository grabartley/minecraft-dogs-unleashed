package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public final class DogDimensionChangeGameTest implements FabricGameTest {

  private static final String BATCH = "dog-dimension-change";
  private static final BlockPos NETHER_LANDING = new BlockPos(3, 70, -5);

  private static UnleashedDogEntity spawnOwnedDog(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(
            context, DogTestData.HUSKY, new BlockPos(0, 1, 0), owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }

  private static UnleashedDogEntity moveTo(
      final TestContext context,
      final UnleashedDogEntity dog,
      final RegistryKey<World> dimension,
      final BlockPos landing) {
    final ServerWorld destination = context.getWorld().getServer().getWorld(dimension);
    final UnleashedDogEntity moved =
        (UnleashedDogEntity)
            dog.teleportTo(
                new TeleportTarget(
                    destination,
                    Vec3d.ofBottomCenter(landing),
                    Vec3d.ZERO,
                    0f,
                    0f,
                    TeleportTarget.NO_OP));
    context.assertTrue(
        moved != null && moved.getWorld().getRegistryKey() == dimension,
        "The dog should have moved to " + dimension.getValue());
    moved.setAiDisabled(true);
    return moved;
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void dogInAnotherDimensionHasNoBedToSleepIn(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    dog.getSleepController().setAssignedBedPos(context.getAbsolutePos(new BlockPos(0, 1, 0)));

    final UnleashedDogEntity inNether = moveTo(context, dog, World.NETHER, NETHER_LANDING);
    final Optional<BlockPos> bed = inNether.getSleepController().getAssignedBedPos();
    inNether.discard();

    context.assertTrue(
        bed.isEmpty(), "A dog in the Nether should not see its Overworld bed, bed=" + bed);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void dogBackInItsBedsDimensionFindsTheBedAgain(final TestContext context) {
    final BlockPos absBedPos = context.getAbsolutePos(new BlockPos(0, 1, 0));
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    dog.getSleepController().setAssignedBedPos(absBedPos);

    final UnleashedDogEntity inNether = moveTo(context, dog, World.NETHER, NETHER_LANDING);
    final UnleashedDogEntity home = moveTo(context, inNether, World.OVERWORLD, absBedPos);
    final Optional<BlockPos> bed = home.getSleepController().getAssignedBedPos();
    home.discard();

    context.assertTrue(
        bed.equals(Optional.of(absBedPos)),
        "A dog back in the Overworld should find its bed again, bed=" + bed);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void stayingDogMovedToAnotherDimensionStaysWhereItLands(final TestContext context) {
    assertAnchorsWhereItLands(context, DogCommand.STAY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void guardingDogMovedToAnotherDimensionGuardsWhereItLands(final TestContext context) {
    assertAnchorsWhereItLands(context, DogCommand.GUARD);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void saveWithoutDimensionsKeepsBedAndStaySpotWhereTheDogLoads(final TestContext context) {
    final BlockPos absBedPos = context.getAbsolutePos(new BlockPos(0, 1, 0));
    final UnleashedDogEntity source = spawnOwnedDog(context);
    source.getSleepController().setAssignedBedPos(absBedPos);
    source.getCommandController().apply(DogCommand.STAY);
    final BlockPos anchor = source.getCommandController().getAnchorPos();
    final NbtCompound nbt = new NbtCompound();
    source.writeCustomDataToNbt(nbt);
    nbt.remove(ModNbtKeys.BED_DIMENSION);
    nbt.remove(ModNbtKeys.COMMAND_ANCHOR_DIMENSION);

    final UnleashedDogEntity restored = spawnOwnedDog(context);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.getSleepController().getAssignedBedPos().equals(Optional.of(absBedPos)),
        "An old save should keep its bed, bed="
            + restored.getSleepController().getAssignedBedPos());
    context.assertTrue(
        anchor != null && anchor.equals(restored.getCommandController().getAnchorPos()),
        "An old save should keep its stay spot, anchor="
            + restored.getCommandController().getAnchorPos());
    context.complete();
  }

  private static void assertAnchorsWhereItLands(
      final TestContext context, final DogCommand command) {
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    dog.getCommandController().apply(command);

    final UnleashedDogEntity inNether = moveTo(context, dog, World.NETHER, NETHER_LANDING);
    final BlockPos anchor = inNether.getCommandController().getAnchorPos();
    inNether.discard();

    context.assertTrue(
        NETHER_LANDING.equals(anchor),
        command + " should anchor where the dog landed, anchor=" + anchor);
    context.complete();
  }
}
