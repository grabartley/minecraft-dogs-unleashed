package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Set;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;

public class CommandFollowOwnerGoal extends FollowOwnerGoal {

  private final UnleashedDogEntity dog;
  private final Set<DogCommand> activeCommands;

  public CommandFollowOwnerGoal(
      final UnleashedDogEntity dog,
      final double speed,
      final float maxDistance,
      final float minDistance,
      final Set<DogCommand> activeCommands) {
    super(dog, speed, maxDistance, minDistance);
    this.dog = dog;
    this.activeCommands = activeCommands;
  }

  @Override
  public boolean canStart() {
    return !this.dog.isLeashed()
        && this.activeCommands.contains(this.dog.getCommand())
        && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return !this.dog.isLeashed()
        && this.activeCommands.contains(this.dog.getCommand())
        && super.shouldContinue();
  }
}
