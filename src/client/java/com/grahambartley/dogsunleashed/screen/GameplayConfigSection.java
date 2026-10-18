package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.GameplaySettings;

final class GameplayConfigSection implements ConfigSection<GameplaySettings> {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private GameplaySettings settings;

  GameplayConfigSection(final GameplaySettings settings) {
    this.settings = settings;
  }

  @Override
  public void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
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
  }

  @Override
  public GameplaySettings settings() {
    return this.settings;
  }
}
