package com.grahambartley.dogsunleashed.render.fur;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FurCoverTest {

  private static final RigProfile PROFILE =
      new RigProfile(
          Map.of(
              Slot.TORSO,
              Set.of("body"),
              Slot.HEAD,
              Set.of("head"),
              Slot.NECK,
              Set.of("neck"),
              Slot.TAIL,
              Set.of("tail"),
              Slot.FUR_SHAGGY,
              Set.of("shag_body", "shag_neck", "shag_tail"),
              Slot.FUR_OVERGROWN,
              Set.of("mop_head", "mop_body")),
          Map.of(),
          List.of(),
          Map.of());

  private static FurCover wearing(final String gear) {
    final Set<String> worn = Set.of(gear.split(" "));
    return new FurCover(
        worn.contains("armour"),
        worn.contains("saddlebags"),
        worn.contains("harness"),
        worn.contains("costume"),
        worn.contains("hat"),
        worn.contains("bandana"),
        worn.contains("pendant"));
  }

  @ParameterizedTest(name = "{0} covers {1}")
  @CsvSource({
    "none, ''",
    "armour, TORSO",
    "saddlebags, TORSO",
    "harness, TORSO",
    "hat, HEAD",
    "bandana, NECK",
    "pendant, NECK",
    "armour hat pendant, TORSO HEAD NECK"
  })
  @DisplayName("each piece of gear covers the slot it sits on")
  void gearCoversItsSlot(final String gear, final String slots) {
    final Set<Slot> expected =
        slots.isEmpty()
            ? EnumSet.noneOf(Slot.class)
            : Arrays.stream(slots.split(" ")).map(Slot::valueOf).collect(Collectors.toSet());
    assertEquals(expected, wearing(gear).coveredSlots());
  }

  @ParameterizedTest(name = "costume covers {0}")
  @CsvSource({
    "TORSO",
    "MOUTH",
    "HEAD",
    "EYES",
    "NECK",
    "TAIL",
    "EAR_LEFT",
    "EAR_RIGHT",
    "LEG_FRONT_LEFT",
    "LEG_FRONT_RIGHT",
    "LEG_BACK_LEFT",
    "LEG_BACK_RIGHT"
  })
  @DisplayName("a costume covers every body slot, since the suit is drawn over the whole dog")
  void costumeCoversTheWholeBody(final Slot slot) {
    assertEquals(true, wearing("costume").coveredSlots().contains(slot));
  }

  @ParameterizedTest(name = "{0}: {1} on {2} hidden={3}")
  @CsvSource({
    "none, shag_body, body, false",
    "armour, shag_body, body, true",
    "armour, mop_body, body, true",
    "armour, shag_neck, neck, false",
    "armour, shag_tail, tail, false",
    "saddlebags, shag_body, body, true",
    "harness, mop_body, body, true",
    "hat, mop_head, head, true",
    "hat, shag_body, body, false",
    "bandana, shag_neck, neck, true",
    "pendant, shag_neck, neck, true",
    "costume, shag_tail, tail, true",
    "costume, mop_head, head, true",
    "armour, body, body, false",
    "costume, head, '', false",
    "hat, mop_head, '', false"
  })
  @DisplayName("only fur growing from a covered bone is hidden, and real body bones never are")
  void hidesFurUnderGear(
      final String gear, final String bone, final String parent, final boolean expected) {
    assertEquals(expected, wearing(gear).hides(PROFILE, bone, parent.isEmpty() ? null : parent));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"armour", "costume", "hat", "pendant"})
  @DisplayName("a rig with no fur art hides nothing, whatever it wears")
  void noFurArtHidesNothing(final String gear) {
    assertEquals(false, wearing(gear).hides(RigProfile.EMPTY, "body", "root"));
  }
}
