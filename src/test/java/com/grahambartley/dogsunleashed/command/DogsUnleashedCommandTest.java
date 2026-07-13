package com.grahambartley.dogsunleashed.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogsUnleashedCommandTest {

  static Stream<Arguments> permissionCases() {
    return Stream.of(
        Arguments.of(0, false),
        Arguments.of(1, false),
        Arguments.of(2, true),
        Arguments.of(3, true),
        Arguments.of(4, true));
  }

  @ParameterizedTest(name = "level {0} -> isOp={1}")
  @MethodSource("permissionCases")
  @DisplayName("isOp returns true only when the source meets the op permission threshold")
  void isOpReflectsPermissionLevel(final int level, final boolean expected) {
    final ServerCommandSource source = mock(ServerCommandSource.class);
    when(source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL))
        .thenReturn(level >= ServerConfigService.OP_PERMISSION_LEVEL);

    assertEquals(expected, DogsUnleashedCommand.isOp(source));
  }

  static Stream<Arguments> helpKeyCases() {
    return Stream.of(
        Arguments.of(0, "command.dogs-unleashed.help.header"),
        Arguments.of(1, "command.dogs-unleashed.help.status"),
        Arguments.of(2, "command.dogs-unleashed.help.spawn"),
        Arguments.of(3, "command.dogs-unleashed.help.spawnrate"),
        Arguments.of(4, "command.dogs-unleashed.help.spawnratebreed"),
        Arguments.of(5, "command.dogs-unleashed.help.graves"),
        Arguments.of(6, "command.dogs-unleashed.help.autosleep"),
        Arguments.of(7, "command.dogs-unleashed.help.autosleeprange"),
        Arguments.of(8, "command.dogs-unleashed.help.barkvolume"),
        Arguments.of(9, "command.dogs-unleashed.help.howlvolume"),
        Arguments.of(10, "command.dogs-unleashed.help.reset"),
        Arguments.of(11, "command.dogs-unleashed.help.list"),
        Arguments.of(12, "command.dogs-unleashed.help.summon"),
        Arguments.of(13, "command.dogs-unleashed.help.find"));
  }

  @ParameterizedTest(name = "help line {0} -> {1}")
  @MethodSource("helpKeyCases")
  @DisplayName("help output is built from translation keys, not hardcoded literals")
  void helpLinesUseTranslationKeys(final int index, final String expectedKey) {
    final List<Text> lines = DogsUnleashedCommand.helpLines();

    assertEquals(expectedKey, translationKey(lines.get(index)));
  }

  static Stream<Arguments> statusKeyCases() {
    return Stream.of(
        Arguments.of(0, "command.dogs-unleashed.status.header"),
        Arguments.of(1, "command.dogs-unleashed.status.spawn"),
        Arguments.of(2, "command.dogs-unleashed.status.spawnrate"),
        Arguments.of(3, "command.dogs-unleashed.status.spawnrate.breed"),
        Arguments.of(4, "command.dogs-unleashed.status.spawnrate.breed"),
        Arguments.of(5, "command.dogs-unleashed.status.spawnrate.breed"),
        Arguments.of(6, "command.dogs-unleashed.status.spawnrate.breed"),
        Arguments.of(7, "command.dogs-unleashed.status.spawnrate.breed"),
        Arguments.of(8, "command.dogs-unleashed.status.graves"),
        Arguments.of(9, "command.dogs-unleashed.status.autosleep"),
        Arguments.of(10, "command.dogs-unleashed.status.autosleeprange"),
        Arguments.of(11, "command.dogs-unleashed.status.barkvolume"),
        Arguments.of(12, "command.dogs-unleashed.status.howlvolume"));
  }

  @ParameterizedTest(name = "status line {0} -> {1}")
  @MethodSource("statusKeyCases")
  @DisplayName("status output is built from translation keys, not hardcoded literals")
  void statusLinesUseTranslationKeys(final int index, final String expectedKey) {
    final List<Text> lines = DogsUnleashedCommand.statusLines(DogsUnleashedConfig.defaults());

    assertEquals(expectedKey, translationKey(lines.get(index)));
  }

  @Test
  @DisplayName("status value lines carry the live config values as translation arguments")
  void statusLinesCarryConfigValuesAsArguments() {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(true, 150, Map.of("beagle", 75), false, true, 32, 0.5f, 1.5f);

    final List<Text> lines = DogsUnleashedCommand.statusLines(config);

    assertEquals(true, translationArgs(lines.get(1))[0]);
    assertEquals(150, translationArgs(lines.get(2))[0]);
    assertEquals(false, translationArgs(lines.get(8))[0]);
    assertEquals(true, translationArgs(lines.get(9))[0]);
    assertEquals(32, translationArgs(lines.get(10))[0]);
    assertEquals("0.50", translationArgs(lines.get(11))[0]);
    assertEquals("1.50", translationArgs(lines.get(12))[0]);
  }

  static Stream<Arguments> breedStatusLineCases() {
    return Stream.of(
        Arguments.of(3, "husky", 100),
        Arguments.of(4, "dachshund", 100),
        Arguments.of(5, "beagle", 75),
        Arguments.of(6, "goldenretriever", 100),
        Arguments.of(7, "shibainu", 100));
  }

  @ParameterizedTest(name = "line {0} -> {1}={2}")
  @MethodSource("breedStatusLineCases")
  @DisplayName("status includes one spawnrate line per breed with its multiplier as arguments")
  void statusLinesCarryBreedSpawnRates(
      final int index, final String breedId, final int expectedPercent) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(true, 150, Map.of("beagle", 75), false, true, 32, 0.5f, 1.5f);

    final List<Text> lines = DogsUnleashedCommand.statusLines(config);

    assertEquals(breedId, translationArgs(lines.get(index))[0]);
    assertEquals(expectedPercent, translationArgs(lines.get(index))[1]);
  }

  private static String translationKey(final Text text) {
    final TranslatableTextContent content =
        assertInstanceOf(TranslatableTextContent.class, text.getContent());
    return content.getKey();
  }

  private static Object[] translationArgs(final Text text) {
    final TranslatableTextContent content =
        assertInstanceOf(TranslatableTextContent.class, text.getContent());
    return content.getArgs();
  }
}
