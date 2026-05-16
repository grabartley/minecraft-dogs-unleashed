package com.grahambartley.entity.goal;

import com.grahambartley.entity.TennisBallProjectileEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class FetchChaseGoal extends Goal {
  private static final int SEARCH_XZ_RANGE = 128;
  private static final int SEARCH_Y_RANGE = 64;
  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;
  private static final double CHASE_SPEED = 1.5;

  private final UnleashedDogEntity dog;
  private TennisBallProjectileEntity targetBall;

  public FetchChaseGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.getActiveBallBlockPos() != null) {
      return false;
    }
    return this.findTargetBall();
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.getActiveBallBlockPos() != null) {
      return false;
    }
    if (this.targetBall == null || this.targetBall.isRemoved()) {
      return false;
    }
    return true;
  }

  private boolean findTargetBall() {
    if (this.dog.getPlayPartnerPlayerUuid() == null) {
      return false;
    }
    java.util.UUID playerUuid = this.dog.getPlayPartnerPlayerUuid();
    List<TennisBallProjectileEntity> balls =
        this.dog
            .getWorld()
            .getEntitiesByClass(
                TennisBallProjectileEntity.class,
                this.dog.getBoundingBox().expand(SEARCH_XZ_RANGE, SEARCH_Y_RANGE, SEARCH_XZ_RANGE),
                ball ->
                    ball.getOwner() instanceof PlayerEntity p && playerUuid.equals(p.getUuid()));
    if (balls.isEmpty()) {
      return false;
    }
    this.targetBall = balls.get(0);
    return true;
  }

  @Override
  public void tick() {
    if (this.targetBall == null || this.targetBall.isRemoved()) {
      return;
    }
    this.dog.getLookControl().lookAt(this.targetBall, LOOK_YAW, LOOK_PITCH);
    this.dog
        .getNavigation()
        .startMovingTo(
            this.targetBall.getX(), this.targetBall.getY(), this.targetBall.getZ(), CHASE_SPEED);
  }

  @Override
  public void stop() {
    this.targetBall = null;
    this.dog.getNavigation().stop();
  }
}
