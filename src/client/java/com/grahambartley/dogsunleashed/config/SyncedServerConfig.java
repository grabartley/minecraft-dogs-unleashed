package com.grahambartley.dogsunleashed.config;

public final class SyncedServerConfig {

  private static volatile DogsUnleashedConfig current = DogsUnleashedConfig.defaults();

  private SyncedServerConfig() {}

  public static DogsUnleashedConfig get() {
    return current;
  }

  public static void apply(final DogsUnleashedConfig config) {
    current = config;
  }

  public static void reset() {
    current = DogsUnleashedConfig.defaults();
  }
}
