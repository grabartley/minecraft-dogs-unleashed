package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.FurSettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FurConfigCommandTest {

  @Test
  @DisplayName("help lists fur growth and both percent bounds")
  void helpLines() {
    final List<Text> lines = FurConfigCommand.helpLines();
    assertEquals("command.dogs-unleashed.help.furgrowth", key(lines.get(0)));
    for (final int line : new int[] {1, 2}) {
      assertArrayEquals(
          new Object[] {FurSettings.PERCENT_MIN, FurSettings.PERCENT_MAX}, args(lines.get(line)));
    }
  }

  @Test
  @DisplayName("status shows each live fur setting")
  void statusLines() {
    final List<Text> lines = FurConfigCommand.statusLines(new FurSettings(false, 250, 40));
    assertArrayEquals(new Object[] {false}, args(lines.get(0)));
    assertArrayEquals(new Object[] {250}, args(lines.get(1)));
    assertArrayEquals(new Object[] {40}, args(lines.get(2)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
