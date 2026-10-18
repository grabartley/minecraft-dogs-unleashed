package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record DireWolfStaminaSettings(boolean drains, int capacityPercent, int recoveryPercent) {

  public static final int PERCENT_MIN = 25;
  public static final int PERCENT_MAX = 400;

  public static final DireWolfStaminaSettings DEFAULTS =
      new DireWolfStaminaSettings(true, 100, 100);

  static final String KEY_DRAINS = "drains";
  static final String KEY_CAPACITY_PERCENT = "capacityPercent";
  static final String KEY_RECOVERY_PERCENT = "recoveryPercent";

  public DireWolfStaminaSettings {
    capacityPercent = clamp(capacityPercent);
    recoveryPercent = clamp(recoveryPercent);
  }

  public DireWolfStaminaSettings withDrains(final boolean value) {
    return new DireWolfStaminaSettings(value, capacityPercent, recoveryPercent);
  }

  public DireWolfStaminaSettings withCapacityPercent(final int value) {
    return new DireWolfStaminaSettings(drains, value, recoveryPercent);
  }

  public DireWolfStaminaSettings withRecoveryPercent(final int value) {
    return new DireWolfStaminaSettings(drains, capacityPercent, value);
  }

  static DireWolfStaminaSettings fromJson(final JsonObject json) {
    final ConfigJsonFields fields = new ConfigJsonFields(json);
    return new DireWolfStaminaSettings(
        fields.readBoolean(KEY_DRAINS, DEFAULTS.drains),
        fields.readInt(KEY_CAPACITY_PERCENT, DEFAULTS.capacityPercent, PERCENT_MIN, PERCENT_MAX),
        fields.readInt(KEY_RECOVERY_PERCENT, DEFAULTS.recoveryPercent, PERCENT_MIN, PERCENT_MAX));
  }

  JsonObject toJson() {
    final JsonObject json = new JsonObject();
    json.addProperty(KEY_DRAINS, drains);
    json.addProperty(KEY_CAPACITY_PERCENT, capacityPercent);
    json.addProperty(KEY_RECOVERY_PERCENT, recoveryPercent);
    return json;
  }

  private static int clamp(final int value) {
    return Math.max(PERCENT_MIN, Math.min(PERCENT_MAX, value));
  }
}
