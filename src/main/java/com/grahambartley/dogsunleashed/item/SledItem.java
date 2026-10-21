package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SledItem extends Item implements GeoItem {

  private static final Predicate<Entity> BLOCKERS =
      EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);
  private static final double REACH = 5.0;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public SledItem(final Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack stack = user.getStackInHand(hand);
    final HitResult hit = raycast(world, user, RaycastContext.FluidHandling.NONE);
    if (hit.getType() != HitResult.Type.BLOCK || this.lookingThroughAnEntity(world, user)) {
      return TypedActionResult.pass(stack);
    }
    final Vec3d at = hit.getPos();
    final SledEntity sled = new SledEntity(ModEntities.SLED, world);
    sled.refreshPositionAndAngles(at.x, at.y, at.z, user.getYaw(), 0.0f);
    sled.placeFacing(user.getYaw());
    if (world instanceof ServerWorld serverWorld) {
      final Consumer<SledEntity> copier = EntityType.copier(serverWorld, stack, user);
      copier.accept(sled);
    }
    if (!world.isSpaceEmpty(sled, sled.getBoundingBox())) {
      return TypedActionResult.fail(stack);
    }
    if (!world.isClient) {
      world.spawnEntity(sled);
      world.emitGameEvent(user, GameEvent.ENTITY_PLACE, at);
      stack.decrementUnlessCreative(1, user);
    }
    user.incrementStat(Stats.USED.getOrCreateStat(this));
    return TypedActionResult.success(stack, world.isClient);
  }

  private boolean lookingThroughAnEntity(final World world, final PlayerEntity user) {
    final Vec3d look = user.getRotationVec(1.0f);
    final List<Entity> near =
        world.getOtherEntities(
            user, user.getBoundingBox().stretch(look.multiply(REACH)).expand(1.0), BLOCKERS);
    final Vec3d eyes = user.getEyePos();
    for (final Entity entity : near) {
      final Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
      if (box.contains(eyes)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {}

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
