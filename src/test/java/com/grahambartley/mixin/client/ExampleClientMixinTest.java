package com.grahambartley.mixin.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExampleClientMixinTest {

  @Test
  @DisplayName("Template client mixin class stays deleted")
  void templateClientMixinClassStaysDeleted() {
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("com.grahambartley.mixin.client.ExampleClientMixin"));
  }
}
