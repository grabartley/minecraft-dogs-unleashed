package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;

public class PointedFetchGoal extends Goal {

  private static final double RETRIEVE_SPEED = 1.3;
  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;
  private static final int REPATH_INTERVAL_TICKS = 10;

  private final UnleashedDogEntity dog;
  private int repathCooldown;
  private int fetchTicks;

  public PointedFetchGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final ItemEntity item = this.orders().fetchItem();
    if (item == null) {
      return false;
    }
    if (!this.isFetchable(item)) {
      this.orders().clearFetch();
      return false;
    }
    return this.isFree();
  }

  @Override
  public boolean shouldContinue() {
    final ItemEntity item = this.orders().fetchItem();
    return item != null && this.isFetchable(item) && this.isFree();
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
    this.fetchTicks = 0;
  }

  @Override
  public void tick() {
    final ItemEntity item = this.orders().fetchItem();
    if (item == null) {
      return;
    }
    final PointedFetchRules.Step step =
        PointedFetchRules.step(
            ++this.fetchTicks, item.cannotPickup(), this.dog.squaredDistanceTo(item));
    if (step == PointedFetchRules.Step.GIVE_UP) {
      this.orders().clearFetch();
      return;
    }
    this.dog.getLookControl().lookAt(item, LOOK_YAW, LOOK_PITCH);
    if (step == PointedFetchRules.Step.PICK_UP) {
      this.pickUp(item);
      return;
    }
    if (this.repathCooldown-- > 0) {
      return;
    }
    this.repathCooldown = REPATH_INTERVAL_TICKS;
    this.dog.getNavigation().startMovingTo(item, RETRIEVE_SPEED);
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }

  private void pickUp(final ItemEntity item) {
    final ItemStack stack = item.getStack().copy();
    item.discard();
    this.orders().clearFetch();
    this.dog.getFetchCarry().setActiveType(null);
    this.dog.getFetchCarry().setCarriedStack(stack);
    this.dog.getFetchCarry().setCarrying(true);
  }

  private boolean isFetchable(final ItemEntity item) {
    return item.isAlive() && !item.getStack().isEmpty() && item.getWorld() == this.dog.getWorld();
  }

  private boolean isFree() {
    return !this.dog.getFetchCarry().isCarrying()
        && !this.dog.isInSittingPose()
        && !this.dog.isLeashed();
  }

  private DogPointedOrders orders() {
    return this.dog.getComponents().get(DogPointedOrders.class);
  }
}
