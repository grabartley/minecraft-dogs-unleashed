package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record SoundSettings(float barkVolume, float howlVolume) {

  public static final float VOLUME_MIN = 0.0f;
  public static final float VOLUME_MAX = 2.0f;

  public static final SoundSettings DEFAULTS = new SoundSettings(1.0f, 1.5f);

  static final String KEY_BARK_VOLUME = "barkVolume";
  static final String KEY_HOWL_VOLUME = "howlVolume";

  public SoundSettings {
    barkVolume = ConfigBounds.clamp(barkVolume, VOLUME_MIN, VOLUME_MAX);
    howlVolume = ConfigBounds.clamp(howlVolume, VOLUME_MIN, VOLUME_MAX);
  }

  public SoundSettings withBarkVolume(final float value) {
    return new SoundSettings(value, howlVolume);
  }

  public SoundSettings withHowlVolume(final float value) {
    return new SoundSettings(barkVolume, value);
  }

  static SoundSettings fromJson(final ConfigJsonFields fields) {
    return new SoundSettings(
        fields.readFloat(KEY_BARK_VOLUME, DEFAULTS.barkVolume, VOLUME_MIN, VOLUME_MAX),
        fields.readFloat(KEY_HOWL_VOLUME, DEFAULTS.howlVolume, VOLUME_MIN, VOLUME_MAX));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_BARK_VOLUME, barkVolume);
    root.addProperty(KEY_HOWL_VOLUME, howlVolume);
  }
}
