package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestPetManagerStatePayload() implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_pet_manager_state");
  public static final CustomPayload.Id<RequestPetManagerStatePayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, RequestPetManagerStatePayload> CODEC =
      PacketCodec.unit(new RequestPetManagerStatePayload());

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
