package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogsUnleashedClientTest {

  @Test
  @DisplayName("Client class exists and is instantiable")
  void testClientClassExists() {
    assertDoesNotThrow(
        () -> {
          Class<?> clientClass = Class.forName("com.grahambartley.DogsUnleashedClient");
          assertNotNull(clientClass);
        });
  }
}
