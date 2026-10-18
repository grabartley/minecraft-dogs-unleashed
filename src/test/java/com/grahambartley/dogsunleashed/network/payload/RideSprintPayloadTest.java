package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RideSprintPayloadTest {

  @ParameterizedTest(name = "sprinting {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("codec round-trips the sprint flag")
  void codecRoundTrips(final boolean sprinting) {
    final RideSprintPayload payload = new RideSprintPayload(sprinting);
    final RegistryByteBuf buf = newBuf();
    RideSprintPayload.CODEC.encode(buf, payload);
    assertEquals(payload, RideSprintPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("the flag travels as a single byte")
  void flagIsOneByte() {
    final RegistryByteBuf buf = newBuf();
    RideSprintPayload.CODEC.encode(buf, new RideSprintPayload(true));
    assertEquals(1, buf.readableBytes());
  }
}
