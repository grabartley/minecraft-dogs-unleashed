package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.GameplaySettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameplayConfigCommandTest {

  @Test
  @DisplayName("help lists graves, dropping the leash and name tags")
  void helpLines() {
    assertEquals(
        List.of(
            "command.dogs-unleashed.help.graves",
            "command.dogs-unleashed.help.dropleashonplay",
            "command.dogs-unleashed.help.shownames"),
        GameplayConfigCommand.helpLines().stream().map(GameplayConfigCommandTest::key).toList());
  }

  @Test
  @DisplayName("status shows each live gameplay setting")
  void statusLines() {
    final List<Text> lines =
        GameplayConfigCommand.statusLines(new GameplaySettings(false, true, false));
    assertArrayEquals(new Object[] {false}, args(lines.get(0)));
    assertArrayEquals(new Object[] {true}, args(lines.get(1)));
    assertArrayEquals(new Object[] {false}, args(lines.get(2)));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
