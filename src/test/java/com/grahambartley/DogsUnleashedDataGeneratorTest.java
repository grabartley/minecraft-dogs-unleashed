package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogsUnleashedDataGeneratorTest {

  @Test
  @DisplayName("Template datagen entrypoint class stays deleted")
  void templateDatagenEntrypointClassStaysDeleted() {
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("com.grahambartley.DogsUnleashedDataGenerator"));
  }
}
