package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
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
 *
 * <p>The {@code teleport_arena} template floor sits at context-relative y1, so y2 is the standing
 * level and y2..y4 is the open interior.
 */
public final class DogTeleportFollowGameTest implements FabricGameTest {

  private static final String TELEPORT_ARENA = "dogs-unleashed:teleport_arena";
  private static final Vec3d LONG_TELEPORT_START = new Vec3d(2.5, 2, 2.5);
  private static final Vec3d LONG_TELEPORT_DESTINATION = new Vec3d(20.5, 2, 2.5);
  private static final BlockPos DOG_START = new BlockPos(2, 2, 2);
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
    // The owner lands with their feet inside a solid block, mirroring the report of dogs spawning
    // in the floor. The dog must be placed on a clear neighbouring position, never inside blocks.
    context.setBlockState(new BlockPos(20, 2, 2), Blocks.STONE.getDefaultState());
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
  public void longDistanceTeleportToAirborneOwnerGroundsDogBeneath(TestContext context) {
    // Mirrors an owner in creative flight: the teleport destination is mid-air, so the dog must
    // land on the ground beneath the owner rather than being skipped for lack of safe footing.
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, new Vec3d(20.5, 4, 2.5)));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final double horizontalDistance =
              Math.hypot(husky.getX() - owner.getX(), husky.getZ() - owner.getZ());
          context.assertTrue(
              horizontalDistance <= 4.0,
              "Dog should be beneath its airborne owner, but was "
                  + horizontalDistance
                  + " blocks away horizontally");
          final double standingY = context.getAbsolutePos(new BlockPos(0, 2, 0)).getY();
          context.assertTrue(
              Math.abs(husky.getY() - standingY) < 0.01,
              "Dog should stand on the arena floor, but was at y=" + husky.getY());
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void longDistanceTeleportBringsDogOntoSnowLayeredGround(TestContext context) {
    // Snow layers cover every block around the destination, as on any snowy-biome surface.
    // Partial-height ground cover is valid footing and must never strand the dog.
    coverDestinationFloor(context, Blocks.SNOW.getDefaultState());
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final double distance = Math.sqrt(husky.squaredDistanceTo(owner));
          context.assertTrue(
              distance <= 4.0,
              "Dog should be brought onto snow-layered ground beside its owner, but was "
                  + distance
                  + " blocks away");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void longDistanceTeleportBringsDogOntoSlabGround(TestContext context) {
    coverDestinationFloor(context, Blocks.STONE_SLAB.getDefaultState());
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          final double distance = Math.sqrt(husky.squaredDistanceTo(owner));
          context.assertTrue(
              distance <= 4.0,
              "Dog should be brought onto slab ground beside its owner, but was "
                  + distance
                  + " blocks away");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void teleportIntoSolidTerrainLeavesDogBehindAlive(TestContext context) {
    // The owner ends up fully buried, so there is no safe spot anywhere in summon range. The dog
    // must be left where it was instead of being placed inside blocks to suffocate.
    fillDestinationWithStone(context);
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);
    final Vec3d dogStartPos = husky.getPos();

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, LONG_TELEPORT_DESTINATION));

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.isAlive(), "Dog must survive its owner teleporting into solid terrain");
          context.assertTrue(
              husky.getPos().squaredDistanceTo(dogStartPos) < 0.01,
              "Dog should stay behind when there is no safe ground beside its owner");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void explicitSummonDeliversDogEvenWhenOwnerIsBuried(TestContext context) {
    // Pet Manager summons are an explicit order and must always deliver the dog. With the owner
    // fully buried there is no safe spot, so the dog arrives at the owner's own position.
    fillDestinationWithStone(context);
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_DESTINATION);
    final HuskyEntity husky = spawnRegisteredDog(context, owner, DOG_START);

    context.runAtTick(
        TELEPORT_TICK,
        () -> {
          final PetData petData =
              PetManager.get(context.getWorld().getServer()).getPetByEntityId(husky.getUuid());
          PetLocationService.loadAndSummon(context.getWorld().getServer(), petData, owner);
        });

    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              husky.isAlive(), "Summoned dog must still be alive beside its buried owner");
          context.assertTrue(
              husky.squaredDistanceTo(owner) <= 1.0,
              "Explicit summon must deliver the dog to the owner's position even with no safe"
                  + " ground, but it was "
                  + Math.sqrt(husky.squaredDistanceTo(owner))
                  + " blocks away");
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 60)
  public void summonOfUnfindablePetGivesUpWithoutSpawningAnything(TestContext context) {
    // A pet record whose entity no longer exists anywhere: the locate retry loop must exhaust
    // cleanly and never conjure an entity out of the stale record.
    final ServerPlayerEntity owner = placePlayer(context, LONG_TELEPORT_START);
    final PetData ghost =
        new PetData(
            UUID.randomUUID(),
            owner.getUuid(),
            UnleashedDogBreed.HUSKY,
            "Ghost",
            20.0f,
            20.0f,
            context.getAbsolutePos(DOG_START),
            context.getWorld().getRegistryKey().getValue().toString(),
            true);
    PetManager.get(context.getWorld().getServer()).registerPet(ghost);

    PetLocationService.loadAndSummon(context.getWorld().getServer(), ghost, owner);

    context.runAtTick(
        45,
        () -> {
          context.dontExpectEntity(ModEntities.HUSKY);
          context.complete();
        });
  }

  @GameTest(templateName = TELEPORT_ARENA, tickLimit = 40)
  public void shortDistanceTeleportLeavesDogInPlace(TestContext context) {
    final ServerPlayerEntity owner = placePlayer(context, new Vec3d(8.5, 2, 2.5));
    final HuskyEntity husky = spawnRegisteredDog(context, owner, new BlockPos(1, 2, 2));
    final Vec3d dogStartPos = husky.getPos();

    context.runAtTick(TELEPORT_TICK, () -> teleport(context, owner, new Vec3d(14.5, 2, 2.5)));

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

  private static void fillDestinationWithStone(TestContext context) {
    for (int x = 17; x <= 23; x++) {
      for (int y = 2; y <= 4; y++) {
        for (int z = 0; z <= 4; z++) {
          context.setBlockState(new BlockPos(x, y, z), Blocks.STONE.getDefaultState());
        }
      }
    }
  }

  private static void coverDestinationFloor(TestContext context, BlockState state) {
    for (int x = 17; x <= 23; x++) {
      for (int z = 0; z <= 4; z++) {
        context.setBlockState(new BlockPos(x, 2, z), state);
      }
    }
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
