package com.grahambartley.dogsunleashed.render.fur;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FurLookTest {

  private static final Identifier TRIMMED = Identifier.of("dogs-unleashed", "trimmed.png");
  private static final Identifier BOTCHED = Identifier.of("dogs-unleashed", "botched.png");
  private static final RigProfile PROFILE =
      new RigProfile(
          Map.of(
              Slot.TORSO,
              Set.of("body"),
              Slot.FUR_SHAGGY,
              Set.of("shag"),
              Slot.FUR_OVERGROWN,
              Set.of("mop")),
          Map.of(Overlay.TRIMMED, TRIMMED, Overlay.BOTCHED, BOTCHED),
          List.of(),
          Map.of());

  @ParameterizedTest(name = "{0} at {1} shows={2}")
  @CsvSource({
    "shag, TRIMMED, false",
    "shag, NATURAL, false",
    "shag, SHAGGY, true",
    "shag, OVERGROWN, true",
    "mop, NATURAL, false",
    "mop, SHAGGY, false",
    "mop, OVERGROWN, true",
    "body, TRIMMED, true",
    "body, OVERGROWN, true"
  })
  @DisplayName("fur bones show from their stage up and every other bone always shows")
  void bonesShowByStage(final String bone, final DogFurStage stage, final boolean expected) {
    assertEquals(expected, FurLook.shows(PROFILE, bone, stage));
  }

  @ParameterizedTest(name = "{0} draws {1} overlays")
  @CsvSource({"BOTCHED, 2", "TRIMMED, 1", "NATURAL, 0", "SHAGGY, 0", "OVERGROWN, 0"})
  @DisplayName("a trimmed coat gets the trim overlay, and a botched one the botch on top")
  void overlaysOnlyWhenTrimmed(final DogFurStage stage, final int count) {
    assertEquals(List.of(TRIMMED, BOTCHED).subList(0, count), FurLook.overlays(PROFILE, stage));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"BOTCHED", "TRIMMED", "NATURAL", "SHAGGY", "OVERGROWN"})
  @DisplayName("a rig with no fur art draws exactly what it draws today")
  void noArtChangesNothing(final DogFurStage stage) {
    assertEquals(true, FurLook.shows(RigProfile.EMPTY, "any", stage));
    assertEquals(List.of(), FurLook.overlays(RigProfile.EMPTY, stage));
  }
}
