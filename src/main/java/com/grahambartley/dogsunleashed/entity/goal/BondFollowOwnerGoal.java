package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Set;
import net.minecraft.entity.LivingEntity;

public class BondFollowOwnerGoal extends CommandFollowOwnerGoal {

  private final float startDistance;

  public BondFollowOwnerGoal(
      final UnleashedDogEntity dog,
      final double speed,
      final float maxDistance,
      final float minDistance,
      final Set<DogCommand> activeCommands) {
    super(
        dog,
        speed,
        (float) DogBondStep.DEVOTED.followDistance(maxDistance),
        minDistance,
        activeCommands);
    this.startDistance = maxDistance;
  }

  static boolean farEnough(
      final DogBondStep step, final float startDistance, final double squaredDistanceToOwner) {
    final double distance = step.followDistance(startDistance);
    return squaredDistanceToOwner >= distance * distance;
  }

  @Override
  public boolean canStart() {
    final LivingEntity owner = this.dog.getOwner();
    return super.canStart()
        && owner != null
        && farEnough(this.bond(), this.startDistance, this.dog.squaredDistanceTo(owner));
  }
}
