package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogRecreation {

  private DogRecreation() {}

  static @Nullable UnleashedDogEntity recreateAs(
      final UnleashedDogEntity dog,
      final EntityType<?> type,
      final ServerWorld destination,
      final Vec3d pos) {
    final NbtCompound nbt = dog.writeNbt(new NbtCompound());
    nbt.remove("Dimension");

    final UnleashedDogEntity newDog = fromSnapshot(type, nbt, destination);
    if (newDog == null) {
      return null;
    }
    newDog.setPos(pos.x, pos.y, pos.z);
    newDog.setYaw(dog.getYaw());
    newDog.setPitch(dog.getPitch());

    dog.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    discardStaleTwin(destination, dog.getUuid(), dog);
    destination.spawnEntity(newDog);
    return newDog;
  }

  public static @Nullable UnleashedDogEntity fromSnapshot(
      final EntityType<?> type, final NbtCompound nbt, final ServerWorld world) {
    if (!(type.create(world) instanceof UnleashedDogEntity dog)) {
      return null;
    }
    try {
      dog.readNbt(nbt);
    } catch (RuntimeException e) {
      DogsUnleashed.log.error("[Dog] Could not read saved dog data as {}", type, e);
      return null;
    }
    return dog;
  }

  public static void discardStaleTwin(
      final ServerWorld world, final UUID uuid, final @Nullable Entity keep) {
    final Entity existing = world.getEntity(uuid);
    if (existing != null && existing != keep) {
      DogsUnleashed.log.warn(
          "[Dog] removing stale entity {} from {} (UUID collision)",
          existing.getUuid(),
          world.getRegistryKey().getValue());
      existing.remove(Entity.RemovalReason.DISCARDED);
    }
  }
}
