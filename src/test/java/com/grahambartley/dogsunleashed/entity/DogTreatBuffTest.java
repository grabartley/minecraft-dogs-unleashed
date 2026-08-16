package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.MINECRAFT_TICK_RATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogTreatBuffTest {

  @Test
  @DisplayName("buff lasts the advertised sixty seconds in ticks")
  void durationTicksMatchesAdvertisedSeconds() {
    assertEquals(60, DogTreatBuff.DURATION_SECONDS, "The treat buff is documented as 60 seconds");
    assertEquals(
        DogTreatBuff.DURATION_SECONDS * MINECRAFT_TICK_RATE,
        DogTreatBuff.DURATION_TICKS,
        "Duration in ticks must be the advertised seconds at the Minecraft tick rate");
  }

  @Test
  @DisplayName("movement speed modifier adds ten percent of the dog's base speed")
  void movementSpeedModifierAddsTenPercentOfBase() {
    final EntityAttributeModifier modifier = DogTreatBuff.movementSpeedModifier();

    assertEquals(0.10, modifier.value(), "The treat is specified as a +10% movement speed buff");
    assertEquals(
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE,
        modifier.operation(),
        "A percentage speed buff must scale off the dog's base speed, not add a flat amount");
    assertEquals(
        DogTreatBuff.MOVEMENT_SPEED_MODIFIER_ID,
        modifier.id(),
        "The modifier id is what removal keys off, so it must be stable");
  }

  @Test
  @DisplayName("attack damage modifier adds a flat one point of damage")
  void attackDamageModifierAddsFlatOne() {
    final EntityAttributeModifier modifier = DogTreatBuff.attackDamageModifier();

    assertEquals(1.0, modifier.value(), "The treat is specified as a +1 attack damage buff");
    assertEquals(
        EntityAttributeModifier.Operation.ADD_VALUE,
        modifier.operation(),
        "A flat damage bonus must add a value rather than scale the base");
    assertEquals(
        DogTreatBuff.ATTACK_DAMAGE_MODIFIER_ID,
        modifier.id(),
        "The modifier id is what removal keys off, so it must be stable");
  }

  @Test
  @DisplayName("modifier ids are distinct and namespaced to the mod")
  void modifierIdsAreDistinctAndNamespaced() {
    assertNotEquals(
        DogTreatBuff.MOVEMENT_SPEED_MODIFIER_ID,
        DogTreatBuff.ATTACK_DAMAGE_MODIFIER_ID,
        "Sharing one id across two attributes would make removal ambiguous");
    assertEquals(
        DogsUnleashed.MOD_ID,
        DogTreatBuff.MOVEMENT_SPEED_MODIFIER_ID.getNamespace(),
        "Modifier ids must be namespaced to this mod to avoid colliding with other mods");
    assertEquals(
        DogsUnleashed.MOD_ID,
        DogTreatBuff.ATTACK_DAMAGE_MODIFIER_ID.getNamespace(),
        "Modifier ids must be namespaced to this mod to avoid colliding with other mods");
  }

  @Test
  @DisplayName("repeated modifier construction yields equal modifiers so a re-feed cannot stack")
  void repeatedConstructionYieldsEqualModifiers() {
    assertEquals(
        DogTreatBuff.movementSpeedModifier(),
        DogTreatBuff.movementSpeedModifier(),
        "Two speed modifiers built from the same constants must be equal so removal by id replaces"
            + " rather than stacks");
    assertEquals(
        DogTreatBuff.attackDamageModifier(),
        DogTreatBuff.attackDamageModifier(),
        "Two damage modifiers built from the same constants must be equal so removal by id replaces"
            + " rather than stacks");
  }
}
