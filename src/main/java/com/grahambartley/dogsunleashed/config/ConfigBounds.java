package com.grahambartley.dogsunleashed.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigBounds {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBounds.class);

  private ConfigBounds() {}

  public static int clamp(final int value, final int min, final int max) {
    return Math.max(min, Math.min(max, value));
  }

  public static float clamp(
      final float value, final float min, final float max, final float fallback) {
    if (!Float.isFinite(value)) {
      return fallback;
    }
    return Math.max(min, Math.min(max, value));
  }

  static void warnIfOutside(
      final String key, final Number value, final Number min, final Number max) {
    if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
      LOGGER.warn(
          "Dogs Unleashed config value {}={} out of range [{}, {}], clamping",
          key,
          value,
          min,
          max);
    }
  }
}
