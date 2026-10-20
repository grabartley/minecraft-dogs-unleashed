package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record AgingSettings(boolean dogAgingEnabled, int seniorAfterDays) {

  public static final int DAYS_MIN = 1;
  public static final int DAYS_MAX = 1000;

  public static final AgingSettings DEFAULTS = new AgingSettings(false, 100);

  static final String KEY_DOG_AGING_ENABLED = "dogAgingEnabled";
  static final String KEY_SENIOR_AFTER_DAYS = "seniorAfterDays";

  public AgingSettings {
    seniorAfterDays = ConfigBounds.clamp(seniorAfterDays, DAYS_MIN, DAYS_MAX);
  }

  public AgingSettings withDogAgingEnabled(final boolean value) {
    return new AgingSettings(value, seniorAfterDays);
  }

  public AgingSettings withSeniorAfterDays(final int value) {
    return new AgingSettings(dogAgingEnabled, value);
  }

  static AgingSettings fromJson(final ConfigJsonFields fields) {
    return new AgingSettings(
        fields.readBoolean(KEY_DOG_AGING_ENABLED, DEFAULTS.dogAgingEnabled),
        fields.readInt(KEY_SENIOR_AFTER_DAYS, DEFAULTS.seniorAfterDays, DAYS_MIN, DAYS_MAX));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_DOG_AGING_ENABLED, dogAgingEnabled);
    root.addProperty(KEY_SENIOR_AFTER_DAYS, seniorAfterDays);
  }
}
