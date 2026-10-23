package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public final class AdvancementTestHelper {

  private AdvancementTestHelper() {}

  public static AdvancementEntry advancement(final TestContext context, final String path) {
    final AdvancementEntry advancement =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, path));
    context.assertTrue(advancement != null, "Missing advancement: " + path);
    return advancement;
  }

  public static boolean isDone(
      final TestContext context, final ServerPlayerEntity player, final String path) {
    return player.getAdvancementTracker().getProgress(advancement(context, path)).isDone();
  }
}
