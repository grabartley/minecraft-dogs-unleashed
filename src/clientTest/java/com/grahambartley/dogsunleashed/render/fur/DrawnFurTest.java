package com.grahambartley.dogsunleashed.render.fur;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrawnFurTest {

  private static final RigProfile PROFILE =
      new RigProfile(
          Map.of(
              Slot.TORSO,
              Set.of("body"),
              Slot.NECK,
              Set.of("neck"),
              Slot.FUR_SHAGGY,
              Set.of("shag_body", "shag_neck"),
              Slot.FUR_OVERGROWN,
              Set.of("mop_body")),
          Map.of(),
          List.of(),
          Map.of());
  private static final Map<String, String> PARENTS =
      Map.of("shag_body", "body", "shag_neck", "neck", "mop_body", "body", "neck", "body");
  private static final Set<String> BONES =
      Set.of("body", "neck", "shag_body", "shag_neck", "mop_body");

  private static FurCover wearing(final String gear) {
    final Set<String> worn = Set.of(gear.split(" "));
    return new FurCover(
        FurCover.slotsCovered(
            worn.contains("armour"),
            false,
            false,
            false,
            Optional.<DogCosmetic.Mount>empty(),
            false,
            worn.contains("pendant"),
            worn.contains("collar")));
  }

  @ParameterizedTest(name = "{0} wearing {1} draws {2}")
  @CsvSource({
    "NATURAL, none, body neck",
    "SHAGGY, none, body neck shag_body shag_neck",
    "OVERGROWN, none, body neck shag_body shag_neck mop_body",
    "OVERGROWN, collar, body neck shag_body mop_body",
    "OVERGROWN, pendant, body neck shag_body mop_body",
    "OVERGROWN, armour, body neck shag_neck",
    "SHAGGY, armour collar, body neck",
    "TRIMMED, collar, body neck"
  })
  @DisplayName("charms rest only on the bones and fur actually drawn this frame")
  void keepsOnlyDrawnBones(final DogFurStage stage, final String gear, final String expected) {
    assertEquals(
        Arrays.stream(expected.split(" ")).collect(Collectors.toSet()),
        DrawnFur.keep(BONES, PROFILE, stage, wearing(gear), PARENTS::get));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"NATURAL", "SHAGGY", "OVERGROWN", "BOTCHED"})
  @DisplayName("a rig with no fur art keeps every bone")
  void noFurArtKeepsEverything(final DogFurStage stage) {
    assertEquals(
        BONES, DrawnFur.keep(BONES, RigProfile.EMPTY, stage, wearing("collar"), PARENTS::get));
  }

  @ParameterizedTest(name = "{1} at {0} wearing {2} draws={3}")
  @CsvSource({
    "SHAGGY, shag_neck, none, true",
    "SHAGGY, shag_neck, collar, false",
    "NATURAL, shag_body, none, false",
    "OVERGROWN, mop_body, armour, false",
    "OVERGROWN, body, armour, true"
  })
  @DisplayName("one bone is drawn only when its stage shows it and no gear covers it")
  void drawsOneBone(
      final DogFurStage stage, final String bone, final String gear, final boolean expected) {
    assertEquals(expected, DrawnFur.draws(PROFILE, stage, wearing(gear), bone, PARENTS.get(bone)));
  }
}
