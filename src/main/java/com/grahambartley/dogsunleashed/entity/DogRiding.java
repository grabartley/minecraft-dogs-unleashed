package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogRiding {

  static final float SEAT_HEIGHT = 0.62f;
  static final float SEAT_FORWARD = 0.08f;
  static final float STRAFE_SCALE = 0.5f;
  static final float REVERSE_SCALE = 0.35f;
  static final float ACCELERATION = 0.12f;
  static final float LEAP_FORWARD_BOOST = 0.35f;

  public static final float RIDE_SPEED_SCALE = 0.55f;
  static final double REACH_ALLOWANCE = 2.0;

  private final UnleashedDogEntity dog;
  private final ChargedJump chargedJump = new ChargedJump();
  private float pendingLeapPower;
  private float rideSpeed;
  private float stamina;
  private boolean staminaKnown;

  DogRiding(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static float approach(final float current, final float target) {
    return current + (target - current) * ACCELERATION;
  }

  public boolean applies() {
    return this.dog.getGrowth().applies();
  }

  public boolean isRideableStage() {
    return this.applies() && this.dog.getGrowth().stage().rideable();
  }

  public boolean canCarry(final PlayerEntity player) {
    return this.isRideableStage()
        && this.dog.isAlive()
        && this.dog.isTamed()
        && this.dog.isOwner(player)
        && !this.dog.isSitting()
        && !this.dog.isInSittingPose()
        && !this.dog.isSleepingInBed()
        && !this.dog.isCommandedToSleep()
        && !this.dog.getPlaySession().isInPlayMode()
        && !this.dog.isLeashed()
        && !this.dog.hasPassengers()
        && !player.hasVehicle();
  }

  public @Nullable String refusalKey(final PlayerEntity player) {
    if (!this.applies()) {
      return "screen.dogs-unleashed.command_wheel.ride.breed";
    }
    if (!this.isRideableStage()) {
      return "screen.dogs-unleashed.command_wheel.ride.too_young";
    }
    if (!this.isWithinReach(player)) {
      return "screen.dogs-unleashed.command_wheel.ride.too_far";
    }
    return this.canCarry(player) ? null : "screen.dogs-unleashed.command_wheel.ride.busy";
  }

  public boolean isWithinReach(final PlayerEntity player) {
    return player.canInteractWithEntity(this.dog, REACH_ALLOWANCE);
  }

  public boolean mount(final PlayerEntity player) {
    if (!this.canCarry(player) || !this.isWithinReach(player)) {
      return false;
    }
    this.dog.getNavigation().stop();
    this.dog.setTarget(null);
    return player.startRiding(this.dog);
  }

  public @Nullable PlayerEntity rider() {
    return this.dog.getFirstPassenger() instanceof PlayerEntity player && this.dog.isOwner(player)
        ? player
        : null;
  }

  public boolean isRidden() {
    return this.rider() != null;
  }

  public float stamina() {
    return this.stamina;
  }

  public float staminaFraction() {
    return this.dog.getDataTracker().get(DogMountEntity.STAMINA);
  }

  public boolean isLockedOut() {
    return this.dog.getDataTracker().get(DogMountEntity.STAMINA_LOCKED_OUT);
  }

  public float capacity() {
    return this.isRideableStage()
        ? DireWolfStamina.capacity(this.dog.getGrowth().stage(), settings())
        : 0.0f;
  }

  public boolean isSprinting() {
    return this.isRidden()
        && DireWolfStamina.sprints(
            new DireWolfStamina.State(this.staminaFraction(), this.isLockedOut()),
            this.rider() != null && this.rider().isSprinting(),
            this.dog.isTouchingWater());
  }

  public void setJumpHeld(final boolean jump) {
    final float power = this.chargedJump.tick(jump);
    if (power > 0.0f) {
      this.pendingLeapPower = power;
    }
  }

  public float jumpCharge() {
    return this.chargedJump.charge();
  }

  void tick() {
    if (!this.isRideableStage()) {
      return;
    }
    if (!this.staminaKnown) {
      this.staminaKnown = true;
      this.store(new DireWolfStamina.State(this.capacity(), false));
    }
    final DireWolfStamina.State next =
        DireWolfStamina.next(
            new DireWolfStamina.State(this.stamina(), this.isLockedOut()),
            this.capacity(),
            this.isRidden(),
            this.isSprinting(),
            settings());
    this.store(next);
  }

  Vec3d movementInput(final PlayerEntity rider) {
    final float forward =
        rider.forwardSpeed > 0.0f ? rider.forwardSpeed : rider.forwardSpeed * REVERSE_SCALE;
    return new Vec3d(rider.sidewaysSpeed * STRAFE_SCALE, 0.0, forward);
  }

  float speed() {
    this.rideSpeed = approach(this.rideSpeed, this.targetSpeed());
    return this.rideSpeed;
  }

  public float pace() {
    final float gallop = this.baseSpeed() * DireWolfStamina.SPRINT_SPEED_MULTIPLIER;
    return gallop <= 0.0f ? 0.0f : this.rideSpeed / gallop;
  }

  private float baseSpeed() {
    return (float) this.dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
        * RIDE_SPEED_SCALE;
  }

  public float targetSpeed() {
    final float base = this.baseSpeed();
    return this.isSprinting() ? base * DireWolfStamina.SPRINT_SPEED_MULTIPLIER : base;
  }

  public void leapIfAsked(final Vec3d movementInput) {
    if (this.dog.isLogicalSideForUpdatingMovement()
        && this.pendingLeapPower > 0.0f
        && this.dog.isOnGround()) {
      this.leap(movementInput, this.pendingLeapPower);
    }
  }

  Vec3d seat(final EntityDimensions dimensions) {
    return new Vec3d(0.0, dimensions.height() * SEAT_HEIGHT, dimensions.width() * SEAT_FORWARD);
  }

  void writeNbt(final NbtCompound nbt) {
    if (this.isRideableStage()) {
      nbt.putFloat(ModNbtKeys.STAMINA, this.stamina());
    }
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.STAMINA, NbtElement.NUMBER_TYPE)) {
      final float stamina = nbt.getFloat(ModNbtKeys.STAMINA);
      this.staminaKnown = true;
      this.store(new DireWolfStamina.State(stamina, stamina <= 0.0f));
    }
  }

  private void leap(final Vec3d movementInput, final float power) {
    final Vec3d velocity = this.dog.getVelocity();
    this.dog.setVelocity(velocity.x, this.dog.jumpVelocity(power), velocity.z);
    this.dog.velocityDirty = true;
    if (movementInput.z > 0.0) {
      final float yaw = this.dog.getYaw() * MathHelper.RADIANS_PER_DEGREE;
      this.dog.setVelocity(
          this.dog
              .getVelocity()
              .add(
                  -MathHelper.sin(yaw) * LEAP_FORWARD_BOOST * power,
                  0.0,
                  MathHelper.cos(yaw) * LEAP_FORWARD_BOOST * power));
    }
    this.pendingLeapPower = 0.0f;
  }

  private void store(final DireWolfStamina.State state) {
    this.stamina = state.stamina();
    final float capacity = this.capacity();
    final float fraction = capacity > 0.0f ? state.stamina() / capacity : 0.0f;
    if (this.staminaFraction() != fraction) {
      this.dog.getDataTracker().set(DogMountEntity.STAMINA, fraction);
    }
    if (this.isLockedOut() != state.lockedOut()) {
      this.dog.getDataTracker().set(DogMountEntity.STAMINA_LOCKED_OUT, state.lockedOut());
    }
  }

  private static DireWolfStaminaSettings settings() {
    return DogsUnleashed.SERVER_CONFIG.direWolf().stamina();
  }
}
