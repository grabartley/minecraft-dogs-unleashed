package com.grahambartley.dogsunleashed.gametest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.test.TestFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeneratedGameTestTest {

  private static TestFunction generated() {
    return GeneratedGameTest.of("a-batch", "a.test.name", "a:structure", 42, context -> {});
  }

  @Test
  @DisplayName("a generated test is required, so a failure fails the suite instead of being noted")
  void aGeneratedTestIsRequired() {
    assertTrue(generated().required());
  }

  @Test
  @DisplayName("a generated test asks for no setup ticks, so its body runs from the first tick")
  void aGeneratedTestAsksForNoSetupTicks() {
    assertEquals(0L, generated().setupTicks());
  }

  @Test
  @DisplayName("the batch, test name, structure and tick limit reach the test unchanged")
  void theCallersOwnValuesAreCarriedThrough() {
    final TestFunction function = generated();
    assertEquals("a-batch", function.batchId());
    assertEquals("a.test.name", function.templatePath());
    assertEquals("a:structure", function.templateName());
    assertEquals(42, function.tickLimit());
  }

  @Test
  @DisplayName("the body the caller supplied is the body the test runs")
  void theCallersBodyIsTheBodyThatRuns() {
    final boolean[] ran = {false};
    final TestFunction function = GeneratedGameTest.of("b", "n", "s", 1, context -> ran[0] = true);
    function.starter().accept(null);
    assertTrue(ran[0]);
  }
}
