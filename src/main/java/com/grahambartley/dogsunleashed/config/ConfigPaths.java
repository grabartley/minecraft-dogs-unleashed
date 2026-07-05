package com.grahambartley.dogsunleashed.config;

import java.nio.file.Path;

public final class ConfigPaths {
  private static final String DOGS_UNLEASHED_DIR = "dogs-unleashed";
  private static final String SERVER_CONFIG_FILE = "server-config.json";

  private final Path dogsUnleashedDir;

  public ConfigPaths(Path worldDir) {
    this.dogsUnleashedDir = worldDir.resolve(DOGS_UNLEASHED_DIR);
  }

  public Path getDogsUnleashedDir() {
    return dogsUnleashedDir;
  }

  public Path getServerConfigPath() {
    return dogsUnleashedDir.resolve(SERVER_CONFIG_FILE);
  }
}
