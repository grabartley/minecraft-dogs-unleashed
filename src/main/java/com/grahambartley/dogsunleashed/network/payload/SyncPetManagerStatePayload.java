package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPetManagerStatePayload(
    UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, List<PetSyncData> pets)
    implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_pet_manager_state");
  public static final CustomPayload.Id<SyncPetManagerStatePayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, SyncPetManagerStatePayload> CODEC =
      PacketCodec.of(SyncPetManagerStatePayload::write, SyncPetManagerStatePayload::read);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }

  private void write(final RegistryByteBuf buf) {
    buf.writeBoolean(this.breedFilter != null);
    if (this.breedFilter != null) {
      buf.writeString(this.breedFilter.serializedId());
    }
    buf.writeString(this.aliveFilter.serializedName());
    buf.writeInt(this.pets.size());
    for (final PetSyncData pet : this.pets) {
      pet.write(buf);
    }
  }

  private static SyncPetManagerStatePayload read(final RegistryByteBuf buf) {
    final UnleashedDogBreed breedFilter =
        buf.readBoolean() ? UnleashedDogBreed.fromSerializedId(buf.readString()) : null;
    final PetAliveFilter aliveFilter = PetAliveFilter.fromSerializedName(buf.readString());
    final int petCount = buf.readInt();
    final List<PetSyncData> pets = new ArrayList<>(petCount);
    for (int i = 0; i < petCount; i++) {
      pets.add(PetSyncData.read(buf));
    }
    return new SyncPetManagerStatePayload(breedFilter, aliveFilter, pets);
  }
}
