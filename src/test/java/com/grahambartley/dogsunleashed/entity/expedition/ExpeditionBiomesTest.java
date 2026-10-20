package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ExpeditionBiomesTest {

  @SuppressWarnings("unchecked")
  private static RegistryEntry<Biome> biomeTagged(final List<TagKey<Biome>> tags) {
    final RegistryEntry<Biome> biome = mock(RegistryEntry.class);
    when(biome.isIn(any(TagKey.class))).thenAnswer(call -> tags.contains(call.getArgument(0)));
    return biome;
  }

  static Stream<Arguments> biomes() {
    return Stream.of(
        Arguments.of(List.of(), ExpeditionBiomeGroup.PLAINS),
        Arguments.of(List.of(ConventionalBiomeTags.IS_NETHER), ExpeditionBiomeGroup.NETHER),
        Arguments.of(List.of(ConventionalBiomeTags.IS_END), ExpeditionBiomeGroup.END),
        Arguments.of(List.of(ConventionalBiomeTags.IS_RIVER), ExpeditionBiomeGroup.COAST),
        Arguments.of(List.of(ConventionalBiomeTags.IS_BEACH), ExpeditionBiomeGroup.COAST),
        Arguments.of(List.of(ConventionalBiomeTags.IS_ICY), ExpeditionBiomeGroup.SNOWY),
        Arguments.of(List.of(ConventionalBiomeTags.IS_SAVANNA), ExpeditionBiomeGroup.ARID),
        Arguments.of(List.of(ConventionalBiomeTags.IS_WINDSWEPT), ExpeditionBiomeGroup.MOUNTAIN),
        Arguments.of(List.of(ConventionalBiomeTags.IS_FOREST), ExpeditionBiomeGroup.FOREST),
        Arguments.of(List.of(ConventionalBiomeTags.IS_MUSHROOM), ExpeditionBiomeGroup.MUSHROOM),
        Arguments.of(List.of(ConventionalBiomeTags.IS_OCEAN), ExpeditionBiomeGroup.COAST),
        Arguments.of(List.of(ConventionalBiomeTags.IS_STONY_SHORES), ExpeditionBiomeGroup.COAST),
        Arguments.of(List.of(ConventionalBiomeTags.IS_SNOWY), ExpeditionBiomeGroup.SNOWY),
        Arguments.of(List.of(ConventionalBiomeTags.IS_DESERT), ExpeditionBiomeGroup.ARID),
        Arguments.of(List.of(ConventionalBiomeTags.IS_BADLANDS), ExpeditionBiomeGroup.ARID),
        Arguments.of(List.of(ConventionalBiomeTags.IS_JUNGLE), ExpeditionBiomeGroup.JUNGLE),
        Arguments.of(List.of(ConventionalBiomeTags.IS_SWAMP), ExpeditionBiomeGroup.SWAMP),
        Arguments.of(List.of(ConventionalBiomeTags.IS_MOUNTAIN), ExpeditionBiomeGroup.MOUNTAIN),
        Arguments.of(List.of(ConventionalBiomeTags.IS_TAIGA), ExpeditionBiomeGroup.TAIGA),
        Arguments.of(
            List.of(ConventionalBiomeTags.IS_SWAMP, ConventionalBiomeTags.IS_JUNGLE),
            ExpeditionBiomeGroup.JUNGLE),
        Arguments.of(
            List.of(ConventionalBiomeTags.IS_FOREST, ConventionalBiomeTags.IS_SNOWY),
            ExpeditionBiomeGroup.SNOWY),
        Arguments.of(
            List.of(ConventionalBiomeTags.IS_TAIGA, ConventionalBiomeTags.IS_FOREST),
            ExpeditionBiomeGroup.TAIGA),
        Arguments.of(
            List.of(ConventionalBiomeTags.IS_MOUNTAIN, ConventionalBiomeTags.IS_SNOWY),
            ExpeditionBiomeGroup.SNOWY));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("biomes")
  @DisplayName("a biome falls in the first group whose tag it has, or plains")
  void groupsByFirstMatchingTag(
      final List<TagKey<Biome>> tags, final ExpeditionBiomeGroup expected) {
    assertEquals(expected, ExpeditionBiomes.groupOf(biomeTagged(tags)));
  }
}
