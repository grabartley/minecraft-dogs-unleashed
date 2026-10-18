package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WheelSubActionStateTest {

  private static final Identifier BELL = Identifier.of("minecraft", "bell");

  static Stream<Arguments> states() {
    return Stream.of(
        Arguments.of(new WheelSubActionState(0, "command.trick.beg", BELL, ""), true),
        Arguments.of(
            new WheelSubActionState(
                7, "command.trick.roll_over", BELL, "screen.dogs-unleashed.some.refusal"),
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("states")
  @DisplayName("codec round-trips the id, label, icon and refusal reason")
  void codecRoundTrips(final WheelSubActionState state, final boolean available) {
    final RegistryByteBuf buf = newBuf();
    WheelSubActionState.CODEC.encode(buf, state);
    assertEquals(state, WheelSubActionState.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0} available={1}")
  @MethodSource("states")
  @DisplayName("an action is available only when no refusal reason is sent")
  void availableMeansNoRefusal(final WheelSubActionState state, final boolean available) {
    assertEquals(available, state.available());
  }
}
