package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.config.ClientState;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KeybindDiscoveryNudgeTest {

  @TempDir Path tempDir;

  @ParameterizedTest(name = "{0} -> shows nudge: {2}")
  @MethodSource("nudgeStates")
  @DisplayName("the nudge is offered only to clients that have never seen it")
  void nudgeIsOfferedOnlyOnce(final String label, final ClientState state, final boolean expected) {
    assertEquals(expected, KeybindDiscoveryNudge.shouldShowNudge(state));
  }

  static Stream<Arguments> nudgeStates() {
    return Stream.of(
        Arguments.of("fresh install", ClientState.defaults(), true),
        Arguments.of("nudge already shown", new ClientState(true), false),
        Arguments.of("explicitly unshown", new ClientState(false), true),
        Arguments.of("state unavailable", null, false));
  }

  @Test
  @DisplayName("persisting the nudge across a simulated restart stops it from showing again")
  void persistedNudgeDoesNotShowAgainAfterRestart() {
    final Path statePath = tempDir.resolve("dogs-unleashed").resolve("client-state.json");

    final ClientState firstLaunch = ClientState.load(statePath);
    assertTrue(KeybindDiscoveryNudge.shouldShowNudge(firstLaunch));
    ClientState.save(statePath, firstLaunch.withKeybindNudgeShown(true));

    assertFalse(KeybindDiscoveryNudge.shouldShowNudge(ClientState.load(statePath)));
  }
}
