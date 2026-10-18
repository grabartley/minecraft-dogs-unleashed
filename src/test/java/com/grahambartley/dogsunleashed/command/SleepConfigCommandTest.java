package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.SleepSettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SleepConfigCommandTest {

  @Test
  @DisplayName("help lists auto sleep and its range bounds")
  void helpLines() {
    final List<Text> lines = SleepConfigCommand.helpLines();
    assertEquals("command.dogs-unleashed.help.autosleep", key(lines.get(0)));
    assertArrayEquals(
        new Object[] {SleepSettings.RANGE_MIN, SleepSettings.RANGE_MAX}, args(lines.get(1)));
  }

  @Test
  @DisplayName("status shows each live sleep setting")
  void statusLines() {
    final List<Text> lines = SleepConfigCommand.statusLines(new SleepSettings(false, 48));
    assertArrayEquals(new Object[] {false}, args(lines.get(0)));
    assertArrayEquals(new Object[] {48}, args(lines.get(1)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
