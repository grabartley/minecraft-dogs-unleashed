package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.OptionalDouble;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundEvents;

public class LonelyWhineGoal extends Goal {

  private final UnleashedDogEntity dog;
  private long lonelyTicks;

  public LonelyWhineGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.isLonely();
  }

  @Override
  public boolean shouldContinue() {
    return this.isLonely();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.lonelyTicks = 0;
  }

  @Override
  public void tick() {
    this.lonelyTicks++;
    if (LonelyWhineRules.whinesNow(this.lonelyTicks)) {
      this.dog.playSound(
          SoundEvents.ENTITY_WOLF_WHINE, 1.0f, this.dog.getVocalization().getBarkPitch());
    }
  }

  private boolean isLonely() {
    return this.dog.isTamed()
        && this.dog.getComponents().get(DogPersonality.class).traits().whinesWhenLonely()
        && LonelyWhineRules.isLonely(
            this.dog.getCommand(), this.dog.getSleepController().isAsleep(), this.ownerDistance());
  }

  private OptionalDouble ownerDistance() {
    final LivingEntity owner = this.dog.getOwner();
    return owner != null && owner.getWorld() == this.dog.getWorld()
        ? OptionalDouble.of(this.dog.squaredDistanceTo(owner))
        : OptionalDouble.empty();
  }
}
