package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyncedServerConfigTest {

  private static final DogsUnleashedConfig SYNCED =
      DogsUnleashedConfig.defaults().withGravesEnabled(false);

  @AfterEach
  void resetHolders() {
    SyncedServerConfig.reset();
    DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.defaults();
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
  @DisplayName("applying a synced copy never writes the server's authoritative config")
  void applyLeavesTheServerConfigAlone() {
    final DogsUnleashedConfig authoritative = DogsUnleashedConfig.defaults();
    DogsUnleashed.SERVER_CONFIG = authoritative;

    SyncedServerConfig.apply(SYNCED);

    assertSame(authoritative, DogsUnleashed.SERVER_CONFIG);
  }

  @Test
  @DisplayName("reset drops the last server's values")
  void resetDropsTheLastServersValues() {
    SyncedServerConfig.apply(SYNCED);

    SyncedServerConfig.reset();

    assertEquals(DogsUnleashedConfig.defaults(), SyncedServerConfig.get());
  }
}
