package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Moves a dog between worlds by recreating it rather than teleporting it in place. */
public final class DogWorldTransfer {

  private DogWorldTransfer() {}

  /**
   * Recreates the dog in the destination world at the given position, which may be the current
   * world. Recreation, rather than an in-place teleport, guarantees clients receive a fresh spawn
   * at the correct position: in-place long-range teleports of entities streamed in from
   * ticket-loaded chunks leave stale tracker state behind, making the dog invisible until relog.
   *
   * @return the dog entity in the destination world (may be a different instance from {@code dog})
   */
  public static UnleashedDogEntity teleportToWorld(
      final UnleashedDogEntity dog, final ServerWorld destination, final Vec3d pos) {
    final NbtCompound nbt = dog.writeNbt(new NbtCompound());
    nbt.remove("Dimension");

    final ServerWorld currentWorld = (ServerWorld) dog.getWorld();
    final UnleashedDogEntity newDog = (UnleashedDogEntity) dog.getType().create(destination);
    if (newDog == null) {
      DogsUnleashed.log.warn(
          "[Dog] teleportToWorld: failed to create entity in {}",
          destination.getRegistryKey().getValue());
      return dog;
    }

    newDog.readNbt(nbt);
    newDog.setPos(pos.x, pos.y, pos.z);
    newDog.setYaw(dog.getYaw());
    newDog.setPitch(dog.getPitch());

    dog.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    currentWorld
        .getChunk(dog.getBlockPos().getX() >> 4, dog.getBlockPos().getZ() >> 4)
        .setNeedsSaving(true);

    final Entity existing = destination.getEntity(dog.getUuid());
    if (existing != null && existing != dog) {
      DogsUnleashed.log.warn(
          "[Dog] teleportToWorld: removing stale entity {} from {} (UUID collision)",
          existing.getUuid(),
          destination.getRegistryKey().getValue());
      existing.remove(Entity.RemovalReason.DISCARDED);
    }

    destination.spawnEntity(newDog);

    DogsUnleashed.log.info(
        "[Dog] teleportToWorld: new dog {} spawned in {}",
        newDog.getUuid(),
        destination.getRegistryKey().getValue());
    return newDog;
  }
}
