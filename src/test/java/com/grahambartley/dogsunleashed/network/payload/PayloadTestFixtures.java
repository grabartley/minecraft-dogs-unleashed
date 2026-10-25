package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.TestRegistries.REGISTRIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;

final class PayloadTestFixtures {

  private PayloadTestFixtures() {}

  static void assertLeadingUuid(final RegistryByteBuf buf, final UUID expected) {
    assertEquals(expected.getMostSignificantBits(), buf.readLong());
    assertEquals(expected.getLeastSignificantBits(), buf.readLong());
  }

  static RegistryByteBuf newBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), null);
  }

  static RegistryByteBuf newRegistryBuf() {
    return new RegistryByteBuf(Unpooled.buffer(), REGISTRIES);
  }

  static PetSyncData samplePet(final UnleashedDogBreed breed, final String name) {
    return samplePet(breed, name, List.of());
  }

  static PetSyncData samplePet(
      final UnleashedDogBreed breed, final String name, final List<BreedShare> composition) {
    return PetSyncDataFixture.pet()
        .breed(breed)
        .name(name)
        .composition(composition)
        .variants(2, 0)
        .build();
  }
}
