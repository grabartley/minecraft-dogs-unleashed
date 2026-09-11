package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogWorldTransfer {

  private DogWorldTransfer() {}

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
