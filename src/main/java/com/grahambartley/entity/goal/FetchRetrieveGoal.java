package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FetchRetrieveGoal extends Goal {
  private static final double TARGET_CENTER_OFFSET = 0.5;
  private static final double RETRIEVE_SPEED = 1.3;
  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;
  private static final int SEARCH_MIN_DY = -10;
  private static final int SEARCH_MAX_DY = 2;
  private static final int SEARCH_HORIZONTAL_RADIUS = 2;
  private static final double CLOSE_ENOUGH_DISTANCE = 2.0;

  private final UnleashedDogEntity dog;
  private BlockPos targetBallPos;

  public FetchRetrieveGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.isCarryingBall()) {
      return false;
    }
    BlockPos ballPos = this.dog.getActiveBallBlockPos();
    if (ballPos == null) {
      return false;
    }
    this.targetBallPos = ballPos;
    return this.isTennisBallAt(this.targetBallPos);
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.isCarryingBall()) {
      return false;
    }
    if (this.targetBallPos == null) {
      return false;
    }
    if (!this.isTennisBallAt(this.targetBallPos)) {
      BlockPos updatedPos = this.findNearbyBall();
      if (updatedPos == null) {
        this.dog.endPlayMode();
        return false;
      }
      this.targetBallPos = updatedPos;
      this.dog.setActiveBallBlockPos(updatedPos);
    }
    return true;
  }

  private boolean isTennisBallAt(BlockPos pos) {
    World world = this.dog.getWorld();
    BlockState state = world.getBlockState(pos);
    return state.isOf(ModBlocks.TENNIS_BALL);
  }

  private BlockPos findNearbyBall() {
    BlockPos origin = this.dog.getActiveBallBlockPos();
    if (origin == null) {
      return null;
    }
    World world = this.dog.getWorld();
    for (int dy = SEARCH_MIN_DY; dy <= SEARCH_MAX_DY; dy++) {
      for (int dx = -SEARCH_HORIZONTAL_RADIUS; dx <= SEARCH_HORIZONTAL_RADIUS; dx++) {
        for (int dz = -SEARCH_HORIZONTAL_RADIUS; dz <= SEARCH_HORIZONTAL_RADIUS; dz++) {
          BlockPos check = origin.add(dx, dy, dz);
          if (world.getBlockState(check).isOf(ModBlocks.TENNIS_BALL)) {
            return check;
          }
        }
      }
    }
    return null;
  }

  @Override
  public void start() {
    if (this.targetBallPos != null) {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetBallPos.getX() + TARGET_CENTER_OFFSET,
              this.targetBallPos.getY(),
              this.targetBallPos.getZ() + TARGET_CENTER_OFFSET,
              RETRIEVE_SPEED);
    }
  }

  @Override
  public void tick() {
    if (this.targetBallPos == null) {
      return;
    }

    this.dog
        .getLookControl()
        .lookAt(
            this.targetBallPos.getX() + TARGET_CENTER_OFFSET,
            this.targetBallPos.getY() + TARGET_CENTER_OFFSET,
            this.targetBallPos.getZ() + TARGET_CENTER_OFFSET,
            LOOK_YAW,
            LOOK_PITCH);

    double distToBall = this.dog.getBlockPos().getSquaredDistance(this.targetBallPos);

    if (distToBall <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      if (this.isTennisBallAt(this.targetBallPos)) {
        this.dog.getWorld().removeBlock(this.targetBallPos, false);
        this.dog.setActiveBallBlockPos(null);
        this.dog.setCarryingBall(true);
      }
    } else {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetBallPos.getX() + TARGET_CENTER_OFFSET,
              this.targetBallPos.getY(),
              this.targetBallPos.getZ() + TARGET_CENTER_OFFSET,
              RETRIEVE_SPEED);
    }
  }

  @Override
  public void stop() {
    this.targetBallPos = null;
    this.dog.getNavigation().stop();
  }
}
