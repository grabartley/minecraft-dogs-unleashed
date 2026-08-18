package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Moves a dog between worlds by recreating it rather than teleporting it in place. */
public final class DogWorldTransfer {

  private DogWorldTransfer() {}

  /**
   * Recreates the dog in the destination world at the given position, which may be the current
   * world.
   *
   * @return the dog entity in the destination world (may be a different instance from {@code dog})
   */
  public static UnleashedDogEntity teleportToWorld(
      final UnleashedDogEntity dog, final ServerWorld destination, final Vec3d pos) {
    final ServerWorld currentWorld = (ServerWorld) dog.getWorld();
    final BlockPos departurePos = dog.getBlockPos();

    final UnleashedDogEntity newDog =
        DogRecreation.recreateAs(dog, dog.getType(), destination, pos);
    if (newDog == null) {
      DogsUnleashed.log.warn(
          "[Dog] teleportToWorld: failed to create entity in {}",
          destination.getRegistryKey().getValue());
      return dog;
    }

    currentWorld.getChunk(departurePos.getX() >> 4, departurePos.getZ() >> 4).setNeedsSaving(true);

    DogsUnleashed.log.info(
        "[Dog] teleportToWorld: new dog {} spawned in {}",
        newDog.getUuid(),
        destination.getRegistryKey().getValue());
    return newDog;
  }
}
