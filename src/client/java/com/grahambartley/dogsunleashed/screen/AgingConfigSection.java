package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.AgingSettings;

final class AgingConfigSection {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private AgingSettings settings;

  AgingConfigSection(final AgingSettings settings) {
    this.settings = settings;
  }

  void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "aging"));
    list.addRow(
        rows.toggle(
            KEY + "dogaging",
            KEY + "dogaging.tooltip",
            this.settings.dogAgingEnabled(),
            value -> this.settings = this.settings.withDogAgingEnabled(value)));
    list.addRow(
        rows.intSlider(
            KEY + "senioragedays",
            KEY + "senioragedays.tooltip",
            AgingSettings.DAYS_MIN,
            AgingSettings.DAYS_MAX,
            this.settings.seniorAfterDays(),
            value -> this.settings = this.settings.withSeniorAfterDays(value)));
  }

  AgingSettings settings() {
    return this.settings;
  }
}
