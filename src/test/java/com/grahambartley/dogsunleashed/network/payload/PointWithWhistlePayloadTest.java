package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PointWithWhistlePayloadTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  void codecRoundTripsEveryTarget(final String label, final PointWithWhistlePayload payload) {
    final RegistryByteBuf buf = newBuf();
    PointWithWhistlePayload.CODEC.encode(buf, payload);
    assertEquals(payload, PointWithWhistlePayload.CODEC.decode(buf));
    assertEquals(0, buf.readableBytes());
  }

  static Stream<Arguments> payloads() {
    return Stream.of(
        Arguments.of(
            "block in main hand",
            PointWithWhistlePayload.atBlock(
                Hand.MAIN_HAND, new BlockPos(12, -40, 3), Direction.NORTH)),
        Arguments.of(
            "block in off hand",
            PointWithWhistlePayload.atBlock(
                Hand.OFF_HAND, new BlockPos(-30000, 319, 29999), Direction.DOWN)),
        Arguments.of("entity in main hand", PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, 0)),
        Arguments.of(
            "entity in off hand", PointWithWhistlePayload.atEntity(Hand.OFF_HAND, 4_000_000)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Hand.class)
  void handSurvivesTheTrip(final Hand hand) {
    final RegistryByteBuf buf = newBuf();
    PointWithWhistlePayload.CODEC.encode(buf, PointWithWhistlePayload.atEntity(hand, 7));
    assertEquals(hand, PointWithWhistlePayload.CODEC.decode(buf).hand());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Direction.class)
  void aimedFaceSurvivesTheTrip(final Direction side) {
    final RegistryByteBuf buf = newBuf();
    PointWithWhistlePayload.CODEC.encode(
        buf, PointWithWhistlePayload.atBlock(Hand.MAIN_HAND, BlockPos.ORIGIN, side));
    assertEquals(side, PointWithWhistlePayload.CODEC.decode(buf).side());
  }

  @ParameterizedTest(name = "entity={0}")
  @MethodSource("targetKinds")
  void blockAndEntityTargetsAreTold(final PointWithWhistlePayload payload, final boolean entity) {
    assertEquals(entity, payload.pointsAtEntity());
  }

  static Stream<Arguments> targetKinds() {
    return Stream.of(
        Arguments.of(
            PointWithWhistlePayload.atBlock(Hand.MAIN_HAND, BlockPos.ORIGIN, Direction.UP), false),
        Arguments.of(PointWithWhistlePayload.atEntity(Hand.MAIN_HAND, 0), true));
  }
}
