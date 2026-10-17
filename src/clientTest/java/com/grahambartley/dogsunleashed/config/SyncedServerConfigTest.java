package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyncedServerConfigTest {

  private static final DogsUnleashedConfig SYNCED =
      DogsUnleashedConfig.defaults().withGravesEnabled(false);

  @AfterEach
  void resetHolders() {
    SyncedServerConfig.reset();
  }

  @Test
  @DisplayName("starts on defaults before any server has synced")
  void startsOnDefaults() {
    assertEquals(DogsUnleashedConfig.defaults(), SyncedServerConfig.get());
  }

  @Test
  @DisplayName("holds the config the server synced")
  void holdsTheSyncedConfig() {
    SyncedServerConfig.apply(SYNCED);

    assertSame(SYNCED, SyncedServerConfig.get());
  }

  @Test
  @DisplayName("reset drops the last server's values")
  void resetDropsTheLastServersValues() {
    SyncedServerConfig.apply(SYNCED);

    SyncedServerConfig.reset();

    assertEquals(DogsUnleashedConfig.defaults(), SyncedServerConfig.get());
  }
}
