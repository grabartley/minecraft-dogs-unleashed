package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledTeamStamina;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public final class DogSledHarness extends DogComponent {

  private final UnleashedDogEntity dog;
  private @Nullable SledEntity sled;
  private DireWolfStamina.State stamina = new DireWolfStamina.State(0.0f, false);
  private boolean staminaKnown;

  DogSledHarness(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public void hitchTo(final SledEntity sled) {
    this.sled = sled;
  }

  public @Nullable SledEntity sled() {
    return this.sled != null && !this.sled.isRemoved() && this.sled.getTeam().has(this.dog)
        ? this.sled
        : null;
  }

  public boolean isHitched() {
    return this.sled() != null;
  }

  public float capacity() {
    return SledTeamStamina.capacity(this.dog.getGrowth().stage(), settings());
  }

  public DireWolfStamina.State stamina() {
    if (!this.staminaKnown) {
      this.staminaKnown = true;
      this.stamina = new DireWolfStamina.State(this.capacity(), false);
    }
    return this.stamina;
  }

  public void storeStamina(final DireWolfStamina.State state) {
    this.staminaKnown = true;
    this.stamina = state;
  }

  void onRemoved(final Entity.RemovalReason reason) {
    final SledEntity hitched = this.sled();
    if (hitched != null
        && reason != Entity.RemovalReason.UNLOADED_TO_CHUNK
        && reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
      hitched.getTeam().dropLost(this.dog);
    }
  }

  @Override
  void tick() {
    if (this.staminaKnown && !this.isHitched()) {
      this.stamina = DireWolfStamina.next(this.stamina, this.capacity(), false, false, settings());
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.staminaKnown) {
      nbt.putFloat(ModNbtKeys.SLED_STAMINA, this.stamina.stamina());
    }
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.SLED_STAMINA, NbtElement.NUMBER_TYPE)) {
      final float saved = nbt.getFloat(ModNbtKeys.SLED_STAMINA);
      this.storeStamina(new DireWolfStamina.State(saved, saved <= 0.0f));
    }
  }

  private static DireWolfStaminaSettings settings() {
    return DogsUnleashed.SERVER_CONFIG.direWolf().stamina();
  }
}
