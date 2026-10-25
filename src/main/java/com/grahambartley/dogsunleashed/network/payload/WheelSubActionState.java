package com.grahambartley.dogsunleashed.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record WheelSubActionState(
    int id, String translationKey, Identifier icon, String refusalKey) {

  public static final int MAX_PER_WHEEL = 16;

  public static final PacketCodec<RegistryByteBuf, WheelSubActionState> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          WheelSubActionState::id,
          PacketCodecs.STRING,
          WheelSubActionState::translationKey,
          Identifier.PACKET_CODEC,
          WheelSubActionState::icon,
          PacketCodecs.STRING,
          WheelSubActionState::refusalKey,
          WheelSubActionState::new);

  public boolean available() {
    return this.refusalKey.isEmpty();
  }
}
