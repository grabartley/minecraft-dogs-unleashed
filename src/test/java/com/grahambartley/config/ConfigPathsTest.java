package com.grahambartley.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigPathsTest {

  private static final Path WORLD_DIR = Path.of("/saves/world1");

  static Stream<Arguments> pathAccessors() {
    final Function<ConfigPaths, Path> dir = ConfigPaths::getDogsUnleashedDir;
    final Function<ConfigPaths, Path> serverConfig = ConfigPaths::getServerConfigPath;
    return Stream.of(
        Arguments.of("getDogsUnleashedDir", dir, WORLD_DIR.resolve("dogs-unleashed")),
        Arguments.of(
            "getServerConfigPath",
            serverConfig,
            WORLD_DIR.resolve("dogs-unleashed/server-config.json")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pathAccessors")
  @DisplayName("config paths nest under <world>/dogs-unleashed")
  void configPathsNestUnderDogsUnleashedDir(
      final String label, final Function<ConfigPaths, Path> accessor, final Path expected) {
    final ConfigPaths paths = new ConfigPaths(WORLD_DIR);
    assertEquals(expected, accessor.apply(paths));
  }
}
