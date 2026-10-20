package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class PointedAttackGoal extends OwnTargetGoal<LivingEntity> {

  private static final double CHASE_RANGE = 80.0;
  private static final int MAX_TICKS_UNSEEN = 200;

  private final UnleashedDogEntity dog;
  private @Nullable LivingEntity engaged;

  public PointedAttackGoal(final UnleashedDogEntity dog) {
    super(dog, LivingEntity.class, 1, target -> target == orders(dog).attackTarget());
    this.dog = dog;
    this.maxTimeWithoutVisibility = MAX_TICKS_UNSEEN;
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = orders(this.dog).attackTarget();
    if (target == null) {
      return false;
    }
    if (!this.stillWants(target)) {
      orders(this.dog).clearAttack();
      return false;
    }
    return super.canStart();
  }

  @Override
  protected boolean overridesCurrentTarget() {
    return true;
  }

  @Override
  protected void findClosestTarget() {
    this.targetEntity = orders(this.dog).attackTarget();
  }

  @Override
  protected double getFollowRange() {
    return CHASE_RANGE;
  }

  @Override
  protected boolean stillWants(final LivingEntity target) {
    return target == orders(this.dog).attackTarget()
        && target.isAlive()
        && target.getWorld() == this.dog.getWorld()
        && !this.dog.isInSittingPose();
  }

  @Override
  public void start() {
    super.start();
    this.engaged = this.mob.getTarget();
  }

  @Override
  public void stop() {
    super.stop();
    if (orders(this.dog).attackTarget() == this.engaged) {
      orders(this.dog).clearAttack();
    }
    this.engaged = null;
  }

  private static DogPointedOrders orders(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPointedOrders.class);
  }
}
