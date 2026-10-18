package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record GameplaySettings(
    boolean gravesEnabled, boolean dropLeashOnPlayMode, boolean showDogNames) {

  public static final GameplaySettings DEFAULTS = new GameplaySettings(true, true, true);

  static final String KEY_GRAVES_ENABLED = "gravesEnabled";
  static final String KEY_DROP_LEASH_ON_PLAY_MODE = "dropLeashOnPlayMode";
  static final String KEY_SHOW_DOG_NAMES = "showDogNames";

  public GameplaySettings withGravesEnabled(final boolean value) {
    return new GameplaySettings(value, dropLeashOnPlayMode, showDogNames);
  }

  public GameplaySettings withDropLeashOnPlayMode(final boolean value) {
    return new GameplaySettings(gravesEnabled, value, showDogNames);
  }

  public GameplaySettings withShowDogNames(final boolean value) {
    return new GameplaySettings(gravesEnabled, dropLeashOnPlayMode, value);
  }

  static GameplaySettings fromJson(final ConfigJsonFields fields) {
    return new GameplaySettings(
        fields.readBoolean(KEY_GRAVES_ENABLED, DEFAULTS.gravesEnabled),
        fields.readBoolean(KEY_DROP_LEASH_ON_PLAY_MODE, DEFAULTS.dropLeashOnPlayMode),
        fields.readBoolean(KEY_SHOW_DOG_NAMES, DEFAULTS.showDogNames));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_GRAVES_ENABLED, gravesEnabled);
    root.addProperty(KEY_DROP_LEASH_ON_PLAY_MODE, dropLeashOnPlayMode);
    root.addProperty(KEY_SHOW_DOG_NAMES, showDogNames);
  }
}
