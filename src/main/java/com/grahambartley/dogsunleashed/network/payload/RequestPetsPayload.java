package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestPetsPayload(
    UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, String searchQuery)
    implements CustomPayload {

  public static final Identifier PACKET_ID = Identifier.of(DogsUnleashed.MOD_ID, "request_pets");
  public static final CustomPayload.Id<RequestPetsPayload> ID = new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, RequestPetsPayload> CODEC =
      PacketCodec.of(RequestPetsPayload::write, RequestPetsPayload::read);

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
    buf.writeString(this.searchQuery, REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH);
  }

  private static RequestPetsPayload read(final RegistryByteBuf buf) {
    final UnleashedDogBreed breedFilter =
        buf.readBoolean() ? UnleashedDogBreed.fromSerializedId(buf.readString()) : null;
    return new RequestPetsPayload(
        breedFilter,
        PetAliveFilter.fromSerializedName(buf.readString()),
        buf.readString(REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH));
  }
}
