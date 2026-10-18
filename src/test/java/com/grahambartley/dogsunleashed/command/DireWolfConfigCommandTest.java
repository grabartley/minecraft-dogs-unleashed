package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.config.DireWolfPackSettings;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DireWolfConfigCommandTest {

  @Test
  @DisplayName("status shows the live dire wolf settings")
  void statusShowsTheSettings() {
    final List<Text> lines =
        DireWolfConfigCommand.statusLines(
            new DireWolfSettings(
                150,
                9,
                6,
                12,
                new DireWolfPackSettings(false, true),
                new DireWolfStaminaSettings(false, 150, 60)));

    assertArrayEquals(new Object[] {150}, args(lines.get(0)));
    assertArrayEquals(new Object[] {9}, args(lines.get(1)));
    assertArrayEquals(new Object[] {6, 12}, args(lines.get(2)));
    assertArrayEquals(new Object[] {false, true}, args(lines.get(3)));
    assertArrayEquals(new Object[] {false, 150, 60}, args(lines.get(4)));
  }

  @Test
  @DisplayName("help lists every dire wolf setting with its range")
  void helpListsEachSettingWithItsRange() {
    final List<Text> lines = DireWolfConfigCommand.helpLines();

    assertEquals(9, lines.size());
    assertArrayEquals(
        new Object[] {DireWolfSettings.GROWTH_RATE_MIN, DireWolfSettings.GROWTH_RATE_MAX},
        args(lines.get(0)));
    assertArrayEquals(
        new Object[] {DireWolfSettings.BATTLES_MIN, DireWolfSettings.BATTLES_MAX},
        args(lines.get(1)));
    assertArrayEquals(
        new Object[] {DireWolfSettings.PACK_MIN, DireWolfSettings.PACK_MAX}, args(lines.get(2)));
    assertArrayEquals(
        new Object[] {DireWolfSettings.PACK_MIN, DireWolfSettings.PACK_MAX}, args(lines.get(3)));
    for (final int toggle : new int[] {4, 5, 6}) {
      assertArrayEquals(new Object[] {}, args(lines.get(toggle)));
    }
    for (final int percent : new int[] {7, 8}) {
      assertArrayEquals(
          new Object[] {DireWolfStaminaSettings.PERCENT_MIN, DireWolfStaminaSettings.PERCENT_MAX},
          args(lines.get(percent)));
    }
  }

  @Test
  @DisplayName("a smallest pack above the largest is refused, naming both sizes")
  void packOrderRefusesASmallestAboveTheLargest() {
    final Text refusal = DireWolfConfigCommand.packOrder(12, 10).orElseThrow();
    assertArrayEquals(new Object[] {12, 10}, args(refusal));
    assertTrue(DireWolfConfigCommand.packOrder(10, 10).isEmpty());
    assertTrue(DireWolfConfigCommand.packOrder(5, 10).isEmpty());
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
