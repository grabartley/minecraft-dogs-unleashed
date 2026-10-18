package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RideSprintPayload(boolean sprinting) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "ride_sprint");
  public static final CustomPayload.Id<RideSprintPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, RideSprintPayload> CODEC =
      PacketCodec.tuple(PacketCodecs.BOOL, RideSprintPayload::sprinting, RideSprintPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
