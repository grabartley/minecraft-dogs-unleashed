package com.grahambartley.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.server.ServerConfigService;
import java.util.List;
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
        Arguments.of(3, "command.dogs-unleashed.help.graves"),
        Arguments.of(4, "command.dogs-unleashed.help.autosleep"),
        Arguments.of(5, "command.dogs-unleashed.help.autosleeprange"),
        Arguments.of(6, "command.dogs-unleashed.help.barkvolume"),
        Arguments.of(7, "command.dogs-unleashed.help.howlvolume"),
        Arguments.of(8, "command.dogs-unleashed.help.reset"));
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
        Arguments.of(2, "command.dogs-unleashed.status.graves"),
        Arguments.of(3, "command.dogs-unleashed.status.autosleep"),
        Arguments.of(4, "command.dogs-unleashed.status.autosleeprange"),
        Arguments.of(5, "command.dogs-unleashed.status.barkvolume"),
        Arguments.of(6, "command.dogs-unleashed.status.howlvolume"));
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
    final DogsUnleashedConfig config = new DogsUnleashedConfig(true, false, true, 32, 0.5f, 1.5f);

    final List<Text> lines = DogsUnleashedCommand.statusLines(config);

    assertEquals(true, translationArgs(lines.get(1))[0]);
    assertEquals(false, translationArgs(lines.get(2))[0]);
    assertEquals(true, translationArgs(lines.get(3))[0]);
    assertEquals(32, translationArgs(lines.get(4))[0]);
    assertEquals("0.50", translationArgs(lines.get(5))[0]);
    assertEquals("1.50", translationArgs(lines.get(6))[0]);
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
