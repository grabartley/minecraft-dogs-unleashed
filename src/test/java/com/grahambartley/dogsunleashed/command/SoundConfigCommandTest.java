package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.grahambartley.dogsunleashed.config.SoundSettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SoundConfigCommandTest {

  @Test
  @DisplayName("help lists both volumes with their bounds")
  void helpLines() {
    for (final Text line : SoundConfigCommand.helpLines()) {
      assertArrayEquals(
          new Object[] {SoundSettings.VOLUME_MIN, SoundSettings.VOLUME_MAX}, args(line));
    }
  }

  @Test
  @DisplayName("status shows both volumes to two decimal places")
  void statusLines() {
    final List<Text> lines = SoundConfigCommand.statusLines(new SoundSettings(0.5f, 1.25f));
    assertArrayEquals(new Object[] {"0.50"}, args(lines.get(0)));
    assertArrayEquals(new Object[] {"1.25"}, args(lines.get(1)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
