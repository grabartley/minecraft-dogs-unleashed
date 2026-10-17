package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PetNamingScreenTest {

  static Stream<Arguments> blankInputs() {
    return Stream.of(
        Arguments.of("null", null),
        Arguments.of("empty string", ""),
        Arguments.of("spaces only", "   "),
        Arguments.of("tabs and spaces", " \t "));
  }

  @ParameterizedTest(name = "{0} yields no submittable name")
  @MethodSource("blankInputs")
  @DisplayName("blank-after-trim input resolves to no submittable name so no send fires")
  void blankResolvesToEmpty(final String label, final String rawText) {
    assertFalse(PetNamingScreen.resolveSubmittableName(rawText).isPresent());
  }

  static Stream<Arguments> nonBlankInputs() {
    return Stream.of(
        Arguments.of("plain value", "Rex", "Rex"),
        Arguments.of("leading whitespace stripped", "   Rex", "Rex"),
        Arguments.of("trailing whitespace stripped", "Rex   ", "Rex"),
        Arguments.of("surrounding whitespace stripped", "  Rex  ", "Rex"),
        Arguments.of("interior spaces preserved", "  Good Boy  ", "Good Boy"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nonBlankInputs")
  @DisplayName("non-blank input resolves to the trimmed name")
  void nonBlankResolvesToTrimmedName(
      final String label, final String rawText, final String expected) {
    final Optional<String> resolved = PetNamingScreen.resolveSubmittableName(rawText);
    assertTrue(resolved.isPresent());
    assertEquals(expected, resolved.get());
  }

  @Test
  @DisplayName("NAME_MAX_LENGTH matches the vanilla-friendly 32 character cap")
  void nameMaxLengthIs32() {
    assertEquals(32, PetNamingScreen.NAME_MAX_LENGTH);
  }

  @ParameterizedTest(name = "bornPuppy={0} uses {1}")
  @MethodSource("headerKeys")
  @DisplayName("a bred puppy is welcomed as a newborn while a tamed dog keeps the tamed header")
  void headerMatchesHowTheDogJoined(final boolean bornPuppy, final String expectedKey) {
    assertEquals(expectedKey, PetNamingScreen.headerKey(bornPuppy));
  }

  static Stream<Arguments> headerKeys() {
    return Stream.of(
        Arguments.of(false, "screen.dogs-unleashed.pet_naming.header"),
        Arguments.of(true, "screen.dogs-unleashed.pet_naming.header_newborn"));
  }

  @Test
  @DisplayName("the field starts with the suggested name")
  void firstInitShowsSuggestedName() {
    final MinecraftClient client = client();
    final PetNamingScreen screen = newScreen();
    screen.init(client, 400, 300);

    assertEquals("Biscuit", nameField(screen).getText());
  }

  @Test
  @DisplayName("a typed name survives the window being resized")
  void typedNameSurvivesResize() {
    final MinecraftClient client = client();
    final PetNamingScreen screen = newScreen();
    screen.init(client, 400, 300);
    nameField(screen).setText("Sir Barksalot");

    screen.resize(client, 640, 360);

    assertEquals("Sir Barksalot", nameField(screen).getText());
  }

  private static PetNamingScreen newScreen() {
    return new PetNamingScreen(UUID.randomUUID(), Text.literal("Husky"), false, "Biscuit");
  }

  private static MinecraftClient client() {
    final MinecraftClient client = mock(MinecraftClient.class, RETURNS_DEEP_STUBS);
    when(client.getNavigationType()).thenReturn(GuiNavigationType.MOUSE);
    return client;
  }

  private static TextFieldWidget nameField(final PetNamingScreen screen) {
    return screen.children().stream()
        .filter(TextFieldWidget.class::isInstance)
        .map(TextFieldWidget.class::cast)
        .findFirst()
        .orElseThrow();
  }
}
