package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenCommandWheelPayloadTest {

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of("typical", 42, 0, true, "Rex", ""),
        Arguments.of(
            "no bed", 1, 3, false, "Good Boy", "screen.dogs-unleashed.command_wheel.ride.breed"),
        Arguments.of(
            "unicode name", 7, 6, true, "小白", "screen.dogs-unleashed.command_wheel.ride.busy"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final int entityId,
      final int commandId,
      final boolean hasBed,
      final String dogName,
      final String rideRefusalKey) {
    final OpenCommandWheelPayload payload =
        new OpenCommandWheelPayload(
            entityId, UUID.randomUUID(), commandId, hasBed, dogName, rideRefusalKey);
    final RegistryByteBuf buf = newBuf();
    OpenCommandWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenCommandWheelPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("a dog is rideable only when no refusal reason is sent")
  void rideableMeansNoRefusal() {
    assertTrue(new OpenCommandWheelPayload(1, UUID.randomUUID(), 0, false, "Rex", "").rideable());
    assertFalse(
        new OpenCommandWheelPayload(1, UUID.randomUUID(), 0, false, "Rex", "some.key").rideable());
  }

  @Test
  @DisplayName("the dog UUID travels as two raw longs after the entity id")
  void dogUuidTravelsAsSixteenRawBytes() {
    final UUID dogId = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
    final RegistryByteBuf buf = newBuf();
    OpenCommandWheelPayload.CODEC.encode(
        buf, new OpenCommandWheelPayload(42, dogId, 0, true, "Rex", ""));

    assertEquals(42, buf.readVarInt());
    assertLeadingUuid(buf, dogId);
  }
}
