package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenDogInspectPayloadTest {

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of(
            "wild dog",
            samplePet(UnleashedDogBreed.HUSKY, ""),
            false,
            "",
            List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f))),
        Arguments.of(
            "tamed dog with owner",
            samplePet(UnleashedDogBreed.BEAGLE, "Rex"),
            true,
            "Steve",
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.25f))),
        Arguments.of(
            "tamed dog with unknown owner",
            samplePet(UnleashedDogBreed.SHIBA_INU, "小白"),
            true,
            "",
            List.of(new BreedShare(UnleashedDogBreed.SHIBA_INU, 1.0f))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final PetSyncData pet,
      final boolean tamed,
      final String ownerName,
      final List<BreedShare> composition) {
    final OpenDogInspectPayload payload =
        new OpenDogInspectPayload(pet, tamed, ownerName, composition);
    final RegistryByteBuf buf = newBuf();
    OpenDogInspectPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenDogInspectPayload.CODEC.decode(buf));
  }
}
