package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatueMaterialTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");
  private static final Path MODELS = Path.of("src/main/resources/assets/dogs-unleashed/models");

  @ParameterizedTest(name = "{0} has a colour ramp")
  @EnumSource(StatueMaterial.class)
  @DisplayName("every material id names a committed colour ramp")
  void everyMaterialHasARamp(final StatueMaterial material) {
    assertTrue(Files.isRegularFile(ASSETS.resolve("textures/statue/" + material.id() + ".png")));
  }

  @ParameterizedTest(name = "{0} has a base model")
  @EnumSource(StatueMaterial.class)
  @DisplayName("every material id names a committed base model")
  void everyMaterialHasABaseModel(final StatueMaterial material) {
    assertTrue(Files.isRegularFile(MODELS.resolve("block/statue_base_" + material.id() + ".json")));
  }
}
