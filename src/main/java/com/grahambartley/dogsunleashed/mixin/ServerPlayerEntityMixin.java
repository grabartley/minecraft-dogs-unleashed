package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
  @Unique private static final double FOLLOWING_DOG_CAPTURE_RADIUS = 12.0D;
  @Unique private static final double PLAYER_TELEPORT_MIN_DISTANCE = 16.0D;

  @Unique private ServerWorld dogsUnleashed$teleportSourceWorld;
  @Unique private Vec3d dogsUnleashed$teleportSourcePos;
  @Unique private List<UUID> dogsUnleashed$dogsToTeleport = List.of();

  @Inject(
      method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
      at = @At("HEAD"))
  private void dogsUnleashed$captureNearbyDogs(
      ServerWorld targetWorld,
      double x,
      double y,
      double z,
      Set<PositionFlag> flags,
      float yaw,
      float pitch,
      CallbackInfoReturnable<Boolean> cir) {
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    this.dogsUnleashed$teleportSourceWorld = player.getServerWorld();
    this.dogsUnleashed$teleportSourcePos = player.getPos();

    final Box searchBox = player.getBoundingBox().expand(FOLLOWING_DOG_CAPTURE_RADIUS);
    final List<UUID> nearbyDogs = new ArrayList<>();
    for (final UnleashedDogEntity dog :
        this.dogsUnleashed$teleportSourceWorld.getEntitiesByClass(
            UnleashedDogEntity.class, searchBox, candidate -> candidate.isOwner(player))) {
      if (dog.isRemoved() || dog.isInSittingPose() || dog.isSleepingInBed()) {
        continue;
      }
      nearbyDogs.add(dog.getUuid());
    }
    this.dogsUnleashed$dogsToTeleport = nearbyDogs;
  }

  @Inject(
      method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
      at = @At("RETURN"))
  private void dogsUnleashed$teleportNearbyDogs(
      ServerWorld targetWorld,
      double x,
      double y,
      double z,
      Set<PositionFlag> flags,
      float yaw,
      float pitch,
      CallbackInfoReturnable<Boolean> cir) {
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    try {
      if (!cir.getReturnValueZ()
          || this.dogsUnleashed$teleportSourceWorld == null
          || this.dogsUnleashed$teleportSourcePos == null
          || this.dogsUnleashed$dogsToTeleport.isEmpty()
          || this.dogsUnleashed$teleportSourceWorld != targetWorld) {
        return;
      }

      if (this.dogsUnleashed$teleportSourcePos.squaredDistanceTo(player.getPos())
          < PLAYER_TELEPORT_MIN_DISTANCE * PLAYER_TELEPORT_MIN_DISTANCE) {
        return;
      }

      for (final UUID dogId : this.dogsUnleashed$dogsToTeleport) {
        if (!(targetWorld.getEntity(dogId) instanceof UnleashedDogEntity dog)
            || dog.isRemoved()
            || dog.isInSittingPose()
            || dog.isSleepingInBed()) {
          continue;
        }

        dog.wakeUp();
        dog.setSitting(false);
        dog.teleport(
            targetWorld,
            player.getX(),
            player.getY(),
            player.getZ(),
            Set.of(),
            dog.getYaw(),
            dog.getPitch());
      }
    } finally {
      this.dogsUnleashed$teleportSourceWorld = null;
      this.dogsUnleashed$teleportSourcePos = null;
      this.dogsUnleashed$dogsToTeleport = List.of();
    }
  }
}
