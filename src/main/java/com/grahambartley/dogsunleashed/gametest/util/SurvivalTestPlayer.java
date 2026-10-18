package com.grahambartley.dogsunleashed.gametest.util;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class SurvivalTestPlayer {

  private SurvivalTestPlayer() {}

  public static ServerPlayerEntity joinAt(final TestContext context, final Vec3d relativePos) {
    final ConnectedClientData data =
        ConnectedClientData.createDefault(
            new GameProfile(UUID.randomUUID(), "test-survival-player"), false);
    final ServerPlayerEntity player =
        new ServerPlayerEntity(
            context.getWorld().getServer(),
            context.getWorld(),
            data.gameProfile(),
            data.syncedOptions());
    final ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
    new EmbeddedChannel(connection);
    context.getWorld().getServer().getPlayerManager().onPlayerConnect(connection, player, data);
    player.changeGameMode(GameMode.SURVIVAL);
    final Vec3d pos = context.getAbsolute(relativePos);
    player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0f, 0.0f);
    return player;
  }

  public static void leave(final TestContext context, final ServerPlayerEntity player) {
    context.getWorld().getServer().getPlayerManager().remove(player);
  }
}
