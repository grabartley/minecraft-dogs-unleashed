package com.grahambartley.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickModelTest {
  private static final Path STICK_MODEL_SOURCE =
      Path.of("src/client/java/com/grahambartley/model/StickModel.java");

  @Test
  @DisplayName("stick model should use the dedicated joined stick texture")
  void stickModelShouldUseDedicatedStickTexture() throws IOException {
    String source = Files.readString(STICK_MODEL_SOURCE);

    assertTrue(source.contains("return Identifier.of(MOD_ID, \"textures/block/stick.png\");"));
    assertTrue(source.contains("return Identifier.of(MOD_ID, \"geo/stick.geo.json\");"));
  }
}
