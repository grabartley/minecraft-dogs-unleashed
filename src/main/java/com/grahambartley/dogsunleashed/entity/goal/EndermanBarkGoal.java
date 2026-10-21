package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DimensionReactionRules;
import com.grahambartley.dogsunleashed.entity.DogDimensionReaction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EndermanEntity;
import org.jetbrains.annotations.Nullable;

public class EndermanBarkGoal extends Goal {

  private static final float LOOK_YAW = 30.0f;
  private static final float LOOK_PITCH = 30.0f;

  private final UnleashedDogEntity dog;
  private @Nullable EndermanEntity enderman;
  private long watchTicks;

  public EndermanBarkGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.watches()) {
      return false;
    }
    this.enderman = this.nearestEnderman();
    return this.enderman != null;
  }

  @Override
  public boolean shouldContinue() {
    return this.watches() && this.enderman != null && this.inSight(this.enderman);
  }

  @Override
  public void start() {
    this.watchTicks = 0;
  }

  @Override
  public void tick() {
    if (this.enderman == null) {
      return;
    }
    this.dog.getLookControl().lookAt(this.enderman, LOOK_YAW, LOOK_PITCH);
    if (DimensionReactionRules.barksNow(this.watchTicks)) {
      this.dog.getVocalization().barkIfReady(this.dog.getVocalization().getBarkPitch());
    }
    this.watchTicks++;
  }

  @Override
  public void stop() {
    this.enderman = null;
  }

  public @Nullable EndermanEntity enderman() {
    return this.enderman;
  }

  private boolean watches() {
    return this.dog.isAlive()
        && DimensionReactionRules.watchesEndermen(
            DogDimensionReaction.isInEnd(this.dog),
            this.dog.getSleepController().isAsleep(),
            this.dog.isInSittingPose());
  }

  private @Nullable EndermanEntity nearestEnderman() {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            EndermanEntity.class,
            this.dog.getBoundingBox().expand(DimensionReactionRules.ENDERMAN_BARK_RANGE),
            this::inSight)
        .stream()
        .min(Comparator.comparingDouble(this.dog::squaredDistanceTo))
        .orElse(null);
  }

  private boolean inSight(final EndermanEntity candidate) {
    final double range = DimensionReactionRules.ENDERMAN_BARK_RANGE;
    return candidate.isAlive()
        && this.dog.squaredDistanceTo(candidate) <= range * range
        && this.dog.canSee(candidate);
  }
}
