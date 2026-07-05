package com.grahambartley.dogsunleashed.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PacketLimitsTest {

  static Stream<Arguments> packetLimits() {
    return Stream.of(
        Arguments.of("SET_PET_NAME_NAME_MAX_LENGTH", PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH, 32),
        Arguments.of(
            "REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH",
            PacketLimits.REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH,
            64));
  }

  @ParameterizedTest(name = "{0} = {2}")
  @MethodSource("packetLimits")
  @DisplayName("packet field length caps stay stable so older clients cannot exceed them")
  void packetLengthCapsHoldDocumentedValues(
      final String name, final int actual, final int expected) {
    assertEquals(expected, actual, name);
  }
}
