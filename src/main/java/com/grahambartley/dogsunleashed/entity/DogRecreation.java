package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
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

    if (!(type.create(destination) instanceof UnleashedDogEntity newDog)) {
      return null;
    }

    newDog.readNbt(nbt);
    newDog.setPos(pos.x, pos.y, pos.z);
    newDog.setYaw(dog.getYaw());
    newDog.setPitch(dog.getPitch());

    dog.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    discardStaleTwin(destination, dog);
    destination.spawnEntity(newDog);
    return newDog;
  }

  private static void discardStaleTwin(
      final ServerWorld destination, final UnleashedDogEntity dog) {
    final Entity existing = destination.getEntity(dog.getUuid());
    if (existing != null && existing != dog) {
      DogsUnleashed.log.warn(
          "[Dog] recreateAs: removing stale entity {} from {} (UUID collision)",
          existing.getUuid(),
          destination.getRegistryKey().getValue());
      existing.remove(Entity.RemovalReason.DISCARDED);
    }
  }
}
