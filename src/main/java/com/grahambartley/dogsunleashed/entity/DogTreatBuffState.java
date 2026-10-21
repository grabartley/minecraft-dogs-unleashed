package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public final class DogTreatBuffState extends DogComponent {

  private final UnleashedDogEntity dog;

  DogTreatBuffState(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public int getRemainingTicks() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.TREAT_BUFF_TICKS);
  }

  public boolean isActive() {
    return this.getRemainingTicks() > 0;
  }

  public void apply(final float barkPitchBoost) {
    this.dog.getDataTracker().set(UnleashedDogEntity.TREAT_BUFF_TICKS, DogTreatBuff.DURATION_TICKS);
    DogTreatBuff.apply(this.dog);
    this.dog.getAmbienceEffects().celebrate(barkPitchBoost);
  }

  @Override
  void tick() {
    final int remaining = this.getRemainingTicks();
    if (remaining <= 0) {
      return;
    }
    if (remaining == 1) {
      DogTreatBuff.clear(this.dog);
    }
    this.dog.getDataTracker().set(UnleashedDogEntity.TREAT_BUFF_TICKS, remaining - 1);
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putInt(ModNbtKeys.TREAT_BUFF_TICKS, this.getRemainingTicks());
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    if (!nbt.contains(ModNbtKeys.TREAT_BUFF_TICKS, NbtElement.NUMBER_TYPE)) {
      return;
    }
    final int remainingTicks = sanitizedTicks(nbt.getInt(ModNbtKeys.TREAT_BUFF_TICKS));
    this.dog.getDataTracker().set(UnleashedDogEntity.TREAT_BUFF_TICKS, remainingTicks);
    if (remainingTicks > 0) {
      DogTreatBuff.apply(this.dog);
    }
  }

  static int sanitizedTicks(final int savedTicks) {
    return Math.max(0, savedTicks);
  }
}
