package com.grahambartley.dogsunleashed.network.payload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.TestRegistries;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OpenDogInspectPayloadTest {

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of(
            "wild dog",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.HUSKY).name("").build(),
            false,
            "",
            List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f)),
            WildTrustView.NONE),
        Arguments.of(
            "wild dire wolf pup",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.DIRE_WOLF).name("").build(),
            false,
            "",
            List.of(new BreedShare(UnleashedDogBreed.DIRE_WOLF, 1.0f)),
            WildTrustView.building(45)),
        Arguments.of(
            "wild dire wolf adult",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.DIRE_WOLF).name("").build(),
            false,
            "",
            List.of(new BreedShare(UnleashedDogBreed.DIRE_WOLF, 1.0f)),
            WildTrustView.TOO_WILD),
        Arguments.of(
            "tamed dog with owner",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.BEAGLE).name("Rex").build(),
            true,
            "Steve",
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.25f)),
            WildTrustView.NONE),
        Arguments.of(
            "tamed dog with unknown owner",
            PetSyncDataFixture.pet().breed(UnleashedDogBreed.SHIBA_INU).name("小白").build(),
            true,
            "",
            List.of(new BreedShare(UnleashedDogBreed.SHIBA_INU, 1.0f)),
            WildTrustView.NONE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final PetSyncData pet,
      final boolean tamed,
      final String ownerName,
      final List<BreedShare> composition,
      final WildTrustView trust) {
    final OpenDogInspectPayload payload =
        new OpenDogInspectPayload(pet, tamed, ownerName, composition, trust);
    final RegistryByteBuf buf = TestRegistries.buf();
    OpenDogInspectPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenDogInspectPayload.CODEC.decode(buf));
  }
}
