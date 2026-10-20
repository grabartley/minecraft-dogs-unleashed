package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.GameplaySettings;

final class GameplayConfigSection {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private final SpawningConfigSection spawning;
  private GameplaySettings settings;

  GameplayConfigSection(final GameplaySettings settings, final SpawningConfigSection spawning) {
    this.settings = settings;
    this.spawning = spawning;
  }

  void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "gameplay"));
    this.spawning.addNaturalSpawningToggle(list, rows);
    list.addRow(
        rows.toggle(
            KEY + "graves",
            KEY + "graves.tooltip",
            this.settings.gravesEnabled(),
            value -> this.settings = this.settings.withGravesEnabled(value)));
    list.addRow(
        rows.toggle(
            KEY + "dropleashonplay",
            KEY + "dropleashonplay.tooltip",
            this.settings.dropLeashOnPlayMode(),
            value -> this.settings = this.settings.withDropLeashOnPlayMode(value)));
    list.addRow(
        rows.toggle(
            KEY + "shownames",
            KEY + "shownames.tooltip",
            this.settings.showDogNames(),
            value -> this.settings = this.settings.withShowDogNames(value)));
    list.addRow(
        rows.toggle(
            KEY + "muddydogs",
            KEY + "muddydogs.tooltip",
            this.settings.muddyDogs(),
            value -> this.settings = this.settings.withMuddyDogs(value)));
  }

  GameplaySettings settings() {
    return this.settings;
  }
}
