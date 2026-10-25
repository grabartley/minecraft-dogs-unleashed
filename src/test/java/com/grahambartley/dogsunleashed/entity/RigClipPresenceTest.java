package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.loading.object.BakedAnimations;

class RigClipPresenceTest {

  private static final Identifier RIG =
      Identifier.of("dogs-unleashed", "animations/husky.animation.json");
  private static final Identifier OTHER_RIG =
      Identifier.of("dogs-unleashed", "animations/direwolf.animation.json");

  @ParameterizedTest(name = "{0} has {1}: {2}")
  @CsvSource({
    "husky, run, false",
    "husky, walk, true",
    "direwolf, run, false",
    "nothing, walk, false"
  })
  @DisplayName("a clip counts only when the rig's own loaded file has it")
  void has(final String loaded, final String clip, final boolean expected) {
    final Map<Identifier, BakedAnimations> files = new HashMap<>();
    if (loaded.equals("husky")) {
      files.put(RIG, clips("idle", "walk", "sit"));
    } else if (loaded.equals("direwolf")) {
      files.put(OTHER_RIG, clips("idle", "walk", "run"));
    }
    assertEquals(expected, RigClipPresence.has(files, RIG, clip));
  }

  @Test
  @DisplayName("find returns the rig's own baked clip")
  void findsRigClip() {
    final Animation tilt =
        new Animation(
            "head_tilt", 1.0, Animation.LoopType.HOLD_ON_LAST_FRAME, new BoneAnimation[0], null);
    final Map<Identifier, BakedAnimations> files = new HashMap<>();
    files.put(RIG, new BakedAnimations(Map.of("head_tilt", tilt)));
    files.put(OTHER_RIG, clips("idle"));
    assertSame(tilt, RigClipPresence.find(files, RIG, "head_tilt"));
  }

  @Test
  @DisplayName("find returns nothing when the rig lacks the clip or is not loaded")
  void findsNothing() {
    final Map<Identifier, BakedAnimations> files = new HashMap<>();
    files.put(OTHER_RIG, clips("head_tilt"));
    assertNull(RigClipPresence.find(files, RIG, "head_tilt"));
    files.put(RIG, clips("idle"));
    assertNull(RigClipPresence.find(files, RIG, "head_tilt"));
  }

  private static BakedAnimations clips(final String... names) {
    final Map<String, Animation> animations = new HashMap<>();
    Arrays.stream(names).forEach(name -> animations.put(name, null));
    return new BakedAnimations(animations);
  }
}
