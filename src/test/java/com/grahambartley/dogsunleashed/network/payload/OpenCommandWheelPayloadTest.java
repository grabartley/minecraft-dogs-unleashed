package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenCommandWheelPayloadTest {

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of("typical", 42, 0, true, "Rex"),
        Arguments.of("no bed", 1, 3, false, "Good Boy"),
        Arguments.of("unicode name", 7, 6, true, "小白"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final int entityId,
      final int commandId,
      final boolean hasBed,
      final String dogName) {
    final OpenCommandWheelPayload payload =
        new OpenCommandWheelPayload(entityId, UUID.randomUUID(), commandId, hasBed, dogName);
    final RegistryByteBuf buf = newBuf();
    OpenCommandWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenCommandWheelPayload.CODEC.decode(buf));
  }
}
