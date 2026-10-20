package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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

  static Stream<Arguments> plainFinds() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(List.of(new ItemStack(Items.BONE))),
        Arguments.of(List.of(new ItemStack(Items.STICK), ItemStack.EMPTY)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("plainFinds")
  @DisplayName("finds with nothing from the rare tag are not rare")
  void plainFindsAreNotRare(final List<ItemStack> finds) {
    assertFalse(ExpeditionFinds.isRare(finds));
  }
}
