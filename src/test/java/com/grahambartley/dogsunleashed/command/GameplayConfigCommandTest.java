package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.GameplaySettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GameplayConfigCommandTest {

  @Test
  @DisplayName("help lists graves, dropping the leash, name tags and muddy dogs")
  void helpLines() {
    assertEquals(
        List.of(
            "command.dogs-unleashed.help.graves",
            "command.dogs-unleashed.help.dropleashonplay",
            "command.dogs-unleashed.help.shownames",
            "command.dogs-unleashed.help.muddydogs"),
        GameplayConfigCommand.helpLines().stream().map(GameplayConfigCommandTest::key).toList());
  }

  @ParameterizedTest(name = "line {0} reads {1}")
  @CsvSource({"0, false", "1, true", "2, false", "3, true"})
  @DisplayName("status shows each live gameplay setting")
  void statusLines(final int line, final boolean value) {
    final List<Text> lines =
        GameplayConfigCommand.statusLines(new GameplaySettings(false, true, false, true));
    assertEquals(4, lines.size());
    assertArrayEquals(new Object[] {value}, args(lines.get(line)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
