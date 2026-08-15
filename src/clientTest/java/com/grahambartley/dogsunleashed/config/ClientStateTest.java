package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientStateTest {

  @TempDir Path tempDir;

  private Path writeState(final String json) throws IOException {
    final Path path = tempDir.resolve("client-state.json");
    Files.writeString(path, json, StandardCharsets.UTF_8);
    return path;
  }

  @Test
  @DisplayName("a fresh install has not seen the keybind nudge yet")
  void defaultsHaveNotShownTheNudge() {
    assertFalse(ClientState.defaults().keybindNudgeShown());
  }

  @Test
  @DisplayName("a missing state file loads as a fresh install")
  void missingFileLoadsDefaults() {
    assertEquals(ClientState.defaults(), ClientState.load(tempDir.resolve("does-not-exist.json")));
  }

  @Test
  @DisplayName("a null path loads as a fresh install")
  void nullPathLoadsDefaults() {
    assertEquals(ClientState.defaults(), ClientState.load(null));
  }

  @ParameterizedTest(name = "keybindNudgeShown={0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("saving then loading round-trips the nudge flag")
  void saveThenLoadRoundTrips(final boolean shown) {
    final Path path = tempDir.resolve("nested").resolve("client-state.json");

    assertTrue(ClientState.save(path, new ClientState(shown)));

    assertEquals(shown, ClientState.load(path).keybindNudgeShown());
  }

  @Test
  @DisplayName("saving creates the parent directory when it does not exist")
  void saveCreatesParentDirectory() {
    final Path path = tempDir.resolve("config").resolve("dogs-unleashed").resolve("state.json");

    assertTrue(ClientState.save(path, ClientState.defaults()));

    assertTrue(Files.exists(path));
  }

  @Test
  @DisplayName("saving leaves no temp file behind")
  void saveLeavesNoTempFile() throws IOException {
    final Path path = tempDir.resolve("client-state.json");

    ClientState.save(path, new ClientState(true));

    try (Stream<Path> files = Files.list(tempDir)) {
      assertTrue(files.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nullArguments")
  @DisplayName("saving with a null path or state reports failure instead of throwing")
  void saveRejectsNullArguments(final String label, final Path path, final ClientState state) {
    assertFalse(ClientState.save(path, state));
  }

  static Stream<Arguments> nullArguments() {
    return Stream.of(
        Arguments.of("null path", null, ClientState.defaults()),
        Arguments.of("null state", Path.of("client-state.json"), null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unreadableStateFiles")
  @DisplayName("an unusable state file falls back to a fresh install instead of crashing startup")
  void unusableFileLoadsDefaults(final String label, final String json) throws IOException {
    final Path path = writeState(json);

    assertEquals(ClientState.defaults(), ClientState.load(path));
  }

  static Stream<Arguments> unreadableStateFiles() {
    return Stream.of(
        Arguments.of("truncated json", "{\"keybindNudgeShown\": tr"),
        Arguments.of("not an object", "[1, 2, 3]"),
        Arguments.of("empty file", ""),
        Arguments.of("json null", "null"),
        Arguments.of("flag is an object", "{\"keybindNudgeShown\": {}}"),
        Arguments.of("flag is an array", "{\"keybindNudgeShown\": []}"));
  }

  @Test
  @DisplayName("an unrelated key does not disturb the nudge flag")
  void unknownKeysAreIgnored() throws IOException {
    final Path path = writeState("{\"keybindNudgeShown\": true, \"somethingElse\": 42}");

    assertTrue(ClientState.load(path).keybindNudgeShown());
  }

  @Test
  @DisplayName("a state file missing the nudge flag falls back to the default")
  void missingKeyFallsBackToDefault() throws IOException {
    final Path path = writeState("{\"somethingElse\": 42}");

    assertEquals(ClientState.defaults(), ClientState.load(path));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("markedStates")
  @DisplayName("marking the nudge as shown produces the requested flag")
  void withKeybindNudgeShownSetsFlag(
      final String label, final ClientState initial, final boolean value) {
    assertEquals(value, initial.withKeybindNudgeShown(value).keybindNudgeShown());
  }

  static Stream<Arguments> markedStates() {
    return Stream.of(
        Arguments.of("fresh install marked shown", ClientState.defaults(), true),
        Arguments.of("already shown stays shown", new ClientState(true), true),
        Arguments.of("already shown reset to unshown", new ClientState(true), false));
  }

  @Test
  @DisplayName("the persisted file uses the documented json key")
  void savedFileUsesDocumentedKey() throws IOException {
    final Path path = tempDir.resolve("client-state.json");

    ClientState.save(path, new ClientState(true));

    assertTrue(
        Files.readString(path, StandardCharsets.UTF_8)
            .contains(ClientState.KEY_KEYBIND_NUDGE_SHOWN));
  }

  @Test
  @DisplayName("saving over an existing state file replaces its contents")
  void saveOverwritesExistingFile() throws IOException {
    final Path path = writeState("{\"keybindNudgeShown\": false}");

    assertTrue(ClientState.save(path, new ClientState(true)));

    assertTrue(ClientState.load(path).keybindNudgeShown());
  }
}
