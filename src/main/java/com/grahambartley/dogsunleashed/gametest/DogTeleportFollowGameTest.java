package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Enforces that active tamed dogs follow their owner across long-distance teleports within one
 * world (issue #259), while sitting and sleeping dogs stay behind and short teleports change
 * nothing. Runs the real {@code ServerPlayerEntity.teleport} path so the mixin wiring is covered.
 */
public final class DogTeleportFollowGameTest implements FabricGameTest {

  private static final String TELEPORT_ARENA = "dogs-unleashed:teleport_arena";
  private static final Vec3d LONG_TELEPORT_START = new Vec3d(2.5, 1, 2.5);
  private static final Vec3d LONG_TELEPORT_DESTINATION = new Vec3d(20.5, 1, 2.5);
  private static final BlockPos DOG_START = new BlockPos(2, 1, 2);
  private static final long TELEPORT_TICK = 5;
  private static final long ASSERT_TICK = 25;

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void longDistanceTeleportBringsActiveDogToOwner(TestContext context) {
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final double distance = Math.sqrt(husky.squaredDistanceTo(owner));
          context.assertTrue(
              distance <= 4.0,
              "Active dog should be beside its owner after a long-distance teleport, but was "
                  + distance
                  + " blocks away");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void longDistanceTeleportPlacesDogClearOfBlocks(TestContext context) {
    // The owner lands inside a solid block, mirroring the report of dogs spawning in the floor.
    // The dog must still be placed on a safe neighbouring position, never inside the block.
    context.setBlockState(new BlockPos(20, 1, 2), Blocks.STONE.getDefaultState());
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.squaredDistanceTo(owner) <= 16.0,
              "Dog should have been brought to the owner before the placement check");
          context.assertFalse(
              context
                  .getWorld()
                  .getBlockCollisions(husky, husky.getBoundingBox())
                  .iterator()
                  .hasNext(),
              "Brought dog must not intersect any solid block");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void shortDistanceTeleportLeavesDogInPlace(TestContext context) {
    final ServerPlayerEntity owner = placePlayer(context, new Vec3d(8.5, 1, 2.5));
    final HuskyEntity husky = spawnRegisteredDog(context, owner, new BlockPos(1, 1, 2));
    final Vec3d dogStartPos = husky.getPos();

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, new Vec3d(14.5, 1, 2.5)));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.getPos().squaredDistanceTo(dogStartPos) < 0.01,
              "Dog should not move for a teleport under the long-distance threshold");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void sittingDogStaysBehindOnLongDistanceTeleport(TestContext context) {
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);
    husky.setInSittingPose(true);
    final Vec3d dogStartPos = husky.getPos();

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.getPos().squaredDistanceTo(dogStartPos) < 0.01,
              "Sitting dog should stay behind when its owner teleports away");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void sleepingDogStaysBehindOnLongDistanceTeleport(TestContext context) {
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);
    husky.startSleepingInBed(context.getAbsolutePos(DOG_START));
    final Vec3d dogStartPos = husky.getPos();

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.getPos().squaredDistanceTo(dogStartPos) < 0.01,
              "Sleeping dog should stay behind when its owner teleports away");
          context.complete();
        });
  }

  private static ServerPlayerEntity placePlayer(TestContext context, Vec3d relativePos) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d absolutePos = context.getAbsolute(relativePos);
    player.refreshPositionAndAngles(absolutePos.x, absolutePos.y, absolutePos.z, 0.0F, 0.0F);
    return player;
  }

  private static HuskyEntity spawnRegisteredDog(
      TestContext context, ServerPlayerEntity owner, BlockPos relativePos) {
    final HuskyEntity husky = context.spawnEntity(ModEntities.HUSKY, relativePos);
    husky.setAiDisabled(true);
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());

    final ServerWorld world = context.getWorld();
    final PetData petData =
        new PetData(
            husky.getUuid(),
            owner.getUuid(),
            husky.getBreed(),
            "Rex",
            husky.getHealth(),
            husky.getMaxHealth(),
            husky.getBlockPos(),
            world.getRegistryKey().getValue().toString(),
            true);
    PetManager.get(world.getServer()).registerPet(petData);
    return husky;
  }

  private static void teleport(TestContext context, ServerPlayerEntity player, Vec3d relativePos) {
    final Vec3d absolutePos = context.getAbsolute(relativePos);
    player.teleport(
        context.getWorld(),
        absolutePos.x,
        absolutePos.y,
        absolutePos.z,
        Set.of(),
        player.getYaw(),
        player.getPitch());
  }
}
