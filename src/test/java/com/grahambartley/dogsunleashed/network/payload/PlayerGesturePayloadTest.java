package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerGesturePayloadTest {

  @ParameterizedTest(name = "entity {0}, {1}, {2}")
  @CsvSource({
    "0, MAIN_HAND, WHISTLE",
    "1, OFF_HAND, WHISTLE",
    "4000000, MAIN_HAND, PAT",
    "2147483647, OFF_HAND, PAT"
  })
  @DisplayName("the gesture, the player and the hand survive the trip")
  void codecRoundTripsTheGesture(final int entityId, final Hand hand, final PlayerGesture gesture) {
    final PlayerGesturePayload payload = PlayerGesturePayload.of(entityId, hand, gesture);
    final RegistryByteBuf buf = newBuf();
    PlayerGesturePayload.CODEC.encode(buf, payload);
    final PlayerGesturePayload decoded = PlayerGesturePayload.CODEC.decode(buf);
    assertEquals(payload, decoded);
    assertEquals(hand, decoded.hand());
    assertEquals(gesture, decoded.gesture());
    assertEquals(0, buf.readableBytes());
  }
}
