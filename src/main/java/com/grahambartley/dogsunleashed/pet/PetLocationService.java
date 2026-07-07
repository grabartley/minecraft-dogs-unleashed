package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
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

      summonDog(dog, petData, player, false);
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
      summonDog(loadedDog, petData, player, true);
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
            summonDog(dog, petData, player, true);
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

  /**
   * Force placement is for explicit summons (Pet Manager, commands), which must always deliver the
   * dog: when nothing nearby is safe, the owner's own position is the destination and the owner
   * deals with the surroundings. Automatic follows pass false so a teleport into hazardous terrain
   * leaves the dog safely where it was.
   */
  private static void summonDog(
      UnleashedDogEntity dog, PetData petData, ServerPlayerEntity player, boolean forcePlacement) {
    final ServerWorld playerWorld = player.getServerWorld();
    Vec3d summonPos = findSafeSummonPosition(playerWorld, player.getBlockPos(), dog);
    if (summonPos == null) {
      if (!forcePlacement) {
        DogsUnleashed.log.warn(
            "[PetSummon] No safe position near {} for dog {}, leaving it where it is",
            player.getBlockPos(),
            petData.getPetId());
        return;
      }
      summonPos = player.getPos();
    }

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

  /**
   * Returns null when nothing near the center is safe, e.g. the owner teleported into solid
   * terrain. Summoning must be skipped in that case: any position in range would suffocate the dog.
   * Candidates are validated through vanilla's respawn placement rules, so partial-height ground
   * cover (snow layers, slabs, paths, farmland) counts as valid footing and the dog stands at the
   * precise height of the block's collision shape.
   */
  @Nullable
  private static Vec3d findSafeSummonPosition(
      ServerWorld world, BlockPos center, UnleashedDogEntity dog) {
    for (final BlockPos basePos : BlockPos.iterateOutwards(snapToGround(world, center), 2, 1, 2)) {
      if (world.getFluidState(basePos).isStill()) {
        continue;
      }

      final Vec3d standPos = Dismounting.findRespawnPos(dog.getType(), world, basePos, false);
      if (standPos != null) {
        return standPos;
      }
    }

    return null;
  }

  /**
   * Walks down through passable blocks so a flying or falling owner still gets pets placed on the
   * ground beneath them. A buried owner has no passable column below, keeps the original center,
   * and fails the safe-base checks so the summon is skipped.
   */
  private static BlockPos snapToGround(ServerWorld world, BlockPos center) {
    if (!isPassable(world.getBlockState(center))) {
      // An embedded owner (teleported into terrain) gets no descent: walking down from inside a
      // block would tunnel through the surrounding solid into unrelated caves or gaps below.
      return center;
    }

    final BlockPos.Mutable pos = center.mutableCopy();
    while (pos.getY() > world.getBottomY() + 1 && isPassable(world.getBlockState(pos.down()))) {
      pos.move(Direction.DOWN);
    }
    return pos.toImmutable();
  }

  private static boolean isPassable(BlockState state) {
    return state.isAir() || state.isReplaceable();
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
