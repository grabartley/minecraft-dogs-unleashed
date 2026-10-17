package com.grahambartley.dogsunleashed.config;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class SyncedServerConfig {

  private static volatile DogsUnleashedConfig current = DogsUnleashedConfig.defaults();

  private SyncedServerConfig() {}

  public static DogsUnleashedConfig get() {
    return current;
  }

  public static void apply(final DogsUnleashedConfig config) {
    current = config;
  }

  public static void resetOnDisconnect() {
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
  }

  public static void reset() {
    current = DogsUnleashedConfig.defaults();
  }
}
