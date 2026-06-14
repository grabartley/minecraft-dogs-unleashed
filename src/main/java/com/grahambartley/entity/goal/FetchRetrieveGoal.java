package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchCarriedItemProvider;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import java.util.EnumSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
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
  private BlockPos targetFetchItemPos;

  public FetchRetrieveGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.isCarryingFetchItem()) {
      return false;
    }
    BlockPos fetchItemPos = this.dog.getActiveFetchBlockPos();
    if (fetchItemPos == null) {
      return false;
    }
    this.targetFetchItemPos = fetchItemPos;
    return this.getFetchItemTypeAt(this.targetFetchItemPos) != null;
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.isCarryingFetchItem()) {
      return false;
    }
    if (this.targetFetchItemPos == null) {
      return false;
    }
    if (this.getFetchItemTypeAt(this.targetFetchItemPos) == null) {
      BlockPos updatedPos = this.findNearbyFetchItem();
      if (updatedPos == null) {
        this.dog.endPlayMode();
        return false;
      }
      this.targetFetchItemPos = updatedPos;
      this.dog.setActiveFetchBlockPos(updatedPos);
    }
    return true;
  }

  private FetchItemType getFetchItemTypeAt(BlockPos pos) {
    return FetchTypes.forBlock(this.dog.getWorld().getBlockState(pos).getBlock());
  }

  private BlockPos findNearbyFetchItem() {
    BlockPos origin = this.dog.getActiveFetchBlockPos();
    if (origin == null) {
      return null;
    }
    World world = this.dog.getWorld();
    for (int dy = SEARCH_MIN_DY; dy <= SEARCH_MAX_DY; dy++) {
      for (int dx = -SEARCH_HORIZONTAL_RADIUS; dx <= SEARCH_HORIZONTAL_RADIUS; dx++) {
        for (int dz = -SEARCH_HORIZONTAL_RADIUS; dz <= SEARCH_HORIZONTAL_RADIUS; dz++) {
          BlockPos check = origin.add(dx, dy, dz);
          if (FetchTypes.forBlock(world.getBlockState(check).getBlock()) != null) {
            return check;
          }
        }
      }
    }
    return null;
  }

  @Override
  public void start() {
    if (this.targetFetchItemPos != null) {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetFetchItemPos.getX() + TARGET_CENTER_OFFSET,
              this.targetFetchItemPos.getY(),
              this.targetFetchItemPos.getZ() + TARGET_CENTER_OFFSET,
              RETRIEVE_SPEED);
    }
  }

  @Override
  public void tick() {
    if (this.targetFetchItemPos == null) {
      return;
    }

    this.dog
        .getLookControl()
        .lookAt(
            this.targetFetchItemPos.getX() + TARGET_CENTER_OFFSET,
            this.targetFetchItemPos.getY() + TARGET_CENTER_OFFSET,
            this.targetFetchItemPos.getZ() + TARGET_CENTER_OFFSET,
            LOOK_YAW,
            LOOK_PITCH);

    double distToFetchItem = this.dog.getBlockPos().getSquaredDistance(this.targetFetchItemPos);

    if (distToFetchItem <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      FetchItemType fetchItemType = this.getFetchItemTypeAt(this.targetFetchItemPos);
      if (fetchItemType != null) {
        ItemStack carriedStack = this.buildCarriedStack(fetchItemType, this.targetFetchItemPos);
        this.dog.getWorld().removeBlock(this.targetFetchItemPos, false);
        this.dog.setActiveFetchBlockPos(null);
        this.dog.setActiveFetchType(fetchItemType);
        this.dog.setCarriedFetchItemStack(carriedStack);
        this.dog.setCarryingFetchItem(true);
      }
    } else {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetFetchItemPos.getX() + TARGET_CENTER_OFFSET,
              this.targetFetchItemPos.getY(),
              this.targetFetchItemPos.getZ() + TARGET_CENTER_OFFSET,
              RETRIEVE_SPEED);
    }
  }

  private ItemStack buildCarriedStack(FetchItemType fetchItemType, BlockPos pos) {
    ItemStack stack = new ItemStack(fetchItemType.item());
    BlockEntity be = this.dog.getWorld().getBlockEntity(pos);
    if (be instanceof FetchCarriedItemProvider provider) {
      provider.enrichCarriedItemStack(stack);
    }
    return stack;
  }

  @Override
  public void stop() {
    this.targetFetchItemPos = null;
    this.dog.getNavigation().stop();
  }
}
