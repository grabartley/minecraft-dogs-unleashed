package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public final class PetLocationService {

  private static final int PET_RECALL_TICKET_LEVEL = 3;
  private static final int PET_RECALL_RETRY_TICKS = 20;
  private static final double LONG_DISTANCE_TELEPORT_MIN_DISTANCE = 16.0;
  private static final double NEARBY_DOG_DISTANCE = 12.0;
  private static final ChunkTicketType<ChunkPos> PET_RECALL_TICKET =
      ChunkTicketType.create(
          "dogs_unleashed_pet_recall", Comparator.comparingLong(ChunkPos::toLong), 40);

  private PetLocationService() {}

  /**
   * A teleport of at least this distance outranges FollowOwnerGoal, whose teleport only fires while
   * the dog's chunk is still ticking near the owner. Beyond it, pets must be brought along
   * explicitly via {@link #bringActivePetsToOwner}.
   */
  public static boolean isLongDistanceTeleport(Vec3d from, Vec3d to) {
    return from.squaredDistanceTo(to)
        >= LONG_DISTANCE_TELEPORT_MIN_DISTANCE * LONG_DISTANCE_TELEPORT_MIN_DISTANCE;
  }

  /**
   * Brings every alive pet that is actively following its owner (not sitting, not sleeping in a
   * bed) to a safe position beside the owner. Call after the owner relocates in a way pets cannot
   * follow on their own: a dimension change or a long-distance teleport within one world.
   */
  public static void bringActivePetsToOwner(ServerPlayerEntity player) {
    if (player.isDisconnected() || player.isRemoved()) {
      return;
    }

    final MinecraftServer server = player.getServer();
    final PetManager petManager = PetManager.get(server);
    for (final PetData petData : petManager.getPetsByOwner(player.getUuid())) {
      if (!petData.isAlive()) {
        continue;
      }

      final UnleashedDogEntity dog = findDog(server, petData);
      if (dog == null) {
        DogsUnleashed.log.warn("[PetFollow] Dog {} not found in any world", petData.getPetId());
        continue;
      }
      if (dog.isRemoved() || dog.isInSittingPose() || dog.isSleepingInBed()) {
        continue;
      }
      if (isBesideOwner(dog, player)) {
        continue;
      }

      summonDog(dog, petData, player);
    }
  }

  private static boolean isBesideOwner(UnleashedDogEntity dog, ServerPlayerEntity player) {
    return dog.getWorld() == player.getWorld()
        && dog.squaredDistanceTo(player) <= NEARBY_DOG_DISTANCE * NEARBY_DOG_DISTANCE;
  }

  @Nullable
  public static UnleashedDogEntity findDog(MinecraftServer server, PetData petData) {
    final BlockPos lastPos = petData.getLastKnownPosition();
    final ServerWorld knownWorld = getKnownWorld(server, petData);
    if (knownWorld != null) {
      final UnleashedDogEntity dog = findInWorld(knownWorld, petData.getPetId());
      if (dog != null) {
        return dog;
      }

      if (lastPos != null) {
        final ChunkPos chunkPos = new ChunkPos(lastPos);
        loadChunkEntities(knownWorld, chunkPos.x, chunkPos.z);
        final UnleashedDogEntity loaded = findInWorld(knownWorld, petData.getPetId());
        if (loaded != null) {
          return loaded;
        }
      }
    }

    for (final ServerWorld world : server.getWorlds()) {
      if (world == knownWorld) continue;
      final UnleashedDogEntity dog = findInWorld(world, petData.getPetId());
      if (dog != null) {
        return dog;
      }
    }
    return null;
  }

  public static void loadAndSummon(
      MinecraftServer server, PetData petData, ServerPlayerEntity player) {
    final UnleashedDogEntity loadedDog = findDog(server, petData);
    if (loadedDog != null) {
      summonDog(loadedDog, petData, player);
      return;
    }

    final String dimStr = petData.getDimension();
    final BlockPos lastKnownPosition = petData.getLastKnownPosition();
    if (dimStr == null || dimStr.isEmpty() || lastKnownPosition == null) {
      return;
    }

    final ServerWorld knownWorld =
        server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimStr)));
    if (knownWorld == null) return;

    final ChunkPos chunkPos = new ChunkPos(lastKnownPosition);
    knownWorld
        .getChunkManager()
        .addTicket(PET_RECALL_TICKET, chunkPos, PET_RECALL_TICKET_LEVEL, chunkPos);
    retrySummon(server, petData, player, knownWorld, chunkPos, PET_RECALL_RETRY_TICKS);
  }

  private static void retrySummon(
      MinecraftServer server,
      PetData petData,
      ServerPlayerEntity player,
      ServerWorld knownWorld,
      ChunkPos chunkPos,
      int attemptsRemaining) {
    DogsUnleashed.runNextTick(
        () -> {
          final UnleashedDogEntity dog = findDog(server, petData);
          if (dog != null) {
            knownWorld
                .getChunkManager()
                .removeTicket(PET_RECALL_TICKET, chunkPos, PET_RECALL_TICKET_LEVEL, chunkPos);
            summonDog(dog, petData, player);
            return;
          }

          if (attemptsRemaining <= 1) {
            knownWorld
                .getChunkManager()
                .removeTicket(PET_RECALL_TICKET, chunkPos, PET_RECALL_TICKET_LEVEL, chunkPos);
            return;
          }

          retrySummon(server, petData, player, knownWorld, chunkPos, attemptsRemaining - 1);
        });
  }

  private static void summonDog(
      UnleashedDogEntity dog, PetData petData, ServerPlayerEntity player) {
    final ServerWorld playerWorld = player.getServerWorld();
    final Vec3d summonPos = findSafeSummonPosition(playerWorld, player.getBlockPos(), dog);

    dog.wakeUp();
    dog.setSitting(false);
    if (dog.getWorld() != playerWorld) {
      dog.teleportToWorld(playerWorld, summonPos);
    } else {
      dog.teleport(
          playerWorld,
          summonPos.x,
          summonPos.y,
          summonPos.z,
          java.util.Set.of(),
          dog.getYaw(),
          dog.getPitch());
      refreshEntityTracking(playerWorld, dog, player);
    }

    petData.setDimension(playerWorld.getRegistryKey().getValue().toString());
    petData.setLastKnownPosition(BlockPos.ofFloored(summonPos));
    PetManager.get(player.getServer()).updatePet(petData);
  }

  private static Vec3d findSafeSummonPosition(
      ServerWorld world, BlockPos center, UnleashedDogEntity dog) {
    for (final BlockPos basePos : BlockPos.iterateOutwards(center, 2, 1, 2)) {
      if (!isSafeSummonBase(world, basePos)) {
        continue;
      }

      final Vec3d candidate = new Vec3d(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5);
      final Box box =
          dog.getBoundingBox()
              .offset(candidate.x - dog.getX(), candidate.y - dog.getY(), candidate.z - dog.getZ());
      if (world.getBlockCollisions(dog, box).iterator().hasNext()) {
        continue;
      }
      return candidate;
    }

    return new Vec3d(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
  }

  private static boolean isSafeSummonBase(ServerWorld world, BlockPos basePos) {
    final var stateAtPos = world.getBlockState(basePos);
    final var stateAbove = world.getBlockState(basePos.up());
    final var stateBelow = world.getBlockState(basePos.down());

    final boolean openAtFeet = stateAtPos.isAir() || stateAtPos.isReplaceable();
    final boolean openAtHead = stateAbove.isAir() || stateAbove.isReplaceable();
    final boolean stableFloor = stateBelow.isSolidBlock(world, basePos.down());
    final boolean notInFluid =
        !stateAtPos.getFluidState().isStill() && !stateAbove.getFluidState().isStill();

    return openAtFeet && openAtHead && stableFloor && notInFluid;
  }

  private static void refreshEntityTracking(
      ServerWorld world, Entity entity, ServerPlayerEntity player) {
    world.getChunkManager().updatePosition(player);
  }

  @Nullable
  private static ServerWorld getKnownWorld(MinecraftServer server, PetData petData) {
    final String dimStr = petData.getDimension();
    if (dimStr == null || dimStr.isEmpty()) {
      return null;
    }
    return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimStr)));
  }

  @Nullable
  private static UnleashedDogEntity findInWorld(ServerWorld world, java.util.UUID petId) {
    final Entity entity = world.getEntity(petId);
    return entity instanceof UnleashedDogEntity dog ? dog : null;
  }

  private static void loadChunkEntities(ServerWorld world, int chunkX, int chunkZ) {
    if (world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true)
        instanceof WorldChunk worldChunk) {
      worldChunk.loadEntities();
    }
  }
}
