package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.OWNER_NAME_MAX_LENGTH;

import net.minecraft.network.RegistryByteBuf;

public record ConnectionDogSyncData(PetSyncData pet, String ownerId, String ownerName) {

  void write(final RegistryByteBuf buf) {
    this.pet.write(buf);
    buf.writeString(this.ownerId);
    buf.writeString(this.ownerName, OWNER_NAME_MAX_LENGTH);
  }

  static ConnectionDogSyncData read(final RegistryByteBuf buf) {
    return new ConnectionDogSyncData(
        PetSyncData.read(buf), buf.readString(), buf.readString(OWNER_NAME_MAX_LENGTH));
  }
}
