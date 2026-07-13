package com.grahambartley.dogsunleashed.mixin;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import java.util.Set;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Brings active pets along when their owner teleports a long distance within one world. No Fabric
 * event fires for same-world teleports; cross-world teleports are already handled by {@code
 * PlayerDimensionChangeListener} via {@code AFTER_PLAYER_CHANGE_WORLD}.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

  @Unique private ServerWorld dogsUnleashed$teleportSourceWorld;
  @Unique private Vec3d dogsUnleashed$teleportSourcePos;

  @Inject(
      method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
      at = @At("HEAD"))
  private void dogsUnleashed$captureTeleportSource(
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
  }

  @Inject(
      method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
      at = @At("RETURN"))
  private void dogsUnleashed$bringPetsAfterLongDistanceTeleport(
      ServerWorld targetWorld,
      double x,
      double y,
      double z,
      Set<PositionFlag> flags,
      float yaw,
      float pitch,
      CallbackInfoReturnable<Boolean> cir) {
    final ServerWorld sourceWorld = this.dogsUnleashed$teleportSourceWorld;
    final Vec3d sourcePos = this.dogsUnleashed$teleportSourcePos;
    this.dogsUnleashed$teleportSourceWorld = null;
    this.dogsUnleashed$teleportSourcePos = null;

    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    if (!cir.getReturnValueZ()
        || sourceWorld == null
        || sourcePos == null
        || sourceWorld != targetWorld
        || !PetLocationService.isLongDistanceTeleport(sourcePos, player.getPos())) {
      return;
    }

    // Deferred a tick so the destination chunks the player is now loading are available for safe
    // pet placement instead of teleporting dogs into still-unloaded terrain.
    DogsUnleashed.runNextTick(() -> PetLocationService.bringActivePetsToOwner(player));
  }
}
