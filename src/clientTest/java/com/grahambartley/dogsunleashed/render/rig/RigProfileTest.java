package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RigProfileTest {

  private static final RigProfile PROFILE =
      RigProfile.parse(
          JsonParser.parseString(
                  """
                  {
                    "torso": ["Torso", "bone"],
                    "mouth": ["snout"],
                    "fur_shaggy": ["shag"],
                    "fur_overgrown": ["mop"],
                    "pupils": [[7, 47], [10, 48]],
                    "trimmed_overlay": "dogs-unleashed:textures/entity/fur/x_trimmed.png"
                  }
                  """)
              .getAsJsonObject());

  @Test
  @DisplayName("each slot reads the bone names listed under its key")
  void slotsReadTheirBones() {
    assertEquals(Set.of("Torso", "bone"), PROFILE.bonesIn(Slot.TORSO));
    assertEquals(Set.of("snout"), PROFILE.bonesIn(Slot.MOUTH));
    assertEquals(Set.of("shag"), PROFILE.bonesIn(Slot.FUR_SHAGGY));
    assertEquals(Set.of("mop"), PROFILE.bonesIn(Slot.FUR_OVERGROWN));
  }

  @Test
  @DisplayName("ears read their own shape and variants, and a profile without them has none")
  void earsRead() {
    final RigProfile withEars =
        RigProfile.parse(
            JsonParser.parseString(
                    """
                    {"ears": {"shape": "drop", "variants": {"pricked": ["up_l", "up_r"]}}}
                    """)
                .getAsJsonObject());

    assertEquals(Optional.of("drop"), withEars.ears().shape());
    assertEquals(Set.of("up_l", "up_r"), withEars.ears().variantBones());
    assertEquals(RigEars.NONE, PROFILE.ears());
  }

  @Test
  @DisplayName("a slot the profile leaves out is empty")
  void missingSlotIsEmpty() {
    final RigProfile torsoOnly =
        RigProfile.parse(JsonParser.parseString("{\"torso\": [\"main\"]}").getAsJsonObject());
    assertTrue(torsoOnly.bonesIn(Slot.FUR_SHAGGY).isEmpty());
    assertTrue(torsoOnly.mouthBone().isEmpty());
    assertTrue(torsoOnly.pupils().isEmpty());
  }

  @Test
  @DisplayName("a malformed pupil is skipped and the rest of the profile still reads")
  void malformedPupilIsSkipped() {
    final RigProfile profile =
        RigProfile.parse(
            JsonParser.parseString(
                    "{\"mouth\": [\"snout\"], \"pupils\": [[7], \"x\", [1, 2], [\"a\", 3], [7.5, 47], [4, 5]]}")
                .getAsJsonObject());

    assertEquals(List.of(new Texel(1, 2), new Texel(4, 5)), profile.pupils());
    assertEquals(Optional.of("snout"), profile.mouthBone());
  }

  @Test
  @DisplayName("pupils that are not a list are ignored")
  void pupilsThatAreNotAListAreIgnored() {
    final RigProfile profile =
        RigProfile.parse(JsonParser.parseString("{\"pupils\": \"7,47\"}").getAsJsonObject());

    assertTrue(profile.pupils().isEmpty());
  }

  @Test
  @DisplayName(
      "seats read in the order the profile lists them, skipping anything that is not a name")
  void seatsReadInOrder() {
    final RigProfile profile =
        RigProfile.parse(
            JsonParser.parseString("{\"seats\": [\"seat_front\", 7, \"seat_back\"]}")
                .getAsJsonObject());

    assertEquals(List.of("seat_front", "seat_back"), profile.seats());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{}", "{\"seats\": \"seat\"}"})
  @DisplayName("a profile with no seat list has no seats")
  void noSeatList(final String json) {
    assertTrue(RigProfile.parse(JsonParser.parseString(json).getAsJsonObject()).seats().isEmpty());
  }

  @Test
  @DisplayName("pupils read as texels in the order the profile lists them")
  void pupilsReadAsTexels() {
    assertEquals(List.of(new Texel(7, 47), new Texel(10, 48)), PROFILE.pupils());
  }

  @Test
  @DisplayName("the mouth bone is the one bone named in the mouth slot")
  void mouthBone() {
    assertEquals(Optional.of("snout"), PROFILE.mouthBone());
  }

  @Test
  @DisplayName("overlays read their texture ids and a missing one is absent")
  void overlays() {
    assertEquals(
        Optional.of(Identifier.of("dogs-unleashed", "textures/entity/fur/x_trimmed.png")),
        PROFILE.overlay(Overlay.TRIMMED));
    assertTrue(PROFILE.overlay(Overlay.BOTCHED).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Slot.class)
  @DisplayName("every slot reads its bones from its lower-case key")
  void everySlotReadsItsKey(final Slot slot) {
    final RigProfile profile =
        RigProfile.parse(
            JsonParser.parseString(
                    "{\"" + slot.name().toLowerCase(Locale.ROOT) + "\": [\"a\", \"b\"]}")
                .getAsJsonObject());

    assertEquals(Set.of("a", "b"), profile.bonesIn(slot));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Slot.class)
  @DisplayName("the empty profile names no bones")
  void emptyProfileHasNoBones(final Slot slot) {
    assertTrue(RigProfile.EMPTY.bonesIn(slot).isEmpty());
  }

  @Test
  @DisplayName("sheet overrides read per sheet and per slot")
  void sheetOverridesRead() {
    final Identifier sheet = Identifier.of("dogs-unleashed", "textures/entity/overlay/rot.png");
    final Identifier head = Identifier.of("dogs-unleashed", "textures/entity/overlay/rot_head.png");
    final RigProfile profile =
        RigProfile.parse(
            JsonParser.parseString(
                    """
                    {"sheet_overrides": {
                      "dogs-unleashed:textures/entity/overlay/rot.png": {
                        "head": "dogs-unleashed:textures/entity/overlay/rot_head.png"
                      }
                    }}
                    """)
                .getAsJsonObject());

    assertEquals(Optional.of(head), profile.sheetOverride(sheet, Slot.HEAD));
    assertTrue(profile.sheetOverride(sheet, Slot.TORSO).isEmpty());
    assertTrue(
        profile
            .sheetOverride(Identifier.of("dogs-unleashed", "textures/other.png"), Slot.HEAD)
            .isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"sheet_overrides\": \"rot\"}",
        "{\"sheet_overrides\": {\"Not A Valid Id\": {\"head\": \"dogs-unleashed:x.png\"}}}",
        "{\"sheet_overrides\": {\"dogs-unleashed:rot.png\": \"head\"}}",
        "{\"sheet_overrides\": {\"dogs-unleashed:rot.png\": {\"head\": 7, \"nose\": \"dogs-unleashed:x.png\"}}}",
        "{\"sheet_overrides\": {\"dogs-unleashed:rot.png\": {\"fur_shaggy\": \"dogs-unleashed:x.png\"}}}"
      })
  @DisplayName("malformed sheet overrides are ignored and the rest of the profile still reads")
  void malformedSheetOverridesAreIgnored(final String json) {
    final RigProfile profile = RigProfile.parse(JsonParser.parseString(json).getAsJsonObject());

    for (final Slot slot : Slot.values()) {
      assertTrue(profile.sheetOverride(Identifier.of("dogs-unleashed", "rot.png"), slot).isEmpty());
    }
  }
}
