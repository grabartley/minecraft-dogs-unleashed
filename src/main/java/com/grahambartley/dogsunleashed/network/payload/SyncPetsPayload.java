package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPetsPayload(List<PetSyncData> pets) implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "sync_pets");
  public static final CustomPayload.Id<SyncPetsPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SyncPetsPayload> CODEC =
      PacketCodec.tuple(
          PetSyncData.CODEC.collect(PacketCodecs.toList()),
          SyncPetsPayload::pets,
          SyncPetsPayload::new);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }
}
