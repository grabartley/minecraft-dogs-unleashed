package com.grahambartley.dogsunleashed.gametest.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;

public final class CreativeTestPlayer {

  private CreativeTestPlayer() {}

  @SuppressWarnings("removal")
  public static ServerPlayerEntity join(final TestContext context) {
    return context.createMockCreativeServerPlayerInWorld();
  }
}
