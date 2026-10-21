package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.loading.object.BakedAnimations;

class DogAnimationControllersTest {

  private static final Identifier RIG =
      Identifier.of("dogs-unleashed", "animations/beagle.animation.json");
  private static final Identifier OTHER_RIG =
      Identifier.of("dogs-unleashed", "animations/husky.animation.json");

  static Stream<Arguments> loaded() {
    return Stream.of(
        Arguments.of(Map.of(), List.of()),
        Arguments.of(Map.of(OTHER_RIG, clips("sleep", "sleep_2")), List.of()),
        Arguments.of(Map.of(RIG, clips("idle", "walk")), List.of()),
        Arguments.of(Map.of(RIG, clips("idle", "sleep", "walk")), List.of("sleep")),
        Arguments.of(
            Map.of(RIG, clips("sleep_3", "sleep", "idle", "sleep_2")),
            List.of("sleep", "sleep_2", "sleep_3")));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("loaded")
  @DisplayName("a rig's sleep variants come from its own loaded animation file")
  void sleepVariants(
      final Map<Identifier, BakedAnimations> loadedAnimations, final List<String> expected) {
    assertEquals(expected, DogAnimationControllers.sleepVariantsOf(loadedAnimations, RIG));
  }

  @Test
  @DisplayName("a reloaded animation file is read again instead of served from the old one")
  void reloadReadsNewClips() {
    final BakedAnimations first = clips("sleep");
    final List<String> before = DogAnimationControllers.sleepVariantsOf(Map.of(RIG, first), RIG);
    assertSame(before, DogAnimationControllers.sleepVariantsOf(Map.of(RIG, first), RIG));
    assertEquals(
        List.of("sleep", "sleep_2"),
        DogAnimationControllers.sleepVariantsOf(Map.of(RIG, clips("sleep", "sleep_2")), RIG));
  }

  private static BakedAnimations clips(final String... names) {
    final Map<String, Animation> animations = new HashMap<>();
    Arrays.stream(names).forEach(name -> animations.put(name, null));
    return new BakedAnimations(animations);
  }
}
