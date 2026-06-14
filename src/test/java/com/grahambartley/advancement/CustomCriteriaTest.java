package com.grahambartley.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CustomCriteriaTest {

  static Stream<Arguments> criteria() {
    return Stream.of(
        Arguments.of("HuskyHowled", HuskyHowledCriterion.ID, "dogs-unleashed:husky_howled"),
        Arguments.of("FetchReturned", FetchReturnedCriterion.ID, "dogs-unleashed:fetch_returned"),
        Arguments.of(
            "DogSleptInBed", DogSleptInBedCriterion.ID, "dogs-unleashed:dog_slept_in_bed"));
  }

  @ParameterizedTest(name = "{0} ID = {2}")
  @MethodSource("criteria")
  @DisplayName("each custom criterion exposes its expected identifier")
  void exposesExpectedIdentifier(final String label, final Identifier id, final String expected) {
    final String[] parts = expected.split(":");
    assertEquals(expected, id.toString(), label);
    assertEquals(parts[0], id.getNamespace(), label);
    assertEquals(parts[1], id.getPath(), label);
  }
}
