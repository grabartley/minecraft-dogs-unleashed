package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FetchRetrieveGoal extends Goal {

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
    for (int dy = -10; dy <= 2; dy++) {
      for (int dx = -2; dx <= 2; dx++) {
        for (int dz = -2; dz <= 2; dz++) {
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
              this.targetBallPos.getX() + 0.5,
              this.targetBallPos.getY(),
              this.targetBallPos.getZ() + 0.5,
              1.3);
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
            this.targetBallPos.getX() + 0.5,
            this.targetBallPos.getY() + 0.5,
            this.targetBallPos.getZ() + 0.5,
            10.0f,
            10.0f);

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
              this.targetBallPos.getX() + 0.5,
              this.targetBallPos.getY(),
              this.targetBallPos.getZ() + 0.5,
              1.3);
    }
  }

  @Override
  public void stop() {
    this.targetBallPos = null;
    this.dog.getNavigation().stop();
  }
}
