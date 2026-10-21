package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliverDogPayloadTest {

  @ParameterizedTest(name = "{0} to {1}")
  @CsvSource({
    "0f8fad5b-d9cb-469f-a165-70867728950e, 7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "00000000-0000-0000-0000-000000000000, ffffffff-ffff-ffff-ffff-ffffffffffff"
  })
  @DisplayName("codec round-trips the dog then the recipient")
  void codecRoundTrips(final UUID dogId, final UUID recipientId) {
    final DeliverDogPayload payload = new DeliverDogPayload(dogId, recipientId);
    final RegistryByteBuf buf = newBuf();
    DeliverDogPayload.CODEC.encode(buf, payload);
    assertEquals(payload, DeliverDogPayload.CODEC.decode(buf));

    final RegistryByteBuf order = newBuf();
    DeliverDogPayload.CODEC.encode(order, payload);
    assertLeadingUuid(order, dogId);
    assertLeadingUuid(order, recipientId);
  }
}
