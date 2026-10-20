package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpeditionBiomeGroupTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "PLAINS, gameplay/expedition/plains",
    "FOREST, gameplay/expedition/forest",
    "TAIGA, gameplay/expedition/taiga",
    "SNOWY, gameplay/expedition/snowy",
    "ARID, gameplay/expedition/arid",
    "JUNGLE, gameplay/expedition/jungle",
    "SWAMP, gameplay/expedition/swamp",
    "MOUNTAIN, gameplay/expedition/mountain",
    "COAST, gameplay/expedition/coast",
    "MUSHROOM, gameplay/expedition/mushroom",
    "NETHER, gameplay/expedition/nether",
    "END, gameplay/expedition/end"
  })
  @DisplayName("each biome group reads its own loot table in the expedition folder")
  void mapsToLootTable(final ExpeditionBiomeGroup group, final String expected) {
    assertEquals(expected, group.lootTablePath());
  }
}
