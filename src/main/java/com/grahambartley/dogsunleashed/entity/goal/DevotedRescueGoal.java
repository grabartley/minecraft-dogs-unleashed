package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class DevotedRescueGoal extends Goal {

  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;

  private final UnleashedDogEntity dog;
  private int ticksLeft;

  public DevotedRescueGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final PlayerEntity owner = this.availableOwner();
    return owner != null
        && DogBondRules.startsRescue(this.step(), owner.hurtTime > 0, this.dog.distanceTo(owner));
  }

  @Override
  public void start() {
    this.ticksLeft = DogBondRules.RESCUE_TICKS;
  }

  @Override
  public boolean shouldContinue() {
    return this.ticksLeft > 0
        && this.step() == DogBondStep.DEVOTED
        && this.availableOwner() != null;
  }

  @Override
  public void tick() {
    final PlayerEntity owner = this.availableOwner();
    if (owner == null) {
      return;
    }
    this.ticksLeft = DogBondRules.rescueTicksLeft(owner.hurtTime > 0, this.ticksLeft);
    this.dog.getLookControl().lookAt(owner, LOOK_YAW, LOOK_PITCH);
    if (this.dog.distanceTo(owner) > DogBondRules.RESCUE_DISTANCE) {
      this.dog
          .getNavigation()
          .startMovingTo(owner, this.step().responseSpeed(DogBondRules.RESCUE_SPEED));
    } else {
      this.dog.getNavigation().stop();
    }
  }

  @Override
  public void stop() {
    this.ticksLeft = 0;
    this.dog.getNavigation().stop();
  }

  private @Nullable PlayerEntity availableOwner() {
    if (!this.dog.isTamed()
        || this.dog.isLeashed()
        || this.dog.isInSittingPose()
        || !DogBondRules.answersRescue(this.dog.getCommand())
        || !(this.dog.getOwner() instanceof PlayerEntity owner)
        || !owner.isAlive()
        || owner.getWorld() != this.dog.getWorld()) {
      return null;
    }
    return owner;
  }

  private DogBondStep step() {
    return this.dog.getComponents().get(DogBond.class).step();
  }
}
