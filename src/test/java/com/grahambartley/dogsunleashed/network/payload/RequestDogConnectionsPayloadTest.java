package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestDogConnectionsPayloadTest {

  private static final UUID ID = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");

  @Test
  @DisplayName("codec round-trips the payload")
  void codecRoundTrips() {
    final RequestDogConnectionsPayload payload = new RequestDogConnectionsPayload(ID);
    final RegistryByteBuf buf = newBuf();
    RequestDogConnectionsPayload.CODEC.encode(buf, payload);
    assertEquals(payload, RequestDogConnectionsPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("the UUID travels as two raw longs in sixteen bytes")
  void uuidTravelsAsSixteenRawBytes() {
    final RegistryByteBuf buf = newBuf();
    RequestDogConnectionsPayload.CODEC.encode(buf, new RequestDogConnectionsPayload(ID));

    assertEquals(ID.getMostSignificantBits(), buf.readLong());
    assertEquals(ID.getLeastSignificantBits(), buf.readLong());
    assertEquals(0, buf.readableBytes());
  }
}
