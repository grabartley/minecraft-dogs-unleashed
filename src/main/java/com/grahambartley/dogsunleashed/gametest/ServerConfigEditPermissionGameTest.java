package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.server.ServerConfigEditPermission;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class ServerConfigEditPermissionGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void operatorCanEditSettings(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final OperatorList operators = player.getServer().getPlayerManager().getOpList();
    operators.add(
        new OperatorEntry(player.getGameProfile(), ServerConfigService.OP_PERMISSION_LEVEL, false));
    try {
      context.assertTrue(
          ServerConfigEditPermission.canEdit(player, player.getServer()),
          "An operator must be allowed to edit settings");
    } finally {
      operators.remove(player.getGameProfile());
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void nonOperatorWhoIsNotHostCannotEditSettings(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.assertFalse(
        player.getServer().isHost(player.getGameProfile()),
        "The test server must not treat the mock player as its host");
    context.assertFalse(
        ServerConfigEditPermission.canEdit(player, player.getServer()),
        "A player who is neither an operator nor the host must not edit settings");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void nonOperatorWithNoLocalServerCannotEditSettings(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.assertFalse(
        ServerConfigEditPermission.canEdit(player, null),
        "Without a local server only operator permission can grant edits");
    context.complete();
  }
}
