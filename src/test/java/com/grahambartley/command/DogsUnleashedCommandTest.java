package com.grahambartley.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  @ParameterizedTest(name = "level {0} -> isOp={1}")
  @MethodSource("permissionCases")
  @DisplayName("isOp returns true only when the source meets the op permission threshold")
  void isOpReflectsPermissionLevel(final int level, final boolean expected) {
    final ServerCommandSource source = mock(ServerCommandSource.class);
    when(source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL))
        .thenReturn(level >= ServerConfigService.OP_PERMISSION_LEVEL);

    assertEquals(expected, DogsUnleashedCommand.isOp(source));
  }
}
