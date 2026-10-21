package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SledSprintPayload(boolean held) implements CustomPayload {

  public static final CustomPayload.Id<SledSprintPayload> ID =
      new CustomPayload.Id<>(Identifier.of(DogsUnleashed.MOD_ID, "sled_sprint"));
  public static final PacketCodec<RegistryByteBuf, SledSprintPayload> CODEC =
      PacketCodec.tuple(PacketCodecs.BOOL, SledSprintPayload::held, SledSprintPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
