package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogFurTest {

  @ParameterizedTest(name = "{0} rolls={1}")
  @CsvSource({
    "NATURAL, true",
    "CHUNK_GENERATION, true",
    "BREEDING, false",
    "SPAWN_EGG, false",
    "COMMAND, false",
    "SPAWNER, false",
    "CONVERSION, false"
  })
  @DisplayName("only dogs found in the wild roll a starting coat")
  void onlyWildDogsRoll(final SpawnReason reason, final boolean rolls) {
    assertEquals(rolls, DogFur.rollsStageOnSpawn(reason));
  }
}
