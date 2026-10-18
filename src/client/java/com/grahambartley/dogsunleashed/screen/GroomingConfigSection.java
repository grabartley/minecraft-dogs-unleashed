package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.FurSettings;

final class GroomingConfigSection implements ConfigSection<FurSettings> {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private FurSettings settings;

  GroomingConfigSection(final FurSettings settings) {
    this.settings = settings;
  }

  @Override
  public void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "grooming"));
    list.addRow(
        rows.toggle(
            KEY + "furgrowth",
            KEY + "furgrowth.tooltip",
            this.settings.furGrowthEnabled(),
            value -> this.settings = this.settings.withFurGrowthEnabled(value)));
    list.addRow(
        rows.percentSlider(
            KEY + "furgrowthrate",
            FurSettings.PERCENT_MIN,
            FurSettings.PERCENT_MAX,
            this.settings.furGrowthRateMultiplierPercent(),
            value -> this.settings = this.settings.withFurGrowthRateMultiplierPercent(value)));
    list.addRow(
        rows.percentSlider(
            KEY + "badhairdaychance",
            FurSettings.PERCENT_MIN,
            FurSettings.PERCENT_MAX,
            this.settings.badHairDayChanceMultiplierPercent(),
            value -> this.settings = this.settings.withBadHairDayChanceMultiplierPercent(value)));
  }

  @Override
  public FurSettings settings() {
    return this.settings;
  }
}
