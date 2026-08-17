package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SelectWheelActionPayloadTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogWheelAction.class)
  @DisplayName("codec round-trips every wheel action")
  void codecRoundTripsEveryWheelAction(final DogWheelAction action) {
    final SelectWheelActionPayload payload =
        new SelectWheelActionPayload(UUID.randomUUID(), action.id());
    final RegistryByteBuf buf = newBuf();
    SelectWheelActionPayload.CODEC.encode(buf, payload);
    assertEquals(payload, SelectWheelActionPayload.CODEC.decode(buf));
  }
}
