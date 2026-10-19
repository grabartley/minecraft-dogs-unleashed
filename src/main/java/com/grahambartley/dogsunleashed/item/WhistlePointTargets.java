package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.DogFriendlyTargetPolicy;
import com.grahambartley.dogsunleashed.entity.DogStandingSpot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.MobFacts;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.Order;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.PointedKind;
import com.grahambartley.dogsunleashed.network.payload.PointWithWhistlePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Angerable;
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
import net.minecraft.world.RaycastContext;

public final class WhistlePointTargets {

  private static final double FACE_INSET = 0.49;
  private static final int MAX_DROP_TO_GROUND = 4;

  private WhistlePointTargets() {}

  public static Optional<WhistlePointTarget> resolve(
      final ServerPlayerEntity player, final PointWithWhistlePayload payload) {
    return payload.pointsAtEntity()
        ? resolveEntity(player, payload.entityId())
        : resolveBlock(player, payload.blockPos());
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
    final Order order = WhistlePointRules.orderForEntity(kindOf(entity), factsOf(entity));
    return order == Order.NONE
        ? Optional.empty()
        : Optional.of(WhistlePointTarget.atEntity(order, entity));
  }

  private static Optional<WhistlePointTarget> resolveBlock(
      final ServerPlayerEntity player, final BlockPos pos) {
    final ServerWorld world = player.getServerWorld();
    if (!world.isChunkLoaded(pos) || world.getBlockState(pos).isAir()) {
      return Optional.empty();
    }
    final Vec3d eye = player.getEyePos();
    if (!WhistlePointRules.inRange(eye.squaredDistanceTo(Vec3d.ofCenter(pos)))) {
      return Optional.empty();
    }
    for (final Direction face : Direction.values()) {
      final BlockHitResult hit = raycast(player, eye, faceCentre(pos, face));
      if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
        return Optional.of(WhistlePointTarget.atBlock(pos, hit.getSide()));
      }
    }
    return Optional.empty();
  }

  private static BlockHitResult raycast(
      final ServerPlayerEntity player, final Vec3d from, final Vec3d to) {
    return player
        .getServerWorld()
        .raycast(
            new RaycastContext(
                from,
                to,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player));
  }

  private static Vec3d faceCentre(final BlockPos pos, final Direction face) {
    return Vec3d.ofCenter(pos)
        .add(
            face.getOffsetX() * FACE_INSET,
            face.getOffsetY() * FACE_INSET,
            face.getOffsetZ() * FACE_INSET);
  }

  public static BlockPos standingSpot(
      final UnleashedDogEntity dog, final BlockPos pos, final Direction side) {
    final BlockPos beside = pos.offset(side);
    final List<BlockPos> candidates = new ArrayList<>();
    for (int drop = 0; drop <= MAX_DROP_TO_GROUND; drop++) {
      candidates.add(beside.down(drop));
    }
    candidates.add(pos.up());
    return DogStandingSpot.firstSafe(dog.getWorld(), dog, candidates)
        .map(BlockPos::ofFloored)
        .orElse(beside);
  }

  private static PointedKind kindOf(final Entity entity) {
    if (entity instanceof ItemEntity) {
      return PointedKind.ITEM;
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
        entity instanceof Angerable);
  }

  private static Vec3d closestPoint(final Box box, final Vec3d from) {
    return new Vec3d(
        MathHelper.clamp(from.x, box.minX, box.maxX),
        MathHelper.clamp(from.y, box.minY, box.maxY),
        MathHelper.clamp(from.z, box.minZ, box.maxZ));
  }
}
