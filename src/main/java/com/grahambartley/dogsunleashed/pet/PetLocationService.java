package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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

  public static boolean isLongDistanceTeleport(Vec3d from, Vec3d to) {
    return from.squaredDistanceTo(to)
        >= LONG_DISTANCE_TELEPORT_MIN_DISTANCE * LONG_DISTANCE_TELEPORT_MIN_DISTANCE;
  }

  public static void bringActivePetsToOwner(ServerPlayerEntity player) {
    if (player.isDisconnected() || player.isRemoved()) {
      return;
    }

    final MinecraftServer server = player.getServer();
    for (final PetData petData : PetManager.get(server).getPetsByOwner(player.getUuid())) {
      if (!petData.isAlive()) {
        continue;
      }
      locateAndSummon(server, petData, player, false, dog -> isActivelyFollowing(dog, player));
    }
  }

  private static boolean isActivelyFollowing(UnleashedDogEntity dog, ServerPlayerEntity player) {
    return !dog.isRemoved()
        && dog.getCommand().followsOwnerOnRelocation()
        && !dog.isInSittingPose()
        && !dog.isSleepingInBed()
        && !isBesideOwner(dog, player);
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
    locateAndSummon(server, petData, player, true, dog -> !dog.isRemoved());
  }

  private static void locateAndSummon(
      MinecraftServer server,
      PetData petData,
      ServerPlayerEntity player,
      boolean explicitSummon,
      Predicate<UnleashedDogEntity> shouldSummon) {
    final UnleashedDogEntity dog = findDog(server, petData);
    if (dog != null) {
      if (shouldSummon.test(dog)) {
        summonDog(dog, petData, player, explicitSummon);
      }
      return;
    }

    final String dimStr = petData.getDimension();
    final BlockPos lastKnownPosition = petData.getLastKnownPosition();
    final ServerWorld knownWorld =
        dimStr == null || dimStr.isEmpty()
            ? null
            : server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimStr)));
    if (knownWorld == null || lastKnownPosition == null) {
      notifyLocateFailed(player, petData, explicitSummon);
      return;
    }

    final ChunkPos chunkPos = new ChunkPos(lastKnownPosition);
    knownWorld
        .getChunkManager()
        .addTicket(PET_RECALL_TICKET, chunkPos, PET_RECALL_TICKET_LEVEL, chunkPos);
    retrySummon(
        server,
        petData,
        player,
        knownWorld,
        chunkPos,
        PET_RECALL_RETRY_TICKS,
        explicitSummon,
        shouldSummon);
  }

  private static void retrySummon(
      MinecraftServer server,
      PetData petData,
      ServerPlayerEntity player,
      ServerWorld knownWorld,
      ChunkPos chunkPos,
      int attemptsRemaining,
      boolean explicitSummon,
      Predicate<UnleashedDogEntity> shouldSummon) {
    DogsUnleashed.runNextTick(
        () -> {
          final UnleashedDogEntity dog = findDog(server, petData);
          if (dog != null || attemptsRemaining <= 1 || player.isDisconnected()) {
            knownWorld
                .getChunkManager()
                .removeTicket(PET_RECALL_TICKET, chunkPos, PET_RECALL_TICKET_LEVEL, chunkPos);
            if (dog != null && !player.isDisconnected()) {
              if (shouldSummon.test(dog)) {
                summonDog(dog, petData, player, explicitSummon);
              }
            } else if (dog == null) {
              notifyLocateFailed(player, petData, explicitSummon);
            }
            return;
          }

          retrySummon(
              server,
              petData,
              player,
              knownWorld,
              chunkPos,
              attemptsRemaining - 1,
              explicitSummon,
              shouldSummon);
        });
  }

  private static void notifyLocateFailed(
      ServerPlayerEntity player, PetData petData, boolean explicitSummon) {
    DogsUnleashed.log.warn(
        "[PetSummon] Could not locate dog {} ({}) in {} near {}",
        petData.getName(),
        petData.getPetId(),
        petData.getDimension(),
        petData.getLastKnownPosition());
    if (explicitSummon && !player.isDisconnected()) {
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.summon_failed", petData.getName()), true);
    }
  }

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
    if (forcePlacement) {
      dog.getCommandController().apply(DogCommand.FOLLOW);
    }
    dog.teleportToWorld(playerWorld, summonPos);

    petData.setDimension(playerWorld.getRegistryKey().getValue().toString());
    petData.setLastKnownPosition(BlockPos.ofFloored(summonPos));
    PetManager.get(player.getServer()).updatePet(petData);

    DogsUnleashed.log.info(
        "[PetSummon] Brought {} ({}) to {} in {} (explicit={})",
        petData.getName(),
        petData.getPetId(),
        BlockPos.ofFloored(summonPos),
        playerWorld.getRegistryKey().getValue(),
        forcePlacement);
  }

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

  private static BlockPos snapToGround(ServerWorld world, BlockPos center) {
    if (!isPassable(world.getBlockState(center))) {
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
