package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.AgingSettings;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AgingConfigCommandTest {

  @Test
  @DisplayName("help lists the aging toggle and the senior age bounds")
  void helpLines() {
    final List<Text> lines = AgingConfigCommand.helpLines();
    assertEquals("command.dogs-unleashed.help.dogaging", key(lines.get(0)));
    assertEquals("command.dogs-unleashed.help.senioragedays", key(lines.get(1)));
    assertArrayEquals(
        new Object[] {AgingSettings.DAYS_MIN, AgingSettings.DAYS_MAX}, args(lines.get(1)));
  }

  static Stream<Arguments> statusCases() {
    return Stream.of(Arguments.of(false, 100), Arguments.of(true, 7), Arguments.of(true, 1000));
  }

  @ParameterizedTest(name = "aging {0}, senior after {1} days")
  @MethodSource("statusCases")
  @DisplayName("status shows each live aging setting")
  void statusLines(final boolean enabled, final int days) {
    final List<Text> lines = AgingConfigCommand.statusLines(new AgingSettings(enabled, days));
    assertEquals("command.dogs-unleashed.status.dogaging", key(lines.get(0)));
    assertArrayEquals(new Object[] {enabled}, args(lines.get(0)));
    assertArrayEquals(new Object[] {days}, args(lines.get(1)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
