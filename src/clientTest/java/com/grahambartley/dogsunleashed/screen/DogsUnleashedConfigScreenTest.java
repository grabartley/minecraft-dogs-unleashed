package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.screen.DogsUnleashedConfigScreen.EditAccess;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogsUnleashedConfigScreenTest {

  static Stream<Arguments> accessCases() {
    return Stream.of(
        Arguments.of("title screen, no player", false, false, EditAccess.NO_WORLD),
        Arguments.of("title screen, stale edit flag", false, true, EditAccess.NO_WORLD),
        Arguments.of("in world without edit permission", true, false, EditAccess.READ_ONLY),
        Arguments.of("in world with edit permission", true, true, EditAccess.EDITABLE));
  }

  @ParameterizedTest(name = "{0} resolves to {3}")
  @MethodSource("accessCases")
  @DisplayName("edit access is resolved from world membership and edit permission")
  void resolvesAccess(
      final String label, final boolean inWorld, final boolean canEdit, final EditAccess expected) {
    assertEquals(expected, EditAccess.resolve(inWorld, canEdit));
  }

  @ParameterizedTest(name = "{0} carries banner text")
  @EnumSource(
      value = EditAccess.class,
      names = {"READ_ONLY", "NO_WORLD"})
  @DisplayName("every non-editable state carries banner and hint translation keys")
  void nonEditableStatesHaveBannerText(final EditAccess access) {
    assertFalse(access.bannerKey().isEmpty());
    assertFalse(access.hintKey().isEmpty());
    assertTrue(access.bannerKey().startsWith("screen.dogs-unleashed.settings."));
    assertTrue(access.hintKey().startsWith("screen.dogs-unleashed.settings."));
  }

  @Test
  @DisplayName("no-world and read-only states show different text so the reason is unambiguous")
  void nonEditableStatesAreDistinguishable() {
    assertNotEquals(EditAccess.READ_ONLY.bannerKey(), EditAccess.NO_WORLD.bannerKey());
    assertNotEquals(EditAccess.READ_ONLY.hintKey(), EditAccess.NO_WORLD.hintKey());
  }

  @DisplayName("the editable state renders no banner")
  @ParameterizedTest(name = "{0} has no banner keys")
  @EnumSource(
      value = EditAccess.class,
      names = {"EDITABLE"})
  void editableStateHasNoBanner(final EditAccess access) {
    assertTrue(access.bannerKey().isEmpty());
    assertTrue(access.hintKey().isEmpty());
  }
}
