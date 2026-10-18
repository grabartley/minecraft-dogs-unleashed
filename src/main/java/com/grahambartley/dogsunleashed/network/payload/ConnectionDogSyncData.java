package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.OWNER_NAME_MAX_LENGTH;

import java.util.List;
import net.minecraft.network.RegistryByteBuf;

public record ConnectionDogSyncData(
    PetSyncData pet,
    String ownerId,
    String ownerName,
    List<GeneSyncData> genes,
    boolean carriedGenesRevealed) {

  public ConnectionDogSyncData {
    genes = List.copyOf(genes);
  }

  void write(final RegistryByteBuf buf) {
    this.pet.write(buf);
    buf.writeString(this.ownerId);
    buf.writeString(this.ownerName, OWNER_NAME_MAX_LENGTH);
    GeneSyncData.writeList(buf, this.genes);
    buf.writeBoolean(this.carriedGenesRevealed);
  }

  static ConnectionDogSyncData read(final RegistryByteBuf buf) {
    return new ConnectionDogSyncData(
        PetSyncData.read(buf),
        buf.readString(),
        buf.readString(OWNER_NAME_MAX_LENGTH),
        GeneSyncData.readList(buf),
        buf.readBoolean());
  }
}
