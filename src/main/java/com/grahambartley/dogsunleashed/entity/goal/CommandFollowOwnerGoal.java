package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;

public class CommandFollowOwnerGoal extends FollowOwnerGoal {

  private final UnleashedDogEntity dog;
  private final double speed;
  private final float startDistance;
  private final boolean closerWithBond;
  private final Set<DogCommand> activeCommands;

  public CommandFollowOwnerGoal(
      final UnleashedDogEntity dog,
      final double speed,
      final float maxDistance,
      final float minDistance,
      final boolean closerWithBond,
      final Set<DogCommand> activeCommands) {
    super(
        dog,
        speed,
        closerWithBond ? (float) DogBondStep.DEVOTED.followDistance(maxDistance) : maxDistance,
        minDistance);
    this.dog = dog;
    this.speed = speed;
    this.startDistance = maxDistance;
    this.closerWithBond = closerWithBond;
    this.activeCommands = activeCommands;
  }

  @Override
  public boolean canStart() {
    return !this.dog.isLeashed()
        && this.activeCommands.contains(this.dog.getCommand())
        && super.canStart()
        && this.farEnoughForBond();
  }

  @Override
  public boolean shouldContinue() {
    return !this.dog.isLeashed()
        && this.activeCommands.contains(this.dog.getCommand())
        && super.shouldContinue();
  }

  @Override
  public void tick() {
    super.tick();
    this.dog.getNavigation().setSpeed(this.bond().responseSpeed(this.speed));
  }

  static boolean farEnough(
      final boolean closerWithBond,
      final DogBondStep step,
      final float startDistance,
      final double squaredDistanceToOwner) {
    if (!closerWithBond) {
      return true;
    }
    final double distance = step.followDistance(startDistance);
    return squaredDistanceToOwner >= distance * distance;
  }

  private boolean farEnoughForBond() {
    final LivingEntity owner = this.dog.getOwner();
    return owner == null
        || farEnough(
            this.closerWithBond,
            this.bond(),
            this.startDistance,
            this.dog.squaredDistanceTo(owner));
  }

  private DogBondStep bond() {
    return this.dog.getComponents().get(DogBond.class).step();
  }
}
