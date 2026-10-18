package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SelectWheelSubActionPayloadTest {

  @ParameterizedTest(name = "sub-action {0}")
  @ValueSource(ints = {0, 1, 15, 300})
  @DisplayName("codec round-trips the dog and sub-action id")
  void codecRoundTrips(final int subActionId) {
    final SelectWheelSubActionPayload payload =
        new SelectWheelSubActionPayload(UUID.randomUUID(), subActionId);
    final RegistryByteBuf buf = newBuf();
    SelectWheelSubActionPayload.CODEC.encode(buf, payload);
    assertEquals(payload, SelectWheelSubActionPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("the dog UUID travels as two raw longs before the sub-action id")
  void dogUuidLeads() {
    final UUID dogId = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
    final RegistryByteBuf buf = newBuf();
    SelectWheelSubActionPayload.CODEC.encode(buf, new SelectWheelSubActionPayload(dogId, 4));

    assertLeadingUuid(buf, dogId);
    assertEquals(4, buf.readVarInt());
  }
}
