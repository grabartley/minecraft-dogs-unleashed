package com.grahambartley.dogsunleashed.compat;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.Attributes;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.SpawnSettings;
import java.util.List;
import java.util.Locale;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;

/**
 * Builds the per-breed description lines shown on a spawn egg's recipe-viewer info page.
 *
 * <p>Every value is read back off {@link UnleashedDogBreed} so retuning a breed's biomes or
 * attributes updates the JEI and EMI pages without a matching lang edit.
 */
public final class DogBreedInfoText {

  static final String BIOMES_KEY = "info.dogs-unleashed.spawn_egg.biomes";
  static final String STATS_KEY = "info.dogs-unleashed.spawn_egg.stats";

  private static final String BIOME_SEPARATOR = ", ";

  private DogBreedInfoText() {}

  public static List<Text> lines(final UnleashedDogBreed breed) {
    final SpawnSettings spawnSettings = breed.spawnSettings();
    if (spawnSettings == null) {
      return List.of(statsLine(breed));
    }
    return List.of(biomesLine(spawnSettings), statsLine(breed));
  }

  static Text biomesLine(final SpawnSettings spawnSettings) {
    final RegistryKey<Biome>[] biomes = spawnSettings.biomes();
    final MutableText joined = Text.empty();
    for (int i = 0; i < biomes.length; i++) {
      if (i > 0) {
        joined.append(BIOME_SEPARATOR);
      }
      joined.append(Text.translatable(biomeTranslationKey(biomes[i])));
    }
    return Text.translatable(BIOMES_KEY, joined);
  }

  static Text statsLine(final UnleashedDogBreed breed) {
    final Attributes attributes = breed.attributes();
    return Text.translatable(
        STATS_KEY,
        formatHealth(attributes.maxHealth()),
        formatAttackDamage(attributes.attackDamage()),
        formatMovementSpeed(attributes.movementSpeed()));
  }

  static String biomeTranslationKey(final RegistryKey<Biome> biome) {
    return Util.createTranslationKey("biome", biome.getValue());
  }

  static String formatHealth(final double maxHealth) {
    return String.format(Locale.ROOT, "%.0f", maxHealth);
  }

  static String formatAttackDamage(final double attackDamage) {
    return String.format(Locale.ROOT, "%.1f", attackDamage);
  }

  static String formatMovementSpeed(final double movementSpeed) {
    return String.format(Locale.ROOT, "%.2f", movementSpeed);
  }
}
