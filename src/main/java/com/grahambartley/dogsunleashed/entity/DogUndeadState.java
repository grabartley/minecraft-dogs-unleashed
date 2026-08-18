package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.jetbrains.annotations.Nullable;

/**
 * What being undead costs a dog: weaker stats than it had in life, and the vanilla zombie's
 * intolerance of daylight.
 *
 * <p>The undead multipliers compose over whatever the living baseline was, which for a cross-breed
 * is its genome rather than its preset. The genome itself is never rewritten, so curing restores
 * the original numbers exactly.
 */
public final class DogUndeadState {

  public static final double MAX_HEALTH_MULTIPLIER = 0.5;
  public static final double ATTACK_DAMAGE_MULTIPLIER = 0.5;

  /** Matches {@code ZombieEntity}, which burns for eight seconds per exposed tick. */
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

  /**
   * Scales this dog's stats down to their undead values, or leaves a living dog alone. Idempotent,
   * so every spawn and every load can call it without compounding.
   */
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

  /**
   * Puts a freshly cured dog back on its living numbers. A cured dog is recreated from the undead
   * dog's NBT, and vanilla persists attribute base values, so the undead scaling would otherwise
   * survive the cure.
   */
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

  /** Dog armour stands in for the helmet that spares a vanilla zombie. */
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
