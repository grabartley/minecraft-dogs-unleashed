package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public final class DogGrowl {

  public static final int REPEAT_TICKS = 3 * 20;

  private final UnleashedDogEntity dog;
  private int cooldownTicks;
  private long lastWarnTime = Long.MIN_VALUE / 2;
  private @Nullable LivingEntity standOff;

  DogGrowl(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static boolean growlsAgain(final int cooldownTicks, final boolean inMeleeRange) {
    return cooldownTicks <= 0 && !inMeleeRange;
  }

  public boolean isGrowling() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.GROWLING);
  }

  void tick() {
    final SoundEvent sound = this.sound();
    final LivingEntity target = this.dog.getTarget() != null ? this.dog.getTarget() : this.standOff;
    if (sound == null || target == null || !target.isAlive()) {
      this.setGrowling(false);
      this.cooldownTicks = 0;
      return;
    }
    if (this.cooldownTicks > 0) {
      this.cooldownTicks--;
    }
    final boolean startsGrowling = !this.isGrowling();
    this.setGrowling(true);
    if (startsGrowling || growlsAgain(this.cooldownTicks, this.dog.isInAttackRange(target))) {
      this.play(sound);
      this.cooldownTicks = REPEAT_TICKS;
    }
  }

  public void standOff(final @Nullable LivingEntity threat) {
    this.standOff = threat;
  }

  public void warn() {
    final SoundEvent sound = this.sound();
    final long now = this.dog.getWorld().getTime();
    if (sound == null || now - this.lastWarnTime < REPEAT_TICKS) {
      return;
    }
    this.lastWarnTime = now;
    this.play(sound);
  }

  private void play(final SoundEvent sound) {
    this.dog.playSound(
        sound,
        DogsUnleashed.SERVER_CONFIG.sound().barkVolume(),
        DogVocalization.barkPitch(this.dog.isBaby()));
  }

  private @Nullable SoundEvent sound() {
    return this.dog.isUndead() ? null : this.dog.getVoiceBreed().growlSound();
  }

  private void setGrowling(final boolean growling) {
    if (this.isGrowling() != growling) {
      this.dog.getDataTracker().set(UnleashedDogEntity.GROWLING, growling);
    }
  }
}
