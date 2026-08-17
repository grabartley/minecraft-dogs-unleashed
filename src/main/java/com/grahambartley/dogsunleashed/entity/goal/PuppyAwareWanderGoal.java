package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Wander goal that keeps puppies on a short leash. Adults reuse the vanilla far-wander target
 * selection; babies pick destinations from a tighter horizontal/vertical box so they stay close to
 * where they were born. Dogs on an anchored command (Stay, Guard) bias their wander targets toward
 * the anchor so idle wandering does not constantly fight the anchor leash. The radius is
 * re-evaluated every time a target is chosen, so the dog naturally widens its range the moment
 * {@code isBaby()} flips to false or the command changes.
 */
public class PuppyAwareWanderGoal extends WanderAroundFarGoal {

  private static final int PUPPY_HORIZONTAL_RANGE = 5;
  private static final int PUPPY_VERTICAL_RANGE = 4;
  private static final int ANCHORED_HORIZONTAL_RANGE = 5;
  private static final int ANCHORED_VERTICAL_RANGE = 3;

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
    final BlockPos anchor = this.dog.getCommandController().getAnchorPos();
    if (this.dog.getCommand().isAnchored() && anchor != null) {
      return FuzzyTargeting.findTo(
          this.mob,
          ANCHORED_HORIZONTAL_RANGE,
          ANCHORED_VERTICAL_RANGE,
          Vec3d.ofBottomCenter(anchor));
    }
    return super.getWanderTarget();
  }
}
