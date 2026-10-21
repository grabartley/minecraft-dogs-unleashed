package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.BARK_COOLDOWN_TICKS;
import static com.grahambartley.dogsunleashed.ModConstants.BARK_PITCH;
import static com.grahambartley.dogsunleashed.ModConstants.FULL_MOON_PHASE;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_COOLDOWN_TICKS;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_DURATION_TICKS;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_HEARING_RANGE_SQUARED;
import static com.grahambartley.dogsunleashed.ModConstants.HOWL_PITCH;
import static com.grahambartley.dogsunleashed.ModConstants.LOW_HEALTH_THRESHOLD;
import static com.grahambartley.dogsunleashed.ModConstants.PUPPY_BARK_PITCH_MULTIPLIER;
import static com.grahambartley.dogsunleashed.ModConstants.RANDOM_BARK_CHANCE;
import static com.grahambartley.dogsunleashed.ModConstants.RANDOM_HOWL_CHANCE;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.advancement.DogHowledCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

public final class DogVocalization {

  public static final byte BARK_STATUS = 100;
  private static final int NEVER_BARKED = Integer.MIN_VALUE / 2;

  private final UnleashedDogEntity dog;

  private int barkCooldownTicks = 0;
  private int howlCooldownTicks = 0;
  private int howlActiveTicks = 0;
  private int lastBarkAge = NEVER_BARKED;

