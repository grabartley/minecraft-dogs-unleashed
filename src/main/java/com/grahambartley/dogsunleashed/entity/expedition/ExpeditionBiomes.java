package com.grahambartley.dogsunleashed.entity.expedition;

import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;

public final class ExpeditionBiomes {

  private static final List<Map.Entry<TagKey<Biome>, ExpeditionBiomeGroup>> GROUP_TAGS =
      List.of(
          Map.entry(ConventionalBiomeTags.IS_NETHER, ExpeditionBiomeGroup.NETHER),
          Map.entry(ConventionalBiomeTags.IS_END, ExpeditionBiomeGroup.END),
          Map.entry(ConventionalBiomeTags.IS_MUSHROOM, ExpeditionBiomeGroup.MUSHROOM),
          Map.entry(ConventionalBiomeTags.IS_OCEAN, ExpeditionBiomeGroup.COAST),
          Map.entry(ConventionalBiomeTags.IS_BEACH, ExpeditionBiomeGroup.COAST),
          Map.entry(ConventionalBiomeTags.IS_STONY_SHORES, ExpeditionBiomeGroup.COAST),
          Map.entry(ConventionalBiomeTags.IS_RIVER, ExpeditionBiomeGroup.COAST),
          Map.entry(ConventionalBiomeTags.IS_SNOWY, ExpeditionBiomeGroup.SNOWY),
          Map.entry(ConventionalBiomeTags.IS_ICY, ExpeditionBiomeGroup.SNOWY),
          Map.entry(ConventionalBiomeTags.IS_DESERT, ExpeditionBiomeGroup.ARID),
          Map.entry(ConventionalBiomeTags.IS_BADLANDS, ExpeditionBiomeGroup.ARID),
          Map.entry(ConventionalBiomeTags.IS_SAVANNA, ExpeditionBiomeGroup.ARID),
          Map.entry(ConventionalBiomeTags.IS_JUNGLE, ExpeditionBiomeGroup.JUNGLE),
          Map.entry(ConventionalBiomeTags.IS_SWAMP, ExpeditionBiomeGroup.SWAMP),
          Map.entry(ConventionalBiomeTags.IS_MOUNTAIN, ExpeditionBiomeGroup.MOUNTAIN),
          Map.entry(ConventionalBiomeTags.IS_WINDSWEPT, ExpeditionBiomeGroup.MOUNTAIN),
          Map.entry(ConventionalBiomeTags.IS_TAIGA, ExpeditionBiomeGroup.TAIGA),
          Map.entry(ConventionalBiomeTags.IS_FOREST, ExpeditionBiomeGroup.FOREST));

  private ExpeditionBiomes() {}

  public static RegistryEntry<Biome> biomeAt(final ServerWorld world, final int x, final int z) {
    return world
        .getChunkManager()
        .getChunkGenerator()
        .getBiomeSource()
        .getBiome(
            BiomeCoords.fromBlock(x),
            BiomeCoords.fromBlock(world.getSeaLevel()),
            BiomeCoords.fromBlock(z),
            world.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
  }

  public static ExpeditionBiomeGroup groupOf(final RegistryEntry<Biome> biome) {
    return GROUP_TAGS.stream()
        .filter(entry -> biome.isIn(entry.getKey()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(ExpeditionBiomeGroup.PLAINS);
  }
}
