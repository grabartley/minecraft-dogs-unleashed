package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.assertLeadingUuid;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenCommandWheelPayloadTest {

  private static final Identifier BELL = Identifier.of("minecraft", "bell");

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of("typical", 42, 0, true, "Rex", "", List.of()),
        Arguments.of(
            "no bed",
            1,
            3,
            false,
            "Good Boy",
            "screen.dogs-unleashed.command_wheel.ride.breed",
            List.of(new WheelSubActionState(0, "a.label", BELL, ""))),
        Arguments.of(
            "unicode name with sub-actions",
            7,
            6,
            true,
            "小白",
            "screen.dogs-unleashed.command_wheel.ride.busy",
            List.of(
                new WheelSubActionState(2, "a.label", BELL, ""),
                new WheelSubActionState(5, "b.label", BELL, "some.refusal"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("codec round-trips its fields")
  void codecRoundTripsItsFields(
      final String label,
      final int entityId,
      final int commandId,
      final boolean hasBed,
      final String dogName,
      final String rideRefusalKey,
      final List<WheelSubActionState> subActions) {
    final OpenCommandWheelPayload payload =
        new OpenCommandWheelPayload(
            entityId, UUID.randomUUID(), commandId, hasBed, dogName, rideRefusalKey, subActions);
    final RegistryByteBuf buf = newBuf();
    OpenCommandWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenCommandWheelPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("a dog is rideable only when no refusal reason is sent")
  void rideableMeansNoRefusal() {
    assertTrue(
        new OpenCommandWheelPayload(1, UUID.randomUUID(), 0, false, "Rex", "", List.of())
            .rideable());
    assertFalse(
        new OpenCommandWheelPayload(1, UUID.randomUUID(), 0, false, "Rex", "some.key", List.of())
            .rideable());
  }

  @Test
  @DisplayName("the dog UUID travels as two raw longs after the entity id")
  void dogUuidTravelsAsSixteenRawBytes() {
    final UUID dogId = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
    final RegistryByteBuf buf = newBuf();
    OpenCommandWheelPayload.CODEC.encode(
        buf, new OpenCommandWheelPayload(42, dogId, 0, true, "Rex", "", List.of()));

    assertEquals(42, buf.readVarInt());
    assertLeadingUuid(buf, dogId);
  }

  @Test
  @DisplayName("a sub-action list longer than the registry cap is refused on decode")
  void oversizedSubActionListIsRefused() {
    final List<WheelSubActionState> tooMany =
        IntStream.rangeClosed(0, WheelSubActionState.MAX_PER_WHEEL)
            .mapToObj(id -> new WheelSubActionState(id, "a.label", BELL, ""))
            .toList();
    final RegistryByteBuf buf = newBuf();
    buf.writeVarInt(1);
    buf.writeUuid(UUID.randomUUID());
    buf.writeVarInt(0);
    buf.writeBoolean(true);
    buf.writeString("Rex");
    buf.writeString("");
    buf.writeVarInt(tooMany.size());
    tooMany.forEach(state -> WheelSubActionState.CODEC.encode(buf, state));

    assertThrows(DecoderException.class, () -> OpenCommandWheelPayload.CODEC.decode(buf));
  }
}
