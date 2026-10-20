package com.grahambartley.dogsunleashed.render.statue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatueEngravingTest {

  private static final float PIXEL = 1.0f / 16.0f;
  private static final ToIntFunction<String> SIX_PER_CHAR = text -> text.length() * 6;

  @ParameterizedTest(name = "{0} text units wide is drawn {1} blocks wide")
  @CsvSource({"12, 0.2678571", "30, 0.6696429", "31, 0.6875", "60, 0.6875"})
  @DisplayName("short single lines keep full size and longer ones shrink to fit the 11 pixel panel")
  void singleLinesShrinkToFit(final int textWidth, final float drawnWidth) {
    assertEquals(
        drawnWidth,
        textWidth * StatueEngraving.scaleFor(textWidth, StatueEngraving.FULL_SCALE),
        1e-5);
  }

  @Test
  @DisplayName("full size letters are two and a half pixels tall")
  void fullSizeIsTwoAndAHalfPixels() {
    final StatueEngraving.Layout layout = StatueEngraving.layout("Rex", SIX_PER_CHAR);
    assertEquals(2.5f * PIXEL, 7 * layout.scale(), 1e-6);
  }

  static Stream<Arguments> layouts() {
    return Stream.of(
        Arguments.of("Max", List.of("Max")),
        Arguments.of("Bentley", List.of("Bentley")),
        Arguments.of("Longnamewithoutspaces", List.of("Longnamew...")),
        Arguments.of("Sir Barkalot the Third", List.of("Sir Barkalot", "the Third")),
        Arguments.of("Duke Fluffington", List.of("Duke", "Fluffington")),
        Arguments.of(
            "Bartholomewfluffington Pawsworthington", List.of("Bartholom...", "Pawsworth...")),
        Arguments.of("Abcdefghijklmnopqrstuvwxyzabcdef", List.of("Abcdefghi...")),
        Arguments.of("", List.of("")));
  }

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @MethodSource("layouts")
  @DisplayName("long names wrap at the middle space, and only what still cannot fit is trimmed")
  void longNamesWrapThenTrim(final String name, final List<String> lines) {
    assertEquals(lines, StatueEngraving.layout(name, SIX_PER_CHAR).lines());
  }

  @Test
  @DisplayName("two lines fit the four pixel panel with room above and below")
  void twoLinesFitThePanelHeight() {
    final StatueEngraving.Layout layout =
        StatueEngraving.layout("Sir Barkalot the Third", SIX_PER_CHAR);
    final float height = (7 + StatueEngraving.LINE_SPACING) * layout.scale();
    assertTrue(height <= 3.2f * PIXEL + 1e-6, "two lines are " + height / PIXEL + "px");
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(StatueMaterial.class)
  @DisplayName("every material engraves in an opaque colour")
  void colourIsOpaque(final StatueMaterial material) {
    assertEquals(0xFF, StatueEngraving.colourOf(material) >>> 24);
  }
}
