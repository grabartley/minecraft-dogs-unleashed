package com.grahambartley.dogsunleashed.whistle;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.item.WhistlePointRules;
import com.grahambartley.dogsunleashed.network.payload.PointWithWhistlePayload;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class WhistlePointer {

  private static final float NO_PARTIAL_TICK = 1.0f;
  private static final double ENTITY_SEARCH_MARGIN = 1.0;
  private static final double ITEM_SNAP_RADIUS = 1.0;

  private WhistlePointer() {}

  public static void register() {
    UseItemCallback.EVENT.register(
        (player, world, hand) -> {
          final ItemStack stack = player.getStackInHand(hand);
          if (world.isClient
              && player == MinecraftClient.getInstance().player
              && player.isSneaking()
              && stack.isOf(ModItems.DOG_WHISTLE)
              && !player.getItemCooldownManager().isCoolingDown(ModItems.DOG_WHISTLE)) {
            aim(player, hand).ifPresent(ClientPlayNetworking::send);
          }
          return TypedActionResult.pass(stack);
        });
  }

  public static Optional<PointWithWhistlePayload> aim(final PlayerEntity player, final Hand hand) {
    final double range = WhistlePointRules.RANGE;
    final Vec3d eye = player.getCameraPosVec(NO_PARTIAL_TICK);
    final Vec3d look = player.getRotationVec(NO_PARTIAL_TICK);
    final Vec3d end = eye.add(look.multiply(range));
    final HitResult blockHit = player.raycast(range, NO_PARTIAL_TICK, false);
    final double reachSquared =
        blockHit.getType() == HitResult.Type.MISS
            ? range * range
            : eye.squaredDistanceTo(blockHit.getPos());
    final Box sweep =
        player.getBoundingBox().stretch(look.multiply(range)).expand(ENTITY_SEARCH_MARGIN);
    final EntityHitResult entityHit =
        ProjectileUtil.raycast(player, eye, end, sweep, WhistlePointer::isPointable, reachSquared);
    if (entityHit != null) {
      return Optional.of(PointWithWhistlePayload.atEntity(hand, entityHit.getEntity().getId()));
    }
    if (!(blockHit instanceof BlockHitResult block) || block.getType() != HitResult.Type.BLOCK) {
      return Optional.empty();
    }
    return Optional.of(
        itemNear(player, block.getPos())
            .map(item -> PointWithWhistlePayload.atEntity(hand, item.getId()))
            .orElseGet(
                () -> PointWithWhistlePayload.atBlock(hand, block.getBlockPos(), block.getSide())));
  }

  private static boolean isPointable(final Entity entity) {
    return !entity.isSpectator() && (entity.canHit() || entity instanceof ItemEntity);
  }

  private static Optional<ItemEntity> itemNear(final PlayerEntity player, final Vec3d spot) {
    return player
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            Box.of(spot, 2 * ITEM_SNAP_RADIUS, 2 * ITEM_SNAP_RADIUS, 2 * ITEM_SNAP_RADIUS),
            ItemEntity::isAlive)
        .stream()
        .min(Comparator.comparingDouble(item -> item.squaredDistanceTo(spot)));
  }
}
