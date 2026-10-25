package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Hand;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WhistleBlowPayloadTest {

  @ParameterizedTest(name = "entity {0}, {1}")
  @CsvSource({"0, MAIN_HAND", "1, OFF_HAND", "4000000, MAIN_HAND", "2147483647, OFF_HAND"})
  void codecRoundTripsTheBlower(final int entityId, final Hand hand) {
    final WhistleBlowPayload payload = WhistleBlowPayload.of(entityId, hand);
    final RegistryByteBuf buf = newBuf();
    WhistleBlowPayload.CODEC.encode(buf, payload);
    assertEquals(payload, WhistleBlowPayload.CODEC.decode(buf));
    assertEquals(0, buf.readableBytes());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Hand.class)
  void handSurvivesTheTrip(final Hand hand) {
    final RegistryByteBuf buf = newBuf();
    WhistleBlowPayload.CODEC.encode(buf, WhistleBlowPayload.of(7, hand));
    final WhistleBlowPayload decoded = WhistleBlowPayload.CODEC.decode(buf);
    assertEquals(hand, decoded.hand());
    assertEquals(7, decoded.entityId());
  }
}
