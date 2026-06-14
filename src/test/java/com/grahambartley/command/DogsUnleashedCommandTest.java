package com.grahambartley.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.server.ServerConfigService;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.DisplayName;
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

  @ParameterizedTest
  @MethodSource("permissionCases")
  @DisplayName("isOp returns true only for permission level >= 2")
  void isOpRespectsPermissionLevel(final int level, final boolean expected) {
    final ServerCommandSource source = mock(ServerCommandSource.class);
    lenient()
        .when(source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL))
        .thenReturn(level >= ServerConfigService.OP_PERMISSION_LEVEL);
    assertEquals(expected, DogsUnleashedCommand.isOp(source));
  }

  @ParameterizedTest
  @MethodSource("permissionCases")
  @DisplayName("OP_PERMISSION_LEVEL constant is consistent with permission-check behavior")
  void opPermissionLevelMatchesExpectedThreshold(final int level, final boolean expected) {
    final boolean wouldPass = level >= ServerConfigService.OP_PERMISSION_LEVEL;
    assertEquals(expected, wouldPass);
  }

  @org.junit.jupiter.api.Test
  @DisplayName("Config range constants are within valid ranges")
  void configRangesAreSane() {
    assertTrue(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN < DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX);
    assertTrue(DogsUnleashedConfig.VOLUME_MIN < DogsUnleashedConfig.VOLUME_MAX);
    assertTrue(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN >= 1);
    assertFalse(DogsUnleashedConfig.VOLUME_MIN < 0.0f);
  }
}
