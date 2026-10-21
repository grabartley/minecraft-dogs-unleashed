package com.grahambartley.dogsunleashed.entity.away;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogRecreation;
import com.grahambartley.dogsunleashed.entity.Headings;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class AwayArrival {

  private AwayArrival() {}

  public static Optional<UnleashedDogEntity> arrive(
      final UUID pet,
      final Identifier entityType,
      final NbtCompound snapshot,
      final ServerPlayerEntity player,
      final DogCommand command,
      final Runnable leaveStore,
      final Runnable rejoinStore) {
    final MinecraftServer server = player.getServer();
    final ServerWorld world = player.getServerWorld();
    final UnleashedDogEntity dog =
        Registries.ENTITY_TYPE
            .getOrEmpty(entityType)
            .map(type -> DogRecreation.fromSnapshot(type, snapshot, world))
            .orElse(null);
    if (dog == null) {
      DogsUnleashed.log.error(
          "[Away] Could not rebuild {} as {}, keeping it away", pet, entityType);
      return Optional.empty();
    }
    final Vec3d beside = PetLocationService.findSafeSpotBeside(world, player.getBlockPos(), dog);
    final Vec3d spot = beside != null ? beside : player.getPos();
    final float facing =
        beside != null
            ? Headings.yawToward(spot.x, spot.z, player.getX(), player.getZ())
            : player.getYaw() + 180.0f;
    dog.refreshPositionAndAngles(spot.x, spot.y, spot.z, facing, 0.0f);
    dog.setBodyYaw(facing);
    dog.setHeadYaw(facing);
    dog.setVelocity(Vec3d.ZERO);
    dog.fallDistance = 0.0f;
    dog.setFireTicks(0);
    dog.getCommandController().apply(command);
    server
        .getWorlds()
        .forEach(candidate -> DogRecreation.discardStaleTwin(candidate, dog.getUuid(), null));
    leaveStore.run();
    if (!world.spawnEntity(dog)) {
      rejoinStore.run();
      DogsUnleashed.log.error("[Away] Could not spawn {}, keeping it away", dog.getUuid());
      return Optional.empty();
    }
    puff(world, spot);
    recordArrival(server, dog, world, spot);
    return Optional.of(dog);
  }

  public static void puff(final ServerWorld world, final Vec3d pos) {
    world.spawnParticles(ParticleTypes.POOF, pos.x, pos.y + 0.4, pos.z, 12, 0.3, 0.3, 0.3, 0.02);
  }

  private static void recordArrival(
      final MinecraftServer server,
      final UnleashedDogEntity dog,
      final ServerWorld world,
      final Vec3d spot) {
    final PetData pet = PetManager.get(server).getPetByEntityId(dog.getUuid());
    if (pet != null) {
      pet.setDimension(world.getRegistryKey().getValue().toString());
      pet.setLastKnownPosition(BlockPos.ofFloored(spot));
      PetManager.get(server).updatePet(pet);
    }
  }
}
