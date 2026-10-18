package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record SleepSettings(boolean autoSleepEnabled, int autoSleepRangeBlocks) {

  public static final int RANGE_MIN = 4;
  public static final int RANGE_MAX = 128;

  public static final SleepSettings DEFAULTS = new SleepSettings(true, 32);

  static final String KEY_AUTO_SLEEP_ENABLED = "autoSleepEnabled";
  static final String KEY_AUTO_SLEEP_RANGE_BLOCKS = "autoSleepRangeBlocks";

  public SleepSettings {
    autoSleepRangeBlocks = ConfigBounds.clamp(autoSleepRangeBlocks, RANGE_MIN, RANGE_MAX);
  }

  public SleepSettings withAutoSleepEnabled(final boolean value) {
    return new SleepSettings(value, autoSleepRangeBlocks);
  }

  public SleepSettings withAutoSleepRangeBlocks(final int value) {
    return new SleepSettings(autoSleepEnabled, value);
  }

  static SleepSettings fromJson(final ConfigJsonFields fields) {
    return new SleepSettings(
        fields.readBoolean(KEY_AUTO_SLEEP_ENABLED, DEFAULTS.autoSleepEnabled),
        fields.readInt(
            KEY_AUTO_SLEEP_RANGE_BLOCKS, DEFAULTS.autoSleepRangeBlocks, RANGE_MIN, RANGE_MAX));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_AUTO_SLEEP_ENABLED, autoSleepEnabled);
    root.addProperty(KEY_AUTO_SLEEP_RANGE_BLOCKS, autoSleepRangeBlocks);
  }
}
