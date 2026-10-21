package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record DeliverDogPayload(UUID dogId, UUID recipientId) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "deliver_dog");
  public static final CustomPayload.Id<DeliverDogPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, DeliverDogPayload> CODEC =
      PacketCodec.tuple(
          Uuids.PACKET_CODEC,
          DeliverDogPayload::dogId,
          Uuids.PACKET_CODEC,
          DeliverDogPayload::recipientId,
          DeliverDogPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
