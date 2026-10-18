package com.grahambartley.dogsunleashed.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record WheelSubActionState(int id, String refusalKey) {

  public static final PacketCodec<RegistryByteBuf, WheelSubActionState> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          WheelSubActionState::id,
          PacketCodecs.STRING,
          WheelSubActionState::refusalKey,
          WheelSubActionState::new);

  public boolean available() {
    return this.refusalKey.isEmpty();
  }
}
