package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.SleepSettings;

final class SleepConfigSection {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private SleepSettings settings;

  SleepConfigSection(final SleepSettings settings) {
    this.settings = settings;
  }

  void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "sleep"));
    list.addRow(
        rows.toggle(
            KEY + "autosleep",
            KEY + "autosleep.tooltip",
            this.settings.autoSleepEnabled(),
            value -> this.settings = this.settings.withAutoSleepEnabled(value)));
    list.addRow(
        rows.intSlider(
            KEY + "autosleeprange",
            KEY + "autosleeprange.tooltip",
            SleepSettings.RANGE_MIN,
            SleepSettings.RANGE_MAX,
            this.settings.autoSleepRangeBlocks(),
            value -> this.settings = this.settings.withAutoSleepRangeBlocks(value)));
  }

  SleepSettings settings() {
    return this.settings;
  }
}
