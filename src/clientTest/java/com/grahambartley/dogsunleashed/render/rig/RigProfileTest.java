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
}
