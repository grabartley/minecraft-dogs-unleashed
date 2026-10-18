package com.grahambartley.dogsunleashed.entity.goal;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public abstract class OwnTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {

  private @Nullable LivingEntity chosen;

  protected OwnTargetGoal(
      final MobEntity mob,
      final Class<T> targetClass,
      final int reciprocalChance,
      final Predicate<LivingEntity> targetPredicate) {
    super(mob, targetClass, reciprocalChance, true, false, targetPredicate);
  }

  protected abstract boolean stillWants(LivingEntity target);

  @Override
  public boolean canStart() {
    return this.mob.getTarget() == null && super.canStart();
  }

  @Override
  public void start() {
    super.start();
    this.chosen = this.mob.getTarget();
  }

  @Override
  public boolean shouldContinue() {
    final LivingEntity target = this.mob.getTarget();
    return target != null
        && target == this.chosen
        && this.stillWants(target)
        && super.shouldContinue();
  }

  @Override
  public void stop() {
    final LivingEntity current = this.mob.getTarget();
    super.stop();
    if (current != null && current != this.chosen) {
      this.mob.setTarget(current);
    }
    this.chosen = null;
  }
}
