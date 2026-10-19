package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.DogFriendlyTargetPolicy;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.MobFacts;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.Order;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.PointedKind;
import com.grahambartley.dogsunleashed.network.payload.PointWithWhistlePayload;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;

public final class WhistlePointTargets {

  private static final double FACE_INSET = 0.01;

  private WhistlePointTargets() {}

  public static Optional<WhistlePointTarget> resolve(
      final ServerPlayerEntity player, final PointWithWhistlePayload payload) {
    return payload.pointsAtEntity()
        ? resolveEntity(player, payload.entityId())
        : resolveBlock(player, payload.blockPos(), payload.side());
  }

  private static Optional<WhistlePointTarget> resolveEntity(
      final ServerPlayerEntity player, final int entityId) {
    final Entity entity = player.getServerWorld().getEntityById(entityId);
    if (entity == null || entity == player || !entity.isAlive()) {
      return Optional.empty();
    }
    final Vec3d eye = player.getEyePos();
    if (!WhistlePointRules.inRange(
            eye.squaredDistanceTo(closestPoint(entity.getBoundingBox(), eye)))
        || !player.canSee(entity)) {
      return Optional.empty();
    }
    final Order order = WhistlePointRules.orderForEntity(kindOf(entity, player), factsOf(entity));
    return order == Order.NONE
        ? Optional.empty()
        : Optional.of(WhistlePointTarget.atEntity(order, entity));
  }

  private static Optional<WhistlePointTarget> resolveBlock(
      final ServerPlayerEntity player, final BlockPos pos, final Direction aimedSide) {
    final ServerWorld world = player.getServerWorld();
    if (!world.isChunkLoaded(pos) || world.getBlockState(pos).isAir()) {
      return Optional.empty();
    }
    final Vec3d eye = player.getEyePos();
    if (!WhistlePointRules.inRange(eye.squaredDistanceTo(Vec3d.ofCenter(pos)))) {
      return Optional.empty();
    }
    if (seesFace(player, eye, pos, aimedSide)) {
      return Optional.of(WhistlePointTarget.atBlock(pos, aimedSide));
    }
    for (final Direction face : Direction.values()) {
      if (seesFace(player, eye, pos, face)) {
        return Optional.of(WhistlePointTarget.atBlock(pos, face));
      }
    }
    return Optional.empty();
  }

  private static boolean seesFace(
      final ServerPlayerEntity player, final Vec3d eye, final BlockPos pos, final Direction face) {
    final BlockHitResult hit = raycast(player, eye, faceCentre(player, pos, face));
    return hit.getType() == HitResult.Type.BLOCK
        && hit.getBlockPos().equals(pos)
        && hit.getSide() == face;
  }

  private static BlockHitResult raycast(
      final ServerPlayerEntity player, final Vec3d from, final Vec3d to) {
    return player
        .getServerWorld()
        .raycast(
            new RaycastContext(
                from,
                to,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player));
  }

  private static Vec3d faceCentre(
      final ServerPlayerEntity player, final BlockPos pos, final Direction face) {
    final VoxelShape shape =
        player.getServerWorld().getBlockState(pos).getOutlineShape(player.getServerWorld(), pos);
    final Box box = (shape.isEmpty() ? VoxelShapes.fullCube() : shape).getBoundingBox().offset(pos);
    final Vec3d centre = box.getCenter();
    return centre.add(
        face.getOffsetX() * (box.getLengthX() / 2 - FACE_INSET),
        face.getOffsetY() * (box.getLengthY() / 2 - FACE_INSET),
        face.getOffsetZ() * (box.getLengthZ() / 2 - FACE_INSET));
  }

  private static PointedKind kindOf(final Entity entity, final ServerPlayerEntity player) {
    if (entity instanceof ItemEntity item) {
      final Entity claimant = item.getOwner();
      return claimant == null || claimant == player ? PointedKind.ITEM : PointedKind.OTHER;
    }
    if (entity instanceof PlayerEntity) {
      return PointedKind.PLAYER;
    }
    return entity instanceof MobEntity ? PointedKind.MOB : PointedKind.OTHER;
  }

  private static MobFacts factsOf(final Entity entity) {
    final boolean tamed =
        (entity instanceof TameableEntity tameable && tameable.isTamed())
            || (entity instanceof AbstractHorseEntity horse && horse.isTame());
    return new MobFacts(
        DogFriendlyTargetPolicy.isFriendlyTarget(entity),
        tamed,
        entity.hasCustomName(),
        entity instanceof MobEntity mob && mob.isLeashed(),
        entity instanceof Monster,
        entity instanceof Angerable,
        entity instanceof CreeperEntity || entity instanceof GhastEntity);
  }

  private static Vec3d closestPoint(final Box box, final Vec3d from) {
    return new Vec3d(
        MathHelper.clamp(from.x, box.minX, box.maxX),
        MathHelper.clamp(from.y, box.minY, box.maxY),
        MathHelper.clamp(from.z, box.minZ, box.maxZ));
  }
}
