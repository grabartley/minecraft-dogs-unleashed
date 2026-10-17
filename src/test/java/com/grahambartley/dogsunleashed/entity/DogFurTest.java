package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogFurTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(SpawnReason.class)
  @DisplayName("only natural spawns and world generation roll a starting coat")
  void onlyWildSpawnsRoll(final SpawnReason reason) {
    assertEquals(
        reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION,
        DogFur.rollsStageOnSpawn(reason));
  }

  @Test
  @DisplayName("an overgrown coat slows the dog by ten percent of its total speed")
  void overgrownModifier() {
    final EntityAttributeModifier modifier = DogFur.overgrownSpeedModifier();

    assertEquals(DogFur.OVERGROWN_SPEED_MODIFIER_ID, modifier.id());
    assertEquals(-0.10, modifier.value(), 1.0e-9);
    assertEquals(EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, modifier.operation());
  }
}
