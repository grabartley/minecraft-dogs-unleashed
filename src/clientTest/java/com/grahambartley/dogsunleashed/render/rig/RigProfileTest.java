package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
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
                    "head": ["head"],
                    "fur_shaggy": ["shag"],
                    "fur_overgrown": ["mop"],
                    "trimmed_overlay": "dogs-unleashed:textures/entity/fur/x_trimmed.png"
                  }
                  """)
              .getAsJsonObject());

  @Test
  @DisplayName("each slot reads the bone names listed under its key")
  void slotsReadTheirBones() {
    assertEquals(Set.of("Torso", "bone"), PROFILE.bonesIn(Slot.TORSO));
    assertEquals(Set.of("head"), PROFILE.bonesIn(Slot.HEAD));
    assertEquals(Set.of("shag"), PROFILE.bonesIn(Slot.FUR_SHAGGY));
    assertEquals(Set.of("mop"), PROFILE.bonesIn(Slot.FUR_OVERGROWN));
  }

  @Test
  @DisplayName("a slot the profile leaves out is empty")
  void missingSlotIsEmpty() {
    assertTrue(PROFILE.bonesIn(Slot.TAIL).isEmpty());
  }

  @Test
  @DisplayName("overlays read their texture ids and a missing one is absent")
  void overlays() {
    assertEquals(
        Optional.of(Identifier.of("dogs-unleashed", "textures/entity/fur/x_trimmed.png")),
        PROFILE.overlay(Overlay.TRIMMED));
    assertTrue(PROFILE.overlay(Overlay.SCUFFED).isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Slot.class)
  @DisplayName("the empty profile names no bones")
  void emptyProfileHasNoBones(final Slot slot) {
    assertTrue(RigProfile.EMPTY.bonesIn(slot).isEmpty());
  }
}
