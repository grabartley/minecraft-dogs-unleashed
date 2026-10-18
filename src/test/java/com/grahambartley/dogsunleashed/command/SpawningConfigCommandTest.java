package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.SpawningSettings;
import java.util.List;
import java.util.Map;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpawningConfigCommandTest {

  @Test
  @DisplayName("help lists spawning, both spawn rates with bounds, and the cap toggle")
  void helpLines() {
    final List<Text> lines = SpawningConfigCommand.helpLines();
    assertEquals("command.dogs-unleashed.help.spawn", key(lines.get(0)));
    assertArrayEquals(
        new Object[] {SpawningSettings.RATE_MIN, SpawningSettings.RATE_MAX}, args(lines.get(1)));
    assertArrayEquals(
        new Object[] {SpawningSettings.RATE_MIN, SpawningSettings.RATE_MAX}, args(lines.get(2)));
    assertEquals("command.dogs-unleashed.help.capindependentspawning", key(lines.get(3)));
  }

  @Test
  @DisplayName("status shows the live switches, the global rate and each breed rate")
  void statusLines() {
    final List<Text> lines =
        SpawningConfigCommand.statusLines(
            new SpawningSettings(false, 150, Map.of("beagle", 0), true));
    assertArrayEquals(new Object[] {false}, args(lines.get(0)));
    assertArrayEquals(new Object[] {150}, args(lines.get(1)));
    assertEquals("command.dogs-unleashed.status.capindependentspawning", key(lines.getLast()));
    assertArrayEquals(new Object[] {true}, args(lines.getLast()));
    assertEquals(
        List.of(0),
        lines.stream()
            .filter(line -> key(line).equals("command.dogs-unleashed.status.spawnrate.breed"))
            .filter(line -> args(line)[0].equals("beagle"))
            .map(line -> args(line)[1])
            .toList());
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }

  private static Object[] args(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getArgs();
  }
}
