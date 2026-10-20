package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record GameplaySettings(
    boolean gravesEnabled, boolean dropLeashOnPlayMode, boolean showDogNames, boolean muddyDogs) {

  public static final GameplaySettings DEFAULTS = new GameplaySettings(true, true, true, true);

  static final String KEY_GRAVES_ENABLED = "gravesEnabled";
  static final String KEY_DROP_LEASH_ON_PLAY_MODE = "dropLeashOnPlayMode";
  static final String KEY_SHOW_DOG_NAMES = "showDogNames";
  static final String KEY_MUDDY_DOGS = "muddyDogs";

  public GameplaySettings withGravesEnabled(final boolean value) {
    return new GameplaySettings(value, dropLeashOnPlayMode, showDogNames, muddyDogs);
  }

  public GameplaySettings withDropLeashOnPlayMode(final boolean value) {
    return new GameplaySettings(gravesEnabled, value, showDogNames, muddyDogs);
  }

  public GameplaySettings withShowDogNames(final boolean value) {
    return new GameplaySettings(gravesEnabled, dropLeashOnPlayMode, value, muddyDogs);
  }

  public GameplaySettings withMuddyDogs(final boolean value) {
    return new GameplaySettings(gravesEnabled, dropLeashOnPlayMode, showDogNames, value);
  }

  static GameplaySettings fromJson(final ConfigJsonFields fields) {
    return new GameplaySettings(
        fields.readBoolean(KEY_GRAVES_ENABLED, DEFAULTS.gravesEnabled),
        fields.readBoolean(KEY_DROP_LEASH_ON_PLAY_MODE, DEFAULTS.dropLeashOnPlayMode),
        fields.readBoolean(KEY_SHOW_DOG_NAMES, DEFAULTS.showDogNames),
        fields.readBoolean(KEY_MUDDY_DOGS, DEFAULTS.muddyDogs));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_GRAVES_ENABLED, gravesEnabled);
    root.addProperty(KEY_DROP_LEASH_ON_PLAY_MODE, dropLeashOnPlayMode);
    root.addProperty(KEY_SHOW_DOG_NAMES, showDogNames);
    root.addProperty(KEY_MUDDY_DOGS, muddyDogs);
  }
}
