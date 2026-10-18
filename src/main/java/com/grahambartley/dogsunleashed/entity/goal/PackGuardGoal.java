package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogTrustRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PackGuardGoal extends Goal {

  private static final int GUARD_TICKS = 2 * 20;
  private static final double BLOCKING_GAP = 2.0;
  private static final double GUARD_SPEED = 1.2;
  private static final double PUP_SEARCH_RANGE = 16.0;

  private final UnleashedDogEntity dog;
  private @Nullable PlayerEntity intruder;
  private @Nullable UnleashedDogEntity pup;
  private int guardTicks;

  public PackGuardGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  public static boolean isWeapon(final Item item) {
    return item instanceof SwordItem
        || item instanceof AxeItem
        || item instanceof TridentItem
        || item instanceof BowItem
        || item instanceof CrossbowItem
        || item instanceof MaceItem;
  }

  @Override
  public boolean canStart() {
    if (!this.dog.getPack().isWildMember() || !this.dog.getPack().isGrown()) {
      return false;
    }
    for (final UnleashedDogEntity candidate : this.dog.getPack().pupsWithin(PUP_SEARCH_RANGE)) {
      final PlayerEntity provoker = this.provokerNear(candidate);
      if (provoker != null && this.isNearestGuardOf(candidate)) {
        this.pup = candidate;
        this.intruder = provoker;
        return true;
      }
    }
    return false;
  }

  @Override
  public void start() {
    this.guardTicks = GUARD_TICKS;
    this.dog.getGrowl().warn();
    for (final UnleashedDogEntity packPup : this.dog.getPack().pupsWithin(PUP_SEARCH_RANGE)) {
      packPup.getTrust().spook(this.intruder);
    }
  }

  @Override
  public boolean shouldContinue() {
    return this.guardTicks > 0
        && this.intruder != null
        && this.intruder.isAlive()
        && this.pup != null;
  }

  @Override
  public void tick() {
    this.guardTicks--;
    this.dog.getLookControl().lookAt(this.intruder, 30.0f, 30.0f);
    final Vec3d toIntruder = this.intruder.getPos().subtract(this.pup.getPos());
    if (toIntruder.lengthSquared() > 0.0) {
      final Vec3d between = this.pup.getPos().add(toIntruder.normalize().multiply(BLOCKING_GAP));
      this.dog.getNavigation().startMovingTo(between.x, between.y, between.z, GUARD_SPEED);
    }
  }

  @Override
  public void stop() {
    this.intruder = null;
    this.pup = null;
  }

  private @Nullable PlayerEntity provokerNear(final UnleashedDogEntity candidate) {
    return candidate.getWorld().getPlayers().stream()
        .filter(player -> !player.isSpectator() && !player.isCreative())
        .filter(player -> player.distanceTo(candidate) <= DogTrustRules.GUARD_RANGE)
        .filter(
            player ->
                DogTrustRules.provokes(
                    player.isSneaking(),
                    player.isSprinting(),
                    isWeapon(player.getMainHandStack().getItem()),
                    player.distanceTo(candidate)))
        .min(Comparator.comparingDouble(player -> player.distanceTo(candidate)))
        .orElse(null);
  }

  private boolean isNearestGuardOf(final UnleashedDogEntity candidate) {
    return candidate.getPack().packmatesWithin(PUP_SEARCH_RANGE).stream()
        .filter(wolf -> wolf.getPack().isGrown())
        .min(Comparator.comparingDouble(wolf -> wolf.squaredDistanceTo(candidate)))
        .map(nearest -> nearest == this.dog)
        .orElse(false);
  }
}
