package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SledSprintPayloadTest {

  @ParameterizedTest(name = "held {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("the sprint key state survives the trip")
  void codecRoundTrips(final boolean held) {
    final RegistryByteBuf buf = newBuf();
    SledSprintPayload.CODEC.encode(buf, new SledSprintPayload(held));
    assertEquals(new SledSprintPayload(held), SledSprintPayload.CODEC.decode(buf));
    assertEquals(0, buf.readableBytes());
  }
}
