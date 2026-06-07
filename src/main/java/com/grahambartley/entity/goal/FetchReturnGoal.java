package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FetchReturnGoal extends Goal {
  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;
  private static final double RETURN_SPEED = 1.5;
  private static final double BALL_DROP_Y_OFFSET = 0.25;
  private static final int SAFE_DROP_RADIUS = 2;
  private static final int SEARCH_MIN_DY = -1;
  private static final int SEARCH_MAX_DY = 1;
  private static final double CLOSE_ENOUGH_DISTANCE = 3.0;

  private final UnleashedDogEntity dog;

  public FetchReturnGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isCarryingFetchItem()) {
      return false;
    }
    Entity owner = this.dog.getOwner();
    return owner instanceof PlayerEntity player && player.isAlive();
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isCarryingFetchItem()) {
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

    this.dog.getLookControl().lookAt(player, LOOK_YAW, LOOK_PITCH);
    this.dog
        .getNavigation()
        .startMovingTo(player.getX(), player.getY(), player.getZ(), RETURN_SPEED);

    double distToOwner = this.dog.squaredDistanceTo(player);
    if (distToOwner <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      this.placeReturnedFetchItem(player);

      final String dogName = this.dog.getDisplayName().getString();
      player.sendMessage(
          Text.translatable("message.dogs-unleashed.play_ball_returned", dogName), true);

      this.dog.setCarryingFetchItem(false);
      this.dog.setActiveFetchBlockPos(null);
    }
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }

  private void placeReturnedFetchItem(PlayerEntity player) {
    FetchItemType fetchItemType = this.getActiveFetchType();
    BlockPos dropPos = findSafeDropPos(player);
    if (dropPos != null) {
      this.dog.getWorld().setBlockState(dropPos, fetchItemType.landedBlock().getDefaultState());
      return;
    }

    ItemEntity itemEntity =
        new ItemEntity(
            this.dog.getWorld(),
            player.getX(),
            player.getY() + BALL_DROP_Y_OFFSET,
            player.getZ(),
            new ItemStack(fetchItemType.item()));
    this.dog.getWorld().spawnEntity(itemEntity);
  }

  private FetchItemType getActiveFetchType() {
    FetchItemType fetchItemType = this.dog.getActiveFetchType();
    if (fetchItemType != null) {
      return fetchItemType;
    }

    BlockPos activeFetchBlockPos = this.dog.getActiveFetchBlockPos();
    if (activeFetchBlockPos != null) {
      FetchItemType resolvedType =
          FetchTypes.forBlock(this.dog.getWorld().getBlockState(activeFetchBlockPos).getBlock());
      if (resolvedType != null) {
        return resolvedType;
      }
    }

    return FetchTypes.TENNIS_BALL;
  }

  private BlockPos findSafeDropPos(PlayerEntity player) {
    BlockPos playerPos = player.getBlockPos();
    BlockPos dogPos = this.dog.getBlockPos();

    BlockPos dropPos = findSafeDropPosNear(playerPos, SAFE_DROP_RADIUS);
    if (dropPos != null) {
      return dropPos;
    }

    return findSafeDropPosNear(dogPos, SAFE_DROP_RADIUS);
  }

  private BlockPos findSafeDropPosNear(BlockPos origin, int radius) {
    for (int currentRadius = 0; currentRadius <= radius; currentRadius++) {
      for (int dy = SEARCH_MAX_DY; dy >= SEARCH_MIN_DY; dy--) {
        for (int dx = -currentRadius; dx <= currentRadius; dx++) {
          for (int dz = -currentRadius; dz <= currentRadius; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != currentRadius) {
              continue;
            }

            BlockPos candidate = origin.add(dx, dy, dz);
            if (isSafeDropPos(candidate)) {
              return candidate;
            }
          }
        }
      }
    }

    return null;
  }

  private boolean isSafeDropPos(BlockPos pos) {
    World world = this.dog.getWorld();
    var stateAtPos = world.getBlockState(pos);
    var stateBelow = world.getBlockState(pos.down());

    boolean canReplace = stateAtPos.isAir() || stateAtPos.isReplaceable();
    boolean hasValidGround = !stateBelow.isAir() && stateBelow.isSolidBlock(world, pos.down());
    boolean notInFluid =
        !world.getFluidState(pos).isIn(FluidTags.WATER)
            && !world.getFluidState(pos).isIn(FluidTags.LAVA);

    return canReplace && hasValidGround && notInFluid;
  }
}
