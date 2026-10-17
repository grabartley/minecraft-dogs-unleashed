package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenNamingScreenPayloadTest {

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of("tamed pure breed", UnleashedDogBreed.HUSKY, List.of(), false, "Koda"),
        Arguments.of(
            "bred mix puppy",
            UnleashedDogBreed.CROSS_BREED,
            List.of(
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f)),
            true,
            "Biscuit"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final UnleashedDogBreed breed,
      final List<BreedShare> composition,
      final boolean bornPuppy,
      final String suggestedName) {
    final OpenNamingScreenPayload payload =
        new OpenNamingScreenPayload(
            UUID.randomUUID(), breed, composition, bornPuppy, suggestedName);
    final RegistryByteBuf buf = newBuf();
    OpenNamingScreenPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenNamingScreenPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("the pet UUID leads the payload as two raw longs")
  void petUuidTravelsAsSixteenRawBytes() {
    final UUID petId = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
    final RegistryByteBuf buf = newBuf();
    OpenNamingScreenPayload.CODEC.encode(
        buf, new OpenNamingScreenPayload(petId, UnleashedDogBreed.HUSKY, List.of(), false, "Koda"));

    assertLeadingUuid(buf, petId);
    assertEquals(UnleashedDogBreed.HUSKY.serializedId(), buf.readString());
  }
}
