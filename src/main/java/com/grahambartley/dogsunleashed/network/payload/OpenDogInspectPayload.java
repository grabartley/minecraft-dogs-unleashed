package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.OWNER_NAME_MAX_LENGTH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenDogInspectPayload(
    PetSyncData pet, boolean tamed, String ownerName, List<BreedShare> composition)
    implements CustomPayload {

  public static final Identifier PACKET_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_dog_inspect");
  public static final CustomPayload.Id<OpenDogInspectPayload> ID =
      new CustomPayload.Id<>(PACKET_ID);
  public static final PacketCodec<RegistryByteBuf, OpenDogInspectPayload> CODEC =
      PacketCodec.of(OpenDogInspectPayload::write, OpenDogInspectPayload::read);

  @Override
  public CustomPayload.Id<? extends CustomPayload> getId() {
    return ID;
  }

  private void write(final RegistryByteBuf buf) {
    this.pet.write(buf);
    buf.writeBoolean(this.tamed);
    buf.writeString(this.ownerName, OWNER_NAME_MAX_LENGTH);
    BreedShareCodec.writeList(buf, this.composition);
  }

  private static OpenDogInspectPayload read(final RegistryByteBuf buf) {
    return new OpenDogInspectPayload(
        PetSyncData.read(buf),
        buf.readBoolean(),
        buf.readString(OWNER_NAME_MAX_LENGTH),
        BreedShareCodec.readList(buf));
  }
}
