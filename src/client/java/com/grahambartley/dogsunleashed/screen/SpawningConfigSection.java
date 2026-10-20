package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.SpawningSettings;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.text.Text;

final class SpawningConfigSection {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private SpawningSettings settings;

  SpawningConfigSection(final SpawningSettings settings) {
    this.settings = settings;
  }

  void addNaturalSpawningToggle(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(
        rows.toggle(
            KEY + "spawn",
            KEY + "spawn.tooltip",
            this.settings.enableNaturalSpawning(),
            value -> this.settings = this.settings.withEnableNaturalSpawning(value)));
  }

  void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "spawning"));
    list.addRow(
        rows.percentSlider(
            KEY + "spawnrate",
            SpawningSettings.RATE_MIN,
            SpawningSettings.RATE_MAX,
            this.settings.spawnRateMultiplierPercent(),
            value -> this.settings = this.settings.withSpawnRateMultiplierPercent(value)));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed.isNaturallySpawning()) {
        list.addRow(
            rows.percentSlider(
                Text.translatable(
                    KEY + "spawnrate.breed", Text.translatable(breed.translationKey())),
                Text.translatable(KEY + "spawnrate.breed.tooltip"),
                SpawningSettings.RATE_MIN,
                SpawningSettings.RATE_MAX,
                this.settings.breedSpawnRateMultipliersPercent().get(breed.serializedId()),
                value ->
                    this.settings =
                        this.settings.withBreedSpawnRateMultiplierPercent(
                            breed.serializedId(), value)));
      }
    }
    list.addRow(
        rows.toggle(
            KEY + "capindependentspawning",
            KEY + "capindependentspawning.tooltip",
            this.settings.capIndependentSpawningEnabled(),
            value -> this.settings = this.settings.withCapIndependentSpawningEnabled(value)));
    list.addRow(
        rows.toggle(
            KEY + "villagestrays",
            KEY + "villagestrays.tooltip",
            this.settings.villageStrays(),
            value -> this.settings = this.settings.withVillageStrays(value)));
    list.addRow(
        rows.toggle(
            KEY + "traderpuppies",
            KEY + "traderpuppies.tooltip",
            this.settings.traderPuppies(),
            value -> this.settings = this.settings.withTraderPuppies(value)));
  }

  SpawningSettings settings() {
    return this.settings;
  }
}
