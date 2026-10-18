package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.config.SoundSettings;

final class SoundConfigSection implements ConfigSection<SoundSettings> {
  private static final String KEY = "screen.dogs-unleashed.settings.";

  private SoundSettings settings;

  SoundConfigSection(final SoundSettings settings) {
    this.settings = settings;
  }

  @Override
  public void addTo(final DogsUnleashedConfigList list, final ConfigRows rows) {
    list.addRow(rows.header(KEY + "sounds"));
    list.addRow(
        rows.floatSlider(
            KEY + "barkvolume",
            KEY + "barkvolume.tooltip",
            SoundSettings.VOLUME_MIN,
            SoundSettings.VOLUME_MAX,
            this.settings.barkVolume(),
            value -> this.settings = this.settings.withBarkVolume(value)));
    list.addRow(
        rows.floatSlider(
            KEY + "howlvolume",
            KEY + "howlvolume.tooltip",
            SoundSettings.VOLUME_MIN,
            SoundSettings.VOLUME_MAX,
            this.settings.howlVolume(),
            value -> this.settings = this.settings.withHowlVolume(value)));
  }

  @Override
  public SoundSettings settings() {
    return this.settings;
  }
}
