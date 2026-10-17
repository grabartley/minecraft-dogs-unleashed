package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SetPetNamePayloadTest {

  private static final UUID ID = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");

  @Test
  @DisplayName("codec round-trips the payload")
  void codecRoundTrips() {
    final SetPetNamePayload payload = new SetPetNamePayload(ID, "Biscuit");
    final RegistryByteBuf buf = newBuf();
    SetPetNamePayload.CODEC.encode(buf, payload);
    assertEquals(payload, SetPetNamePayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("the UUID travels as two raw longs in sixteen bytes")
  void uuidTravelsAsSixteenRawBytes() {
    final RegistryByteBuf buf = newBuf();
    SetPetNamePayload.CODEC.encode(buf, new SetPetNamePayload(ID, "Biscuit"));

    assertLeadingUuid(buf, ID);
    assertEquals(1 + "Biscuit".length(), buf.readableBytes());
  }
}
