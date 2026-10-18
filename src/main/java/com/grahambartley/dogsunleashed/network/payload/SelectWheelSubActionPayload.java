package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record SelectWheelSubActionPayload(UUID dogId, int subActionId) implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "select_wheel_sub_action");
  public static final CustomPayload.Id<SelectWheelSubActionPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SelectWheelSubActionPayload> CODEC =
      PacketCodec.tuple(
          Uuids.PACKET_CODEC,
          SelectWheelSubActionPayload::dogId,
          PacketCodecs.VAR_INT,
          SelectWheelSubActionPayload::subActionId,
          SelectWheelSubActionPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
