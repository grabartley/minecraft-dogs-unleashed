package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetPetNamePayload(UUID petId, String name) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "set_pet_name");
  public static final CustomPayload.Id<SetPetNamePayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SetPetNamePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
          SetPetNamePayload::petId,
          PacketCodecs.string(SET_PET_NAME_NAME_MAX_LENGTH),
          SetPetNamePayload::name,
          SetPetNamePayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
