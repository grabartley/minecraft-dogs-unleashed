package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import com.grahambartley.dogsunleashed.render.cross.CrossProportions.Pull;
import com.grahambartley.dogsunleashed.render.cross.CrossProportions.SlotSizes;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CrossProportionsTest {

  private static final float EPSILON = 1.0e-4f;
  private static final SlotSizes OWN = new SlotSizes(0.4f, 1.0f, 0.4f, 0.4f);
  private static final SlotSizes BIG_HEAD_SHORT_LEGS = new SlotSizes(0.5f, 1.0f, 0.32f, 0.4f);

  static Stream<Arguments> pulls() {
    return Stream.of(
        Arguments.of("no pull keeps every slot", List.of(), SlotScales.NONE),
        Arguments.of(
            "a full pull takes the other breed's proportions",
            List.of(new Pull(BIG_HEAD_SHORT_LEGS, 1.0f)),
            new SlotScales(1.25f, 1.0f, 0.8f, 1.0f)),
        Arguments.of(
            "a half pull lands halfway on a log scale",
            List.of(new Pull(BIG_HEAD_SHORT_LEGS, 0.5f)),
            new SlotScales(1.12f, 1.0f, 0.89f, 1.0f)),
        Arguments.of(
            "a smaller share pulls less",
            List.of(new Pull(BIG_HEAD_SHORT_LEGS, 0.25f)),
            new SlotScales(1.06f, 1.0f, 0.95f, 1.0f)),
        Arguments.of(
            "an extreme breed is clamped before it pulls",
            List.of(new Pull(new SlotSizes(4.0f, 0.1f, 0.4f, 0.4f), 1.0f)),
            new SlotScales(1.33f, 0.75f, 1.0f, 1.0f)),
        Arguments.of(
            "a slot the other rig lacks is not pulled",
            List.of(new Pull(new SlotSizes(0.0f, 1.0f, 0.0f, 0.0f), 1.0f)),
            SlotScales.NONE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pulls")
  @DisplayName("each slot scales toward the other breeds' proportions by their share")
  void slotsScaleTowardTheOtherBreeds(
      final String name, final List<Pull> pulls, final SlotScales expected) {
    final SlotScales scales = CrossProportions.scalesOf(OWN, pulls);

    assertEquals(expected.head(), scales.head(), EPSILON);
    assertEquals(expected.torso(), scales.torso(), EPSILON);
    assertEquals(expected.legs(), scales.legs(), EPSILON);
    assertEquals(expected.tail(), scales.tail(), EPSILON);
  }
}
