package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SummonPetPayload(UUID petId) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "summon_pet");
  public static final CustomPayload.Id<SummonPetPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SummonPetPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
          SummonPetPayload::petId,
          SummonPetPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
