package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenNamingScreenPayload(
    UUID petId,
    UnleashedDogBreed breed,
    List<BreedShare> composition,
    boolean bornPuppy,
    String suggestedName)
    implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_naming_screen");
  public static final CustomPayload.Id<OpenNamingScreenPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, OpenNamingScreenPayload> CODEC =
      PacketCodec.of(OpenNamingScreenPayload::write, OpenNamingScreenPayload::read);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }

  private void write(final RegistryByteBuf buf) {
    buf.writeString(this.petId.toString());
    buf.writeString(this.breed.serializedId());
    BreedShareCodec.writeList(buf, this.composition);
    buf.writeBoolean(this.bornPuppy);
    buf.writeString(this.suggestedName);
  }

  private static OpenNamingScreenPayload read(final RegistryByteBuf buf) {
    return new OpenNamingScreenPayload(
        UUID.fromString(buf.readString()),
        UnleashedDogBreed.fromSerializedId(buf.readString()),
        BreedShareCodec.readList(buf),
        buf.readBoolean(),
        buf.readString());
  }
}
