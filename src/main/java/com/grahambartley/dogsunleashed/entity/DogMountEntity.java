package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class DogMountEntity extends TameableEntity {

  static final TrackedData<Float> STAMINA =
      DataTracker.registerData(DogMountEntity.class, TrackedDataHandlerRegistry.FLOAT);
  static final TrackedData<Boolean> STAMINA_LOCKED_OUT =
      DataTracker.registerData(DogMountEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

  protected DogMountEntity(final EntityType<? extends TameableEntity> type, final World world) {
    super(type, world);
  }

  public abstract DogRiding getRiding();

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(STAMINA, 0.0f);
    builder.add(STAMINA_LOCKED_OUT, false);
  }

  @Override
  public @Nullable LivingEntity getControllingPassenger() {
    return this.getRiding().rider();
  }

  @Override
  protected void tickControlled(final PlayerEntity controllingPlayer, final Vec3d movementInput) {
    super.tickControlled(controllingPlayer, movementInput);
    if (this.isLogicalSideForUpdatingMovement()) {
      this.setRotation(
          MountTurning.turn(this.getYaw(), controllingPlayer.getYaw(), this.getRiding().pace()),
          0.0f);
    }
    this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
    this.getRiding().leapIfAsked(movementInput);
  }

  @Override
  protected Vec3d getControlledMovementInput(
      final PlayerEntity controllingPlayer, final Vec3d movementInput) {
    return this.getRiding().movementInput(controllingPlayer);
  }

  @Override
  protected float getSaddledSpeed(final PlayerEntity controllingPlayer) {
    return this.getRiding().speed();
  }

  @Override
  protected Vec3d getPassengerAttachmentPos(
      final Entity passenger, final EntityDimensions dimensions, final float scaleFactor) {
    return this.getRiding().seat(dimensions);
  }

  @Override
  public boolean canSprintAsVehicle() {
    return this.getRiding().isRidden();
  }

  @Override
  public boolean canMoveVoluntarily() {
    return super.canMoveVoluntarily() && !this.getRiding().isRidden();
  }

  float jumpVelocity(final float power) {
    return this.getJumpVelocity(power);
  }
}