  DogVocalization(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static float barkPitch(final boolean baby) {
    return baby ? BARK_PITCH * PUPPY_BARK_PITCH_MULTIPLIER : BARK_PITCH;
  }

  public static boolean isBarkReady(
      final boolean hasBarkSound,
      final boolean dead,
      final boolean sleepingInBed,
      final int barkCooldownTicks) {
    return hasBarkSound && !dead && !sleepingInBed && barkCooldownTicks <= 0;
  }

  public static boolean hasBarkTrigger(
      final boolean playerHoldingLure,
      final float health,
      final float maxHealth,
      final boolean leashed,
      final boolean hasTarget) {
    return playerHoldingLure
        || (health < maxHealth * LOW_HEALTH_THRESHOLD && !leashed)
        || hasTarget;
  }

  public static boolean isHowlConditionMet(
      final boolean dead, final boolean sleepingInBed, final boolean day, final int moonPhase) {
    return !dead && !sleepingInBed && !day && moonPhase == FULL_MOON_PHASE;
  }

  public int getBarkCooldownTicks() {
    return this.barkCooldownTicks;
  }

  public int getHowlCooldownTicks() {
    return this.howlCooldownTicks;
  }

  public float getBarkPitch() {
    return barkPitch(this.dog.isBaby());
  }

  @Nullable
  public SoundEvent barkSound() {
    if (this.dog.isUndead()) {
      return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }
    return this.dog.getVoiceBreed().barkSound();
  }

  @Nullable
  public SoundEvent alertSound() {
    if (this.dog.isUndead()) {
      return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }
    return this.dog.getVoiceBreed().alertSound();
  }

  public SoundEvent howlSound() {
    if (this.dog.isUndead()) {
      return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }
    final SoundEvent howl = this.dog.getVoiceBreed().howlSound();
    return howl != null ? howl : ModSounds.HUSKY_HOWL;
  }

  boolean canBark() {
    return isBarkReady(
        this.barkSound() != null,
        this.dog.isDead(),
        this.dog.getSleepController().isAsleep(),
        this.barkCooldownTicks);
  }

  void tick(final @Nullable PlayerEntity nearbyPlayer) {
    if (this.barkCooldownTicks > 0) {
      this.barkCooldownTicks--;
    }
    this.tryBark(nearbyPlayer);
    this.tickHowl();
  }

  void heardBark() {
    this.lastBarkAge = this.dog.age;
  }

  public float ticksSinceBark(final float partialTick) {
    return this.dog.age - this.lastBarkAge + partialTick;
  }

  public void barkIfReady(final float pitch) {
    if (this.canBark()) {
      this.playBark(pitch);
    }
  }

  public void forceBark(final float pitch) {
    if (this.barkSound() != null) {
      this.playBark(pitch);
    }
  }

  public void alertBark() {
    final SoundEvent sound = this.alertSound();
    if (sound != null && !this.dog.isDead()) {
      this.playBark(sound, this.getBarkPitch(), DogsUnleashed.SERVER_CONFIG.sound().barkVolume());
    }
  }

  private void playBark(final float pitch) {
    this.playBark(pitch, DogsUnleashed.SERVER_CONFIG.sound().barkVolume());
  }

  private void playBark(final float pitch, final float volume) {
    this.playBark(this.barkSound(), pitch, volume);
  }

  private void playBark(final SoundEvent sound, final float pitch, final float volume) {
    this.dog.playSound(sound, volume, pitch);
    this.heardBark();
    this.dog.getWorld().sendEntityStatus(this.dog, BARK_STATUS);
    if (this.dog.getRigSourceBreed().barkMovesJaw()) {
      this.dog.triggerAnim(DogAnimationKeys.BARK, DogAnimationKeys.BARK);
    }
    this.barkCooldownTicks = BARK_COOLDOWN_TICKS;
  }

  private void tryBark(final @Nullable PlayerEntity nearbyPlayer) {
    if (this.canBark() && this.shouldBark(nearbyPlayer)) {
      this.playBark(this.getBarkPitch());
    }
  }

  private boolean shouldBark(final @Nullable PlayerEntity nearbyPlayer) {
    final boolean playerHoldingLure =
        nearbyPlayer != null && DogFoods.isHoldingAttentionItem(nearbyPlayer);
    return hasBarkTrigger(
            playerHoldingLure,
            this.dog.getHealth(),
            this.dog.getMaxHealth(),
            this.dog.isLeashed(),
            this.dog.getTarget() != null)
        || this.dog.getRandom().nextInt(RANDOM_BARK_CHANCE) == 0;
  }

  private boolean canHowl() {
    return isHowlConditionMet(
            this.dog.isDead(),
            this.dog.getSleepController().isAsleep(),
            this.dog.getWorld().isDay(),
            this.dog.getWorld().getMoonPhase())
        && this.howlCooldownTicks <= 0;
  }

  private void tickHowl() {
    if (!this.dog.getVoiceBreed().howls()) {
      return;
    }

    if (this.howlCooldownTicks > 0) {
      this.howlCooldownTicks--;
    }

    if (this.howlActiveTicks > 0) {
      this.howlActiveTicks--;
      if (this.howlActiveTicks == 0) {
        this.dog.getAnimationState().setHowling(false);
      }
    }

    if (this.canHowl() && this.dog.getRandom().nextInt(RANDOM_HOWL_CHANCE) == 0) {
      this.howl();
      this.triggerHowlAdvancement();
    }
  }

  void callOut() {
    if (this.dog.getVoiceBreed().howls() && !this.dog.isUndead()) {
      this.howl(DeathWatchRules.callOutVolume(DogsUnleashed.SERVER_CONFIG.sound().howlVolume()));
    } else if (this.barkSound() != null) {
      this.playBark(
          this.getBarkPitch(),
          DeathWatchRules.callOutVolume(DogsUnleashed.SERVER_CONFIG.sound().barkVolume()));
    }
  }

  private void howl() {
    this.howl(DogsUnleashed.SERVER_CONFIG.sound().howlVolume());
  }

  private void howl(final float volume) {
    this.dog.getAnimationState().setHowling(true);
    this.dog.playSound(this.howlSound(), volume, HOWL_PITCH);
    this.howlCooldownTicks = HOWL_COOLDOWN_TICKS;
    this.howlActiveTicks = HOWL_DURATION_TICKS;
  }

  private void triggerHowlAdvancement() {
    if (!(this.dog.getWorld() instanceof ServerWorld)) {
      return;
    }

    final Entity owner = this.dog.getOwner();
    if (!(owner instanceof ServerPlayerEntity player) || !player.isAlive()) {
      return;
    }

    if (this.dog.squaredDistanceTo(player) > HOWL_HEARING_RANGE_SQUARED) {
      return;
    }

    DogHowledCriterion.INSTANCE.trigger(player);
  }
}
