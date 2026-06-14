package com.grahambartley.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigPathsTest {

  @Test
  @DisplayName("getServerConfigPath nests under <world>/dogs-unleashed/server-config.json")
  void serverConfigPathHasExpectedLayout() {
    final Path worldDir = Path.of("/saves/world1");
    final ConfigPaths paths = new ConfigPaths(worldDir);
    assertEquals(
        worldDir.resolve("dogs-unleashed/server-config.json"), paths.getServerConfigPath());
  }

  @Test
  @DisplayName("getDogsUnleashedDir nests under <world>/dogs-unleashed")
  void dogsUnleashedDirHasExpectedLayout() {
    final Path worldDir = Path.of("/saves/world1");
    final ConfigPaths paths = new ConfigPaths(worldDir);
    assertEquals(worldDir.resolve("dogs-unleashed"), paths.getDogsUnleashedDir());
  }
}
