package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class FetchChaseGoal extends Goal {
  private static final int SEARCH_XZ_RANGE = 128;
  private static final int SEARCH_Y_RANGE = 64;
  private static final float LOOK_YAW = 10.0f;
  private static final float LOOK_PITCH = 10.0f;
  private static final double CHASE_SPEED = 1.5;

  private final UnleashedDogEntity dog;
  private Entity targetFetchProjectile;

  public FetchChaseGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.getActiveFetchBlockPos() != null) {
      return false;
    }
    return this.findTargetBall();
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isInPlayMode() || this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.getActiveFetchBlockPos() != null) {
      return false;
    }
    return this.targetFetchProjectile != null && !this.targetFetchProjectile.isRemoved();
  }

  private boolean findTargetBall() {
    if (this.dog.getPlayPartnerPlayerUuid() == null) {
      return false;
    }
    java.util.UUID playerUuid = this.dog.getPlayPartnerPlayerUuid();
    List<Entity> fetchProjectiles =
        this.dog
            .getWorld()
            .getEntitiesByClass(
                Entity.class,
                this.dog.getBoundingBox().expand(SEARCH_XZ_RANGE, SEARCH_Y_RANGE, SEARCH_XZ_RANGE),
                entity ->
                    entity instanceof FetchProjectileEntity
                        && entity instanceof ProjectileEntity projectile
                        && projectile.getOwner() instanceof PlayerEntity player
                        && playerUuid.equals(player.getUuid()));
    if (fetchProjectiles.isEmpty()) {
      return false;
    }
    this.targetFetchProjectile = fetchProjectiles.get(0);
    this.syncActiveFetchTypeFromTarget();
    return true;
  }

  private void syncActiveFetchTypeFromTarget() {
    if (this.targetFetchProjectile instanceof FetchProjectileEntity fetchProjectileEntity) {
      this.dog.setActiveFetchType(fetchProjectileEntity.getFetchItemType());
      return;
    }

    FetchItemType fetchItemType = FetchTypes.forEntityType(this.targetFetchProjectile.getType());
    if (fetchItemType != null) {
      this.dog.setActiveFetchType(fetchItemType);
    }
  }

  @Override
  public void tick() {
    if (this.targetFetchProjectile == null || this.targetFetchProjectile.isRemoved()) {
      return;
    }
    this.dog.getLookControl().lookAt(this.targetFetchProjectile, LOOK_YAW, LOOK_PITCH);
    this.dog
        .getNavigation()
        .startMovingTo(
            this.targetFetchProjectile.getX(),
            this.targetFetchProjectile.getY(),
            this.targetFetchProjectile.getZ(),
            CHASE_SPEED);
  }

  @Override
  public void stop() {
    this.targetFetchProjectile = null;
    this.dog.getNavigation().stop();
  }
}
