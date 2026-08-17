package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SelectWheelActionPayload(UUID dogId, int actionId) implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "select_wheel_action");
  public static final CustomPayload.Id<SelectWheelActionPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SelectWheelActionPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
          SelectWheelActionPayload::dogId,
          PacketCodecs.VAR_INT,
          SelectWheelActionPayload::actionId,
          SelectWheelActionPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
