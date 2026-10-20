package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public final class DogAging extends DogComponent {

  public static final Identifier MOVEMENT_SPEED_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "senior_movement_speed");

  private final UnleashedDogEntity dog;
  private long adultAgeTicks;
  private long lastSeenTime = DogAgingRules.NEVER_SEEN;
  private boolean seniorParent;

  DogAging(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isSenior() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.SENIOR);
  }

  public long adultAgeTicks() {
    return this.adultAgeTicks;
  }

  public void setAdultAgeTicks(final long adultAgeTicks) {
    this.adultAgeTicks = Math.max(0L, adultAgeTicks);
  }

  public boolean hasSeniorParent() {
    return this.seniorParent;
  }

  void inheritLegacy(final DogAging parentA, final DogAging parentB) {
    this.seniorParent = DogAgingRules.inheritsLegacy(parentA.isSenior(), parentB.isSenior());
  }

  @Override
  void tick() {
    final long now = this.dog.getWorld().getTime();
    final long elapsed = DogAgingRules.elapsedSince(this.lastSeenTime, now);
    this.lastSeenTime = now;
    final AgingSettings settings = DogsUnleashed.SERVER_CONFIG.aging();
    if (DogAgingRules.ages(
        settings.dogAgingEnabled(),
        this.dog.isBaby(),
        this.dog.isUndead(),
        this.dog.getGrowth().stage(),
        this.dog.getGrowth().applies())) {
      this.adultAgeTicks += elapsed;
    }
    this.showSenior(
        DogAgingRules.isSenior(
            settings.dogAgingEnabled(), this.adultAgeTicks, settings.seniorAfterDays()));
  }

  private void showSenior(final boolean senior) {
    this.dog.getDataTracker().set(UnleashedDogEntity.SENIOR, senior);
    final EntityAttributeInstance speed =
        this.dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    if (speed == null || speed.hasModifier(MOVEMENT_SPEED_MODIFIER_ID) == senior) {
      return;
    }
    if (senior) {
      speed.addTemporaryModifier(
          new EntityAttributeModifier(
              MOVEMENT_SPEED_MODIFIER_ID,
              DogAgingRules.SENIOR_SPEED_FRACTION,
              EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    } else {
      speed.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, this.adultAgeTicks);
    if (this.lastSeenTime != DogAgingRules.NEVER_SEEN) {
      nbt.putLong(ModNbtKeys.AGE_LAST_SEEN_TIME, this.lastSeenTime);
    }
    nbt.putBoolean(ModNbtKeys.SENIOR_PARENT, this.seniorParent);
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.setAdultAgeTicks(nbt.getLong(ModNbtKeys.ADULT_AGE_TICKS));
    this.lastSeenTime = lastSeenFromSave(nbt);
    this.seniorParent = nbt.getBoolean(ModNbtKeys.SENIOR_PARENT);
  }

  static long lastSeenFromSave(final NbtCompound nbt) {
    return nbt.contains(ModNbtKeys.AGE_LAST_SEEN_TIME, NbtElement.NUMBER_TYPE)
        ? nbt.getLong(ModNbtKeys.AGE_LAST_SEEN_TIME)
        : DogAgingRules.NEVER_SEEN;
  }
}
