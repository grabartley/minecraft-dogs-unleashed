package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.BREED_COMPOSITION_MAX_SIZE;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;

final class BreedShareCodec {

  private BreedShareCodec() {}

  static void writeList(final RegistryByteBuf buf, final List<BreedShare> composition) {
    buf.writeVarInt(Math.min(composition.size(), BREED_COMPOSITION_MAX_SIZE));
    for (final BreedShare share :
        composition.subList(0, Math.min(composition.size(), BREED_COMPOSITION_MAX_SIZE))) {
      buf.writeString(share.breed().serializedId());
      buf.writeFloat(share.share());
    }
  }

  static List<BreedShare> readList(final RegistryByteBuf buf) {
    final int count = Math.min(buf.readVarInt(), BREED_COMPOSITION_MAX_SIZE);
    final List<BreedShare> composition = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      composition.add(
          new BreedShare(UnleashedDogBreed.fromSerializedId(buf.readString()), buf.readFloat()));
    }
    return composition;
  }
}
