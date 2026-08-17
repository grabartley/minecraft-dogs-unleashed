package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.MINECRAFT_TICK_RATE;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class DogAmbienceEffects {

  static final int TAIL_WAG_DURATION_TICKS = (int) (3.75f * MINECRAFT_TICK_RATE);
  static final int SHAKE_DURATION_TICKS = 22;

  private static final int RANDOM_TAIL_WAG_CHANCE = 200;
  private static final int SHAKE_PARTICLE_START_TICK = 10;
  private static final int SHAKE_DELAY_TICKS = 20;
  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final int SHAKE_PARTICLE_COUNT = 20;
  private static final double SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE = 0.5;
  private static final double SHAKE_PARTICLE_VERTICAL_OFFSET_RANGE = 0.5;
  private static final double SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE = 0.3;
  private static final double SHAKE_PARTICLE_VERTICAL_VELOCITY_RANGE = 0.1;
  private static final double SHAKE_PARTICLE_SPEED = 0.1;
  private static final int REUNION_COOLDOWN_TICKS = 60 * MINECRAFT_TICK_RATE;
  private static final int REUNION_HEART_PARTICLE_MIN_COUNT = 4;
  private static final int REUNION_HEART_PARTICLE_MAX_COUNT = 6;
  private static final double REUNION_HEART_HORIZONTAL_OFFSET_RANGE = 0.6;
  private static final double REUNION_HEART_VERTICAL_OFFSET_RANGE = 0.7;

  private final UnleashedDogEntity dog;

  private boolean wasInWater = false;
  private int ticksSinceLeftWater = 0;
  private int lastReunionAge = -1;
  private boolean pendingBirthWakeHearts = false;

  DogAmbienceEffects(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static boolean shouldWagForPlayer(
      final boolean tamed, final boolean holdingTamingItem, final boolean holdingBreedingItem) {
    return (!tamed && holdingTamingItem) || (tamed && holdingBreedingItem);
  }

  /**
   * The random wag roll is a supplier because the original only reaches for the entity RNG in the
   * one branch that needs it, and drawing an extra number would shift every later roll that tick.
   */
  public static int nextTailWagTimer(
      final boolean receptive,
      final boolean shouldWag,
      final int currentTimer,
      final BooleanSupplier randomWagHit) {
    if (!receptive) {
      return currentTimer > 0 ? currentTimer - 1 : currentTimer;
    }
    if (shouldWag) {
      return TAIL_WAG_DURATION_TICKS;
    }
    if (currentTimer > 0) {
      return currentTimer - 1;
    }
    return randomWagHit.getAsBoolean() ? TAIL_WAG_DURATION_TICKS : currentTimer;
  }

  public static int nextTicksSinceLeftWater(
      final boolean inWater, final boolean wasInWater, final int ticksSinceLeftWater) {
    if (inWater) {
      return 0;
    }
    if (wasInWater) {
      return 1;
    }
    return ticksSinceLeftWater > 0 ? ticksSinceLeftWater + 1 : ticksSinceLeftWater;
  }

  public static boolean isShakeStartTick(final boolean inWater, final int ticksSinceLeftWater) {
    return !inWater && ticksSinceLeftWater == SHAKE_DELAY_TICKS;
  }

  public static boolean isShakeParticleTick(final int shakeProgress) {
    return SHAKE_DURATION_TICKS - shakeProgress == SHAKE_PARTICLE_START_TICK;
  }

  void updateSocialCues(final @Nullable PlayerEntity nearbyPlayer) {
    this.dog.setHeadTilting(
        nearbyPlayer != null && this.dog.isPlayerHoldingTamingOrBreedingItem(nearbyPlayer));
    this.updateTailWag(nearbyPlayer);
  }

  void tickShakeOff() {
    final boolean inWater = this.dog.isTouchingWater();
    this.ticksSinceLeftWater =
        nextTicksSinceLeftWater(inWater, this.wasInWater, this.ticksSinceLeftWater);
    if (isShakeStartTick(inWater, this.ticksSinceLeftWater) && !this.dog.isShaking()) {
      this.startShaking();
    }
    this.wasInWater = inWater;

    final int shakeProgress = this.dog.getShakeProgress();
    if (shakeProgress > 0) {
      if (isShakeParticleTick(shakeProgress)) {
        this.spawnShakeParticles();
      }
      this.dog.setShakeProgress(shakeProgress - 1);
    }
  }

  void startTailWag() {
    this.dog.setTailWagTimer(TAIL_WAG_DURATION_TICKS);
  }

  public void celebrateOwnerArrival() {
    if (!(this.dog.getWorld() instanceof ServerWorld serverWorld) || !this.dog.isAlive()) {
      return;
    }
    if (this.lastReunionAge != -1 && this.dog.age - this.lastReunionAge < REUNION_COOLDOWN_TICKS) {
      return;
    }
    this.lastReunionAge = this.dog.age;
    this.startTailWag();
    this.spawnHeartParticles(serverWorld);
  }

  void burstHearts() {
    if (this.dog.getWorld() instanceof ServerWorld serverWorld) {
      this.spawnHeartParticles(serverWorld);
    }
  }

  void armBirthWakeHearts() {
    this.pendingBirthWakeHearts = true;
  }

  public boolean hasPendingBirthWakeHearts() {
    return this.pendingBirthWakeHearts;
  }

  void releaseBirthWakeHearts() {
    if (this.pendingBirthWakeHearts && this.dog.getWorld() instanceof ServerWorld serverWorld) {
      this.spawnHeartParticles(serverWorld);
      this.pendingBirthWakeHearts = false;
    }
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putInt(ModNbtKeys.SHAKE_PROGRESS, this.dog.getShakeProgress());
    nbt.putBoolean(ModNbtKeys.WAS_IN_WATER, this.wasInWater);
    nbt.putInt(ModNbtKeys.TICKS_SINCE_LEFT_WATER, this.ticksSinceLeftWater);
    nbt.putBoolean(ModNbtKeys.PENDING_BIRTH_WAKE_HEARTS, this.pendingBirthWakeHearts);
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.SHAKE_PROGRESS, NbtElement.NUMBER_TYPE)) {
      this.dog.setShakeProgress(nbt.getInt(ModNbtKeys.SHAKE_PROGRESS));
    }
    if (nbt.contains(ModNbtKeys.WAS_IN_WATER)) {
      this.wasInWater = nbt.getBoolean(ModNbtKeys.WAS_IN_WATER);
    }
    if (nbt.contains(ModNbtKeys.TICKS_SINCE_LEFT_WATER, NbtElement.NUMBER_TYPE)) {
      this.ticksSinceLeftWater = nbt.getInt(ModNbtKeys.TICKS_SINCE_LEFT_WATER);
    }
    if (nbt.contains(ModNbtKeys.PENDING_BIRTH_WAKE_HEARTS)) {
      this.pendingBirthWakeHearts = nbt.getBoolean(ModNbtKeys.PENDING_BIRTH_WAKE_HEARTS);
    }
  }

  private void updateTailWag(final @Nullable PlayerEntity nearbyPlayer) {
    final int currentTimer = this.dog.getTailWagTimerTicks();
    final boolean receptive = !this.dog.isInSittingPose() && this.dog.getAngerTime() <= 0;

    boolean shouldWag = false;
    if (receptive && nearbyPlayer != null) {
      shouldWag =
          shouldWagForPlayer(
              this.dog.isTamed(),
              this.dog.isTamingItem(nearbyPlayer.getMainHandStack())
                  || this.dog.isTamingItem(nearbyPlayer.getOffHandStack()),
              this.dog.isBreedingItem(nearbyPlayer.getMainHandStack())
                  || this.dog.isBreedingItem(nearbyPlayer.getOffHandStack()));
    }

    this.dog.setTailWagTimer(
        nextTailWagTimer(
            receptive,
            shouldWag,
            currentTimer,
            () -> this.dog.isTamed() && this.dog.getRandom().nextInt(RANDOM_TAIL_WAG_CHANCE) == 0));
  }

  private void startShaking() {
    if (!this.dog.isShaking() && !this.dog.isInSittingPose()) {
      this.dog.setShakeProgress(SHAKE_DURATION_TICKS);
    }
  }

  private void spawnShakeParticles() {
    if (!(this.dog.getWorld() instanceof ServerWorld serverWorld)) {
      return;
    }
    for (int i = 0; i < SHAKE_PARTICLE_COUNT; i++) {
      final double offsetX =
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE;
      final double offsetY =
          this.dog.getRandom().nextDouble() * SHAKE_PARTICLE_VERTICAL_OFFSET_RANGE;
      final double offsetZ =
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * SHAKE_PARTICLE_HORIZONTAL_OFFSET_RANGE;
      serverWorld.spawnParticles(
          ParticleTypes.SPLASH,
          this.dog.getX() + offsetX,
          this.dog.getY() + offsetY + POSITION_CENTER_OFFSET,
          this.dog.getZ() + offsetZ,
          1,
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE,
          this.dog.getRandom().nextDouble() * SHAKE_PARTICLE_VERTICAL_VELOCITY_RANGE,
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * SHAKE_PARTICLE_HORIZONTAL_VELOCITY_RANGE,
          SHAKE_PARTICLE_SPEED);
    }
  }

  private void spawnHeartParticles(final ServerWorld serverWorld) {
    final int count =
        REUNION_HEART_PARTICLE_MIN_COUNT
            + this.dog
                .getRandom()
                .nextInt(REUNION_HEART_PARTICLE_MAX_COUNT - REUNION_HEART_PARTICLE_MIN_COUNT + 1);
    for (int i = 0; i < count; i++) {
      final double offsetX =
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * REUNION_HEART_HORIZONTAL_OFFSET_RANGE;
      final double offsetY =
          this.dog.getRandom().nextDouble() * REUNION_HEART_VERTICAL_OFFSET_RANGE;
      final double offsetZ =
          (this.dog.getRandom().nextDouble() - POSITION_CENTER_OFFSET)
              * REUNION_HEART_HORIZONTAL_OFFSET_RANGE;
      serverWorld.spawnParticles(
          ParticleTypes.HEART,
          this.dog.getX() + offsetX,
          this.dog.getEyeY() + offsetY,
          this.dog.getZ() + offsetZ,
          1,
          0.0,
          0.0,
          0.0,
          0.0);
    }
  }
}
