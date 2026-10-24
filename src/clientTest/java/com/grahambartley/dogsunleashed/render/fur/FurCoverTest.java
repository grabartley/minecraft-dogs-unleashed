package com.grahambartley.dogsunleashed.render.fur;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static Set<Slot> slots(final String gear) {
    final Set<String> worn = Set.of(gear.split(" "));
    final Optional<DogCosmetic.Mount> cosmetic =
        worn.contains("hat")
            ? Optional.of(DogCosmetic.Mount.HEAD)
            : worn.contains("bandana") ? Optional.of(DogCosmetic.Mount.COLLAR) : Optional.empty();
    return FurCover.slotsCovered(
        worn.contains("armour"),
        worn.contains("saddlebags"),
        worn.contains("harness"),
        worn.contains("costume"),
        cosmetic,
        worn.contains("mobhead"),
        worn.contains("pendant"),
        worn.contains("collar"));
  }

  private static FurCover wearing(final String gear) {
    return new FurCover(slots(gear));
  }

  @ParameterizedTest(name = "{0} covers {1}")
  @CsvSource({
    "none, ''",
    "armour, TORSO",
    "saddlebags, TORSO",
    "harness, TORSO",
    "hat, HEAD",
    "mobhead, HEAD",
    "bandana, NECK",
    "pendant, NECK",
    "collar, NECK",
    "armour hat pendant, TORSO HEAD NECK",
    "saddlebags mobhead bandana, TORSO HEAD NECK"
  })
  @DisplayName("each piece of gear covers the slot it sits on")
  void gearCoversItsSlot(final String gear, final String expected) {
    final Set<Slot> slots =
        expected.isEmpty()
            ? EnumSet.noneOf(Slot.class)
            : Arrays.stream(expected.split(" ")).map(Slot::valueOf).collect(Collectors.toSet());
    assertEquals(slots, slots(gear));
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
    assertEquals(true, slots("costume").contains(slot));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"FUR_SHAGGY", "FUR_OVERGROWN"})
  @DisplayName("a costume never lists the fur slots themselves as covered")
  void costumeLeavesFurSlotsOut(final Slot slot) {
    assertEquals(false, slots("costume").contains(slot));
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
    "mobhead, mop_head, head, true",
    "hat, shag_body, body, false",
    "bandana, shag_neck, neck, true",
    "pendant, shag_neck, neck, true",
    "collar, shag_neck, neck, true",
    "collar, shag_body, body, false",
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

  @ParameterizedTest(name = "{0}")
  @CsvSource({"shag_body, body", "mop_head, head", "shag_neck, neck"})
  @DisplayName("a dog wearing nothing hides no fur")
  void nothingWornHidesNothing(final String bone, final String parent) {
    assertEquals(false, FurCover.NONE.hides(PROFILE, bone, parent));
  }
}
