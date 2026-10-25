package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.OWNER_NAME_MAX_LENGTH;
import static com.grahambartley.dogsunleashed.network.PacketLimits.PARENTS_MAX_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import org.jetbrains.annotations.Nullable;

public record ConnectionDogSyncData(
    PetSyncData pet,
    String ownerId,
    String ownerName,
    List<GeneSyncData> genes,
    boolean carriedGenesRevealed,
    List<String> parentIds) {

  public ConnectionDogSyncData {
    genes = List.copyOf(genes);
    parentIds = List.copyOf(parentIds);
  }

  public static List<String> parentIdsOf(
      final @Nullable UUID parentAId, final @Nullable UUID parentBId) {
    return Stream.of(parentAId, parentBId).filter(Objects::nonNull).map(UUID::toString).toList();
  }

  void write(final RegistryByteBuf buf) {
    this.pet.write(buf);
    buf.writeString(this.ownerId);
    buf.writeString(this.ownerName, OWNER_NAME_MAX_LENGTH);
    GeneSyncData.writeList(buf, this.genes);
    buf.writeBoolean(this.carriedGenesRevealed);
    buf.writeCollection(
        this.parentIds.subList(0, Math.min(this.parentIds.size(), PARENTS_MAX_SIZE)),
        PacketByteBuf::writeString);
  }

  static ConnectionDogSyncData read(final RegistryByteBuf buf) {
    return new ConnectionDogSyncData(
        PetSyncData.read(buf),
        buf.readString(),
        buf.readString(OWNER_NAME_MAX_LENGTH),
        GeneSyncData.readList(buf),
        buf.readBoolean(),
        buf.readCollection(
            PacketByteBuf.getMaxValidator(ArrayList::new, PARENTS_MAX_SIZE),
            PacketByteBuf::readString));
  }
}
