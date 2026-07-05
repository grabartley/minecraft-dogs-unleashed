package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class ServerConfigPayloads {
  public static final Identifier SYNC_SERVER_CONFIG_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_server_config");
  public static final Identifier EDIT_SERVER_CONFIG_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "edit_server_config");

  private ServerConfigPayloads() {}

  public record SyncServerConfigS2CPayload(DogsUnleashedConfig config) implements CustomPayload {
    public static final CustomPayload.Id<SyncServerConfigS2CPayload> ID =
        new CustomPayload.Id<>(SYNC_SERVER_CONFIG_ID);
    public static final PacketCodec<RegistryByteBuf, SyncServerConfigS2CPayload> CODEC =
        PacketCodec.of(SyncServerConfigS2CPayload::write, SyncServerConfigS2CPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      writeConfig(buf, config);
    }

    private static SyncServerConfigS2CPayload read(final RegistryByteBuf buf) {
      return new SyncServerConfigS2CPayload(readConfig(buf));
    }
  }

  public record EditServerConfigC2SPayload(DogsUnleashedConfig config) implements CustomPayload {
    public static final CustomPayload.Id<EditServerConfigC2SPayload> ID =
        new CustomPayload.Id<>(EDIT_SERVER_CONFIG_ID);
    public static final PacketCodec<RegistryByteBuf, EditServerConfigC2SPayload> CODEC =
        PacketCodec.of(EditServerConfigC2SPayload::write, EditServerConfigC2SPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      writeConfig(buf, config);
    }

    private static EditServerConfigC2SPayload read(final RegistryByteBuf buf) {
      return new EditServerConfigC2SPayload(readConfig(buf));
    }
  }

  private static void writeConfig(final RegistryByteBuf buf, final DogsUnleashedConfig config) {
    buf.writeBoolean(config.enableNaturalSpawning());
    buf.writeBoolean(config.gravesEnabled());
    buf.writeBoolean(config.autoSleepEnabled());
    buf.writeInt(config.autoSleepRangeBlocks());
    buf.writeFloat(config.barkVolume());
    buf.writeFloat(config.howlVolume());
  }

  private static DogsUnleashedConfig readConfig(final RegistryByteBuf buf) {
    return new DogsUnleashedConfig(
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readBoolean(),
        buf.readInt(),
        buf.readFloat(),
        buf.readFloat());
  }
}
