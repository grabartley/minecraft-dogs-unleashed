package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PetLifeStateDisplayTest {

  private static final int LIVING = 0xFFFFFF;
  private static final int DECEASED = 0x888888;

  static Stream<Arguments> nameColorCases() {
    return Stream.of(
        Arguments.of(PetLifeState.LIVING, LIVING),
        Arguments.of(PetLifeState.UNDEAD, PetLifeStateDisplay.UNDEAD_COLOR),
        Arguments.of(PetLifeState.DECEASED, DECEASED),
        Arguments.of(PetLifeState.LOST, DECEASED));
  }

  @ParameterizedTest(name = "{0} renders its name in {1}")
  @MethodSource("nameColorCases")
  @DisplayName("undead pets get their own name colour, and lost pets read as deceased")
  void nameColorDistinguishesUndeadFromDeceased(final PetLifeState lifeState, final int expected) {
    assertEquals(expected, PetLifeStateDisplay.nameColor(lifeState, LIVING, DECEASED));
  }

  @ParameterizedTest(name = "{0} has a status label")
  @EnumSource(
      value = PetLifeState.class,
      names = {"UNDEAD", "DECEASED", "LOST"})
  @DisplayName("every state except plain living carries a status label")
  void nonLivingStatesHaveALabel(final PetLifeState lifeState) {
    assertNotNull(PetLifeStateDisplay.statusLabel(lifeState));
  }

  @ParameterizedTest(name = "{0} labels are distinct")
  @EnumSource(
      value = PetLifeState.class,
      names = {"UNDEAD", "DECEASED", "LOST"})
  @DisplayName("each status label uses its own translation key")
  void statusLabelsAreDistinct(final PetLifeState lifeState) {
    for (final PetLifeState other : PetLifeState.values()) {
      if (other == lifeState || other == PetLifeState.LIVING) {
        continue;
      }
      assertEquals(
          false,
          PetLifeStateDisplay.statusLabel(lifeState)
              .getContent()
              .equals(PetLifeStateDisplay.statusLabel(other).getContent()),
          lifeState + " must not share a label with " + other);
    }
  }

  @ParameterizedTest(name = "a living pet needs no status label")
  @EnumSource(
      value = PetLifeState.class,
      names = {"LIVING"})
  @DisplayName("a living pet shows no status of its own")
  void livingHasNoLabel(final PetLifeState lifeState) {
    assertNull(PetLifeStateDisplay.statusLabel(lifeState));
  }

  @ParameterizedTest(name = "{0} status colour")
  @MethodSource("statusColorCases")
  @DisplayName("the undead status reads in its own colour, the gone states in the warning colour")
  void statusColorSeparatesUndeadFromGone(final PetLifeState lifeState, final int expected) {
    assertEquals(expected, PetLifeStateDisplay.statusColor(lifeState));
  }

  static Stream<Arguments> statusColorCases() {
    return Stream.of(
        Arguments.of(PetLifeState.UNDEAD, PetLifeStateDisplay.UNDEAD_COLOR),
        Arguments.of(PetLifeState.DECEASED, PetLifeStateDisplay.GONE_COLOR),
        Arguments.of(PetLifeState.LOST, PetLifeStateDisplay.GONE_COLOR));
  }
}
