package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class FetchReturnGoal extends Goal {

  private static final double CLOSE_ENOUGH_DISTANCE = 3.0;

  private final UnleashedDogEntity dog;

  public FetchReturnGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isCarryingBall()) {
      return false;
    }
    Entity owner = this.dog.getOwner();
    return owner instanceof PlayerEntity player && player.isAlive();
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isCarryingBall()) {
      return false;
    }
    Entity owner = this.dog.getOwner();
    return owner instanceof PlayerEntity player && player.isAlive();
  }

  @Override
  public void tick() {
    Entity owner = this.dog.getOwner();
    if (!(owner instanceof PlayerEntity player) || !player.isAlive()) {
      this.dog.endPlayMode();
      return;
    }

    this.dog.getLookControl().lookAt(player, 10.0f, 10.0f);
    this.dog.getNavigation().startMovingTo(player.getX(), player.getY(), player.getZ(), 1.5);

    double distToOwner = this.dog.squaredDistanceTo(player);
    if (distToOwner <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      BlockPos dropPos = this.dog.getBlockPos();
      if (this.dog.getWorld().getBlockState(dropPos).isAir()) {
        this.dog.getWorld().setBlockState(dropPos, ModBlocks.TENNIS_BALL.getDefaultState());
      }

      final String dogName = this.dog.getDisplayName().getString();
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.play_ball_returned", dogName), true);

      this.dog.setCarryingBall(false);
      this.dog.endPlayMode();
    }
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }
}
