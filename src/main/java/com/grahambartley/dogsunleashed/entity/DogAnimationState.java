package com.grahambartley.dogsunleashed.entity;

public final class DogAnimationState {

  private final UnleashedDogEntity dog;
  private double boneResetTicks = 1;

  DogAnimationState(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isHowling() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.HOWLING);
  }

  public void setHowling(final boolean howling) {
    this.dog.getDataTracker().set(UnleashedDogEntity.HOWLING, howling);
  }

  public int getShakeProgress() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.SHAKE_PROGRESS);
  }

  public boolean isShaking() {
    return this.getShakeProgress() > 0;
  }

  void setShakeProgress(final int progress) {
    this.dog.getDataTracker().set(UnleashedDogEntity.SHAKE_PROGRESS, progress);
  }

  public boolean isHeadTilting() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.HEAD_TILTING);
  }

  void setHeadTilting(final boolean tilting) {
    this.dog.getDataTracker().set(UnleashedDogEntity.HEAD_TILTING, tilting);
  }

  public int getTailWagTimerTicks() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.TAIL_WAG_TIMER);
  }

  void setTailWagTimer(final int ticks) {
    this.dog.getDataTracker().set(UnleashedDogEntity.TAIL_WAG_TIMER, ticks);
  }

  public double boneResetTicks() {
    return this.boneResetTicks;
  }

  void setBoneResetTicks(final double ticks) {
    this.boneResetTicks = ticks;
  }
}
