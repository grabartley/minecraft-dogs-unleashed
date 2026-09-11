package com.grahambartley.dogsunleashed.gametest.util;

import java.util.function.Consumer;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;

public final class GeneratedGameTest {

  private static final long NO_SETUP_TICKS = 0L;
  private static final boolean REQUIRED = true;

  private GeneratedGameTest() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static TestFunction of(
      final String batchId,
      final String testName,
      final String templateName,
      final int tickLimit,
      final Consumer<TestContext> body) {
    return new TestFunction(
        batchId, testName, templateName, tickLimit, NO_SETUP_TICKS, REQUIRED, body);
  }
}
