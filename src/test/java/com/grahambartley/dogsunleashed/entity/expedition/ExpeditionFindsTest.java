package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.registry.RegistryKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ExpeditionFindsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ExpeditionBiomeGroup.class)
  @DisplayName("each biome group rolls its own mod loot table")
  void lootTableIsTheGroupsOwn(final ExpeditionBiomeGroup group) {
    final var key = ExpeditionFinds.lootTable(group);
    assertEquals(RegistryKeys.LOOT_TABLE, key.getRegistryRef());
    assertEquals("dogs-unleashed:" + group.lootTablePath(), key.getValue().toString());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ExpeditionBiomeGroup.class)
  @DisplayName("each biome group's loot table ships in the mod's data")
  void lootTableFileExists(final ExpeditionBiomeGroup group) {
    assertNotNull(
        ExpeditionFindsTest.class.getResource(
            "/data/dogs-unleashed/loot_table/" + group.lootTablePath() + ".json"),
        group + " has no loot table file");
  }
}
