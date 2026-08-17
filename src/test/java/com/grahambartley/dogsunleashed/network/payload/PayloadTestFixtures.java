package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;

final class PayloadTestFixtures {

  private PayloadTestFixtures() {}

  // These payload codecs only use primitive and string buf operations, so no registry lookup is
  // needed and a null registry is safe here.
  static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  static PetSyncData samplePet(final UnleashedDogBreed breed, final String name) {
    return samplePet(breed, name, List.of());
  }

  static PetSyncData samplePet(
      final UnleashedDogBreed breed, final String name, final List<BreedShare> composition) {
    return new PetSyncData(
        UUID.randomUUID().toString(),
        breed,
        name,
        12.5f,
        25.0f,
        10,
        64,
        -20,
        "minecraft:overworld",
        true,
        false,
        1,
        2,
        0,
        0.29f,
        3.5f,
        composition);
  }
}
