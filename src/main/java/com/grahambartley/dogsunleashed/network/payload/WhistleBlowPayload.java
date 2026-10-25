package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record WhistleBlowPayload(int entityId, boolean mainHand) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "whistle_blow");
  public static final CustomPayload.Id<WhistleBlowPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, WhistleBlowPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          WhistleBlowPayload::entityId,
          PacketCodecs.BOOL,
          WhistleBlowPayload::mainHand,
          WhistleBlowPayload::new);

  public static WhistleBlowPayload of(final int entityId, final Hand hand) {
    return new WhistleBlowPayload(entityId, hand == Hand.MAIN_HAND);
  }

  public Hand hand() {
    return this.mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
  }

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
