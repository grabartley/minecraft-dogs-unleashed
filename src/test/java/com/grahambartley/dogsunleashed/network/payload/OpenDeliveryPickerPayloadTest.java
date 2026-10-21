package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.dogsunleashed.network.PacketLimits;
import com.grahambartley.dogsunleashed.network.payload.OpenDeliveryPickerPayload.Recipient;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OpenDeliveryPickerPayloadTest {

  private static List<Recipient> recipients(final int count) {
    return IntStream.range(0, count)
        .mapToObj(index -> new Recipient(UUID.randomUUID(), "Player" + index))
        .toList();
  }

  @ParameterizedTest(name = "{0} recipients")
  @ValueSource(ints = {0, 1, 7, PacketLimits.DELIVERY_RECIPIENTS_MAX_SIZE})
  @DisplayName("codec round-trips the dog and every recipient in order")
  void codecRoundTrips(final int count) {
    final OpenDeliveryPickerPayload payload =
        new OpenDeliveryPickerPayload(UUID.randomUUID(), "Balto", recipients(count));
    final RegistryByteBuf buf = newBuf();
    OpenDeliveryPickerPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenDeliveryPickerPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("a list over the limit is refused rather than sent")
  void refusesTooManyRecipients() {
    final OpenDeliveryPickerPayload payload =
        new OpenDeliveryPickerPayload(
            UUID.randomUUID(), "Balto", recipients(PacketLimits.DELIVERY_RECIPIENTS_MAX_SIZE + 1));
    assertThrows(
        RuntimeException.class, () -> OpenDeliveryPickerPayload.CODEC.encode(newBuf(), payload));
  }
}
