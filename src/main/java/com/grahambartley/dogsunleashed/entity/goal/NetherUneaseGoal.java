package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DimensionReactionRules;
import com.grahambartley.dogsunleashed.entity.DogAnimationKeys;
import com.grahambartley.dogsunleashed.entity.DogDimensionReaction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundEvents;

public class NetherUneaseGoal extends Goal {

  private final UnleashedDogEntity dog;
  private long uneasyTicks;

  public NetherUneaseGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.isUneasy();
  }

  @Override
  public boolean shouldContinue() {
    return this.isUneasy();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.uneasyTicks = 0;
  }

  @Override
  public void tick() {
    if (DimensionReactionRules.whinesNow(this.uneasyTicks, this.dog.getId())) {
      this.dog.playSound(
          SoundEvents.ENTITY_WOLF_WHINE, 1.0f, this.dog.getVocalization().getBarkPitch());
    }
    if (DimensionReactionRules.cowersNow(this.uneasyTicks, this.dog.isInSittingPose())) {
      this.dog.triggerAnim(DogAnimationKeys.COWER, DogAnimationKeys.COWER);
    }
    this.uneasyTicks++;
  }

  private boolean isUneasy() {
    return this.dog.isAlive()
        && !this.dog.getSleepController().isAsleep()
        && DogDimensionReaction.isUneasy(this.dog);
  }
}
