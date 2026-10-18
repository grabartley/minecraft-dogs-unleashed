package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record DireWolfPackSettings(boolean hunt, boolean defend) {

  public static final DireWolfPackSettings DEFAULTS = new DireWolfPackSettings(true, true);

  static final String KEY_HUNT = "hunt";
  static final String KEY_DEFEND = "defend";

  public DireWolfPackSettings withHunt(final boolean value) {
    return new DireWolfPackSettings(value, defend);
  }

  public DireWolfPackSettings withDefend(final boolean value) {
    return new DireWolfPackSettings(hunt, value);
  }

  static DireWolfPackSettings fromJson(final JsonObject json) {
    final ConfigJsonFields fields = new ConfigJsonFields(json);
    return new DireWolfPackSettings(
        fields.readBoolean(KEY_HUNT, DEFAULTS.hunt),
        fields.readBoolean(KEY_DEFEND, DEFAULTS.defend));
  }

  JsonObject toJson() {
    final JsonObject json = new JsonObject();
    json.addProperty(KEY_HUNT, hunt);
    json.addProperty(KEY_DEFEND, defend);
    return json;
  }
}
