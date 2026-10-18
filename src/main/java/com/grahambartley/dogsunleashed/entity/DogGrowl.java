package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public final class DogGrowl {

  public static final int REPEAT_TICKS = 3 * 20;

  private final UnleashedDogEntity dog;
  private int cooldownTicks;

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
    final LivingEntity target = this.dog.getTarget();
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
      this.dog.playSound(
          sound,
          DogsUnleashed.SERVER_CONFIG.barkVolume(),
          DogVocalization.barkPitch(this.dog.isBaby()));
      this.cooldownTicks = REPEAT_TICKS;
    }
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
