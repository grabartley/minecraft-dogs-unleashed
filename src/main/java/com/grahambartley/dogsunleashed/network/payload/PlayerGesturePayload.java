package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record PlayerGesturePayload(
    int entityId, boolean mainHand, PlayerGesture gesture, int targetEntityId)
    implements CustomPayload {

  public static final int NO_TARGET = -1;

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "player_gesture");
  public static final CustomPayload.Id<PlayerGesturePayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, PlayerGesturePayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          PlayerGesturePayload::entityId,
          PacketCodecs.BOOL,
          PlayerGesturePayload::mainHand,
          PacketCodecs.indexed(index -> PlayerGesture.values()[index], PlayerGesture::ordinal)
              .cast(),
          PlayerGesturePayload::gesture,
          PacketCodecs.VAR_INT,
          PlayerGesturePayload::targetEntityId,
          PlayerGesturePayload::new);

  public static PlayerGesturePayload of(
      final int entityId, final Hand hand, final PlayerGesture gesture, final int targetEntityId) {
    return new PlayerGesturePayload(entityId, hand == Hand.MAIN_HAND, gesture, targetEntityId);
  }

  public Hand hand() {
    return this.mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
  }

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
