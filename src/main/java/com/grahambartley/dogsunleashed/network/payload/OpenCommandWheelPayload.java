package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenCommandWheelPayload(
    int entityId,
    UUID dogId,
    int currentCommandId,
    boolean hasBed,
    String dogName,
    String rideRefusalKey)
    implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_command_wheel");
  public static final CustomPayload.Id<OpenCommandWheelPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, OpenCommandWheelPayload> CODEC =
      PacketCodec.of(OpenCommandWheelPayload::write, OpenCommandWheelPayload::read);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }

  private void write(final RegistryByteBuf buf) {
    buf.writeVarInt(this.entityId);
    buf.writeUuid(this.dogId);
    buf.writeVarInt(this.currentCommandId);
    buf.writeBoolean(this.hasBed);
    buf.writeString(this.dogName);
    buf.writeString(this.rideRefusalKey);
  }

  public boolean rideable() {
    return this.rideRefusalKey.isEmpty();
  }

  private static OpenCommandWheelPayload read(final RegistryByteBuf buf) {
    return new OpenCommandWheelPayload(
        buf.readVarInt(),
        buf.readUuid(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readString(),
        buf.readString());
  }
}
