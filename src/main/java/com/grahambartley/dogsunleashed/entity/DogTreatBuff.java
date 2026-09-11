package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.MINECRAFT_TICK_RATE;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogTreatBuff {

  public static final int DURATION_SECONDS = 60;
  public static final int DURATION_TICKS = DURATION_SECONDS * MINECRAFT_TICK_RATE;

  public static final double MOVEMENT_SPEED_BONUS_FRACTION = 0.10;
  public static final double ATTACK_DAMAGE_BONUS = 1.0;

  public static final Identifier MOVEMENT_SPEED_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "dog_treat_movement_speed");
  public static final Identifier ATTACK_DAMAGE_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "dog_treat_attack_damage");

  private DogTreatBuff() {}

  public static EntityAttributeModifier movementSpeedModifier() {
    return new EntityAttributeModifier(
        MOVEMENT_SPEED_MODIFIER_ID,
        MOVEMENT_SPEED_BONUS_FRACTION,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
  }

  public static EntityAttributeModifier attackDamageModifier() {
    return new EntityAttributeModifier(
        ATTACK_DAMAGE_MODIFIER_ID,
        ATTACK_DAMAGE_BONUS,
        EntityAttributeModifier.Operation.ADD_VALUE);
  }

  public static void apply(final LivingEntity dog) {
    replace(
        dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED), movementSpeedModifier());
    replace(
        dog.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE), attackDamageModifier());
  }

  public static void clear(final LivingEntity dog) {
    remove(
        dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED),
        MOVEMENT_SPEED_MODIFIER_ID);
    remove(
        dog.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        ATTACK_DAMAGE_MODIFIER_ID);
  }

  public static boolean isApplied(final LivingEntity dog) {
    final EntityAttributeInstance speed =
        dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    return speed != null && speed.hasModifier(MOVEMENT_SPEED_MODIFIER_ID);
  }

  private static void replace(
      final @Nullable EntityAttributeInstance instance, final EntityAttributeModifier modifier) {
    if (instance == null) {
      return;
    }
    instance.removeModifier(modifier.id());
    instance.addTemporaryModifier(modifier);
  }

  private static void remove(
      final @Nullable EntityAttributeInstance instance, final Identifier modifierId) {
    if (instance != null) {
      instance.removeModifier(modifierId);
    }
  }
}
