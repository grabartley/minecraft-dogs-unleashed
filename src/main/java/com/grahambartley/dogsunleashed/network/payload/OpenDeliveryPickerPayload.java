package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.network.PacketLimits;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record OpenDeliveryPickerPayload(UUID dogId, String dogName, List<Recipient> recipients)
    implements CustomPayload {

  public record Recipient(UUID id, String name) {

    public static final PacketCodec<RegistryByteBuf, Recipient> CODEC =
        PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            Recipient::id,
            PacketCodecs.string(PacketLimits.OWNER_NAME_MAX_LENGTH),
            Recipient::name,
            Recipient::new);
  }

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_delivery_picker");
  public static final CustomPayload.Id<OpenDeliveryPickerPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, OpenDeliveryPickerPayload> CODEC =
      PacketCodec.tuple(
          Uuids.PACKET_CODEC,
          OpenDeliveryPickerPayload::dogId,
          PacketCodecs.string(PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH),
          OpenDeliveryPickerPayload::dogName,
          Recipient.CODEC.collect(PacketCodecs.toList(PacketLimits.DELIVERY_RECIPIENTS_MAX_SIZE)),
          OpenDeliveryPickerPayload::recipients,
          OpenDeliveryPickerPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
