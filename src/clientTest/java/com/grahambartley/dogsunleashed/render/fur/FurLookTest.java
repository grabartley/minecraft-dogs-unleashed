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
  private static final Identifier SCUFFED = Identifier.of("dogs-unleashed", "scuffed.png");
  private static final RigProfile PROFILE =
      new RigProfile(
          Map.of(
              Slot.TORSO,
              Set.of("body"),
              Slot.FUR_SHAGGY,
              Set.of("shag"),
              Slot.FUR_OVERGROWN,
              Set.of("mop")),
          Map.of(Overlay.TRIMMED, TRIMMED, Overlay.SCUFFED, SCUFFED));

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

  @ParameterizedTest(name = "{0} scuffed={1}")
  @CsvSource({
    "TRIMMED, false, 1",
    "TRIMMED, true, 2",
    "NATURAL, true, 0",
    "SHAGGY, false, 0",
    "OVERGROWN, false, 0"
  })
  @DisplayName("only a trimmed coat gets overlays, with the scuffed one on top after a botch")
  void overlaysOnlyWhenTrimmed(final DogFurStage stage, final boolean scuffed, final int count) {
    final List<Identifier> overlays = FurLook.overlays(PROFILE, stage, scuffed);
    assertEquals(List.of(TRIMMED, SCUFFED).subList(0, count), overlays);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"TRIMMED", "NATURAL", "SHAGGY", "OVERGROWN"})
  @DisplayName("a rig with no fur art draws exactly what it draws today")
  void noArtChangesNothing(final DogFurStage stage) {
    assertEquals(true, FurLook.shows(RigProfile.EMPTY, "any", stage));
    assertEquals(List.of(), FurLook.overlays(RigProfile.EMPTY, stage, true));
  }
}
