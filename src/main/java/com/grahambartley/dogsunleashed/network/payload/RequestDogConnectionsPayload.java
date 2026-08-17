package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestDogConnectionsPayload(UUID dogId) implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_dog_connections");
  public static final CustomPayload.Id<RequestDogConnectionsPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, RequestDogConnectionsPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
          RequestDogConnectionsPayload::dogId,
          RequestDogConnectionsPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
