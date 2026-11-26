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

  @ParameterizedTest(name = "entity {0}, {1}, {2}, target {3}")
  @CsvSource({
    "0, MAIN_HAND, WHISTLE, -1",
    "1, OFF_HAND, WHISTLE, -1",
    "4000000, MAIN_HAND, PAT, 12",
    "2147483647, OFF_HAND, PAT, 2147483647"
  })
  @DisplayName("the gesture, the player, the hand and the target survive the trip")
  void codecRoundTripsTheGesture(
      final int entityId, final Hand hand, final PlayerGesture gesture, final int target) {
    final PlayerGesturePayload payload = PlayerGesturePayload.of(entityId, hand, gesture, target);
    final RegistryByteBuf buf = newBuf();
    PlayerGesturePayload.CODEC.encode(buf, payload);
    final PlayerGesturePayload decoded = PlayerGesturePayload.CODEC.decode(buf);
    assertEquals(payload, decoded);
    assertEquals(hand, decoded.hand());
    assertEquals(gesture, decoded.gesture());
    assertEquals(target, decoded.targetEntityId());
    assertEquals(0, buf.readableBytes());
  }
}
