package com.grahambartley.dogsunleashed.network.payload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
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
        PetLifeState.LIVING,
        false,
        1,
        2,
        0,
        DogFurStage.NATURAL.ordinal(),
        0.29f,
        3.5f,
        composition,
        RidingView.NONE,
        PetAwayStatus.HOME,
        false,
        DogBondStep.STRANGER,
        DogTraits.NONE);
  }
}
