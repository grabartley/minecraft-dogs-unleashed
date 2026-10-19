package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.DeathWatchRules;
import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class OwnerDeathListener {

  private OwnerDeathListener() {}

  public static void initialize() {
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, damageSource) -> {
          if (entity instanceof PlayerEntity player
              && player.getWorld() instanceof ServerWorld world
              && DeathWatchRules.isGuardable(player.getBlockY(), world.getBottomY())) {
            gatherDogsAt(world, player.getUuid(), player.getBlockPos());
          }
        });
  }

  private static void gatherDogsAt(
      final ServerWorld world, final UUID ownerId, final BlockPos spot) {
    for (final UnleashedDogEntity dog :
        world.getEntitiesByClass(
            UnleashedDogEntity.class,
            new Box(spot).expand(DeathWatchRules.GATHER_RANGE),
            dog -> answersCall(dog, ownerId, spot))) {
      dog.getComponents().get(DogDeathWatch.class).keepWatchAt(spot, world.getRegistryKey());
    }
  }

  private static boolean answersCall(
      final UnleashedDogEntity dog, final UUID ownerId, final BlockPos spot) {
    return dog.isAlive()
        && DeathWatchRules.answersCall(
            new DeathWatchRules.Candidate(
                dog.isTamed(),
                ownerId.equals(dog.getOwnerUuid()),
                dog.getCommand().followsOwnerOnRelocation(),
                dog.getComponents().get(DogDeathWatch.class).isKeepingWatch(),
                dog.isLeashed(),
                dog.squaredDistanceTo(Vec3d.ofBottomCenter(spot))));
  }
}
