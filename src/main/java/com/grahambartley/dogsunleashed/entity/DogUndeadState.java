package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.jetbrains.annotations.Nullable;

public final class DogUndeadState {

  public static final double MAX_HEALTH_MULTIPLIER = 0.5;
  public static final double ATTACK_DAMAGE_MULTIPLIER = 0.5;

  public static final float DAYLIGHT_BURN_SECONDS = 8.0f;

  private final UnleashedDogEntity dog;

  DogUndeadState(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static double undeadMaxHealth(final double livingMaxHealth) {
    return livingMaxHealth * MAX_HEALTH_MULTIPLIER;
  }

  public static double undeadAttackDamage(final double livingAttackDamage) {
    return livingAttackDamage * ATTACK_DAMAGE_MULTIPLIER;
  }

  public static double livingMaxHealth(
      final UnleashedDogBreed breed, final @Nullable DogGenome genome) {
    return genome != null ? genome.maxHealth() : breed.attributes().maxHealth();
  }

  public static double livingAttackDamage(
      final UnleashedDogBreed breed, final @Nullable DogGenome genome) {
    return genome != null ? genome.attackDamage() : breed.attributes().attackDamage();
  }

  public void applyAttributeScaling() {
    if (!this.dog.isUndead()) {
      return;
    }
    this.setBaseValues(
        undeadMaxHealth(livingMaxHealth(this.dog.getBreed(), this.dog.getGenome())),
        undeadAttackDamage(livingAttackDamage(this.dog.getBreed(), this.dog.getGenome())));
    if (this.dog.getHealth() > this.dog.getMaxHealth()) {
      this.dog.setHealth(this.dog.getMaxHealth());
    }
  }

  public static void restoreLivingAttributes(final UnleashedDogEntity curedDog) {
    curedDog
        .getUndeadState()
        .setBaseValues(
            livingMaxHealth(curedDog.getBreed(), curedDog.getGenome()),
            livingAttackDamage(curedDog.getBreed(), curedDog.getGenome()));
  }

  void tick() {
    if (!this.dog.isUndead()) {
      return;
    }
    if (this.isShelteredFromDaylight()) {
      return;
    }
    if (this.dog.isExposedToDaylight()) {
      this.dog.setOnFireFor(DAYLIGHT_BURN_SECONDS);
    }
  }

  private boolean isShelteredFromDaylight() {
    return !this.dog.getEquipmentHolder().getStack(DogEquipmentSlot.ARMOUR).isEmpty();
  }

  private void setBaseValues(final double maxHealth, final double attackDamage) {
    setBaseValue(this.dog.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH), maxHealth);
    setBaseValue(
        this.dog.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE), attackDamage);
  }

  private static void setBaseValue(
      final @Nullable EntityAttributeInstance instance, final double value) {
    if (instance != null) {
      instance.setBaseValue(value);
    }
  }
}
