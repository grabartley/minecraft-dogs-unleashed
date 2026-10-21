package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.List;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrossCoatMaskTest {

  private static final CrossCoatMask MASK =
      CrossCoatMask.of(
          8,
          8,
          List.of(
              new SlotRegion("head", Direction.NORTH, 1, 1, 3, 2),
              new SlotRegion("tail", Direction.UP, 6, 6, 4, 4)),
          List.of(new Texel(2, 1), new Texel(20, 20)));

  @ParameterizedTest(name = "({0}, {1}) tints {2}")
  @CsvSource({
    "1, 1, true",
    "3, 2, true",
    "4, 1, false",
    "1, 3, false",
    "0, 0, false",
    "2, 1, false",
    "7, 7, true",
    "8, 8, false",
    "-1, 0, false"
  })
  @DisplayName("only texels inside a coat region and away from the pupils are tinted")
  void tintsOnlyCoatTexels(final int x, final int y, final boolean tints) {
    assertEquals(tints, MASK.tints(new Texel(x, y)));
  }
}
