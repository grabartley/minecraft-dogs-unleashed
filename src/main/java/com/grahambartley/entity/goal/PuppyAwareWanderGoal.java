package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Wander goal that keeps puppies on a short leash. Adults reuse the vanilla far-wander target
 * selection; babies pick destinations from a tighter horizontal/vertical box so they stay close to
 * where they were born. The radius is re-evaluated every time a target is chosen, so the dog
 * naturally widens its range the moment {@code isBaby()} flips to false.
 */
public class PuppyAwareWanderGoal extends WanderAroundFarGoal {

  private static final int PUPPY_HORIZONTAL_RANGE = 5;
  private static final int PUPPY_VERTICAL_RANGE = 4;

  private final UnleashedDogEntity dog;

  public PuppyAwareWanderGoal(final UnleashedDogEntity dog, final double speed) {
    super(dog, speed);
    this.dog = dog;
  }

  @Override
  @Nullable
  protected Vec3d getWanderTarget() {
    if (this.dog.isBaby()) {
      return FuzzyTargeting.find(this.mob, PUPPY_HORIZONTAL_RANGE, PUPPY_VERTICAL_RANGE);
    }
    return super.getWanderTarget();
  }
}
