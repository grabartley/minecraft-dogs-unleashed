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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.loading.object.BakedAnimations;

class RigSleepClipsTest {

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
  void variants(
      final Map<Identifier, BakedAnimations> loadedAnimations, final List<String> expected) {
    assertEquals(expected, new RigSleepClips().variantsOf(loadedAnimations, RIG));
  }

  @ParameterizedTest(name = "reloaded {0}")
  @CsvSource({"false", "true"})
  @DisplayName("the same file is served from memory, a reloaded file is read again")
  void reload(final boolean reloaded) {
    final RigSleepClips rigClips = new RigSleepClips();
    final BakedAnimations first = clips("sleep");
    final List<String> before = rigClips.variantsOf(Map.of(RIG, first), RIG);
    final BakedAnimations next = reloaded ? clips("sleep", "sleep_2") : first;
    final List<String> after = rigClips.variantsOf(Map.of(RIG, next), RIG);
    if (reloaded) {
      assertEquals(List.of("sleep", "sleep_2"), after);
    } else {
      assertSame(before, after);
    }
  }

  private static BakedAnimations clips(final String... names) {
    final Map<String, Animation> animations = new HashMap<>();
    Arrays.stream(names).forEach(name -> animations.put(name, null));
    return new BakedAnimations(animations);
  }
}
