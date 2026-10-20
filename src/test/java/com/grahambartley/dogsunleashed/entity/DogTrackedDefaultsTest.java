package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogTrackedDefaultsTest {

  private static final Map<TrackedData<?>, Object> DEFAULTS = new HashMap<>();

  @BeforeAll
  static void collectDefaults() {
    final DataTracker.Builder builder = mock(DataTracker.Builder.class);
    doAnswer(
            call -> {
              DEFAULTS.put(call.getArgument(0), call.getArgument(1));
              return builder;
            })
        .when(builder)
        .add(any(), any());
    DogTrackedDefaults.addTo(builder);
  }

  private static Set<TrackedData<?>> declaredOnTheEntity() {
    return Arrays.stream(UnleashedDogEntity.class.getDeclaredFields())
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .filter(field -> field.getType() == TrackedData.class)
        .map(DogTrackedDefaultsTest::valueOf)
        .collect(Collectors.toSet());
  }

  private static TrackedData<?> valueOf(final Field field) {
    try {
      field.setAccessible(true);
      return (TrackedData<?>) field.get(null);
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  @DisplayName("every tracked value declared on the dog gets a default")
  void everyTrackedValueGetsADefault() {
    assertEquals(declaredOnTheEntity(), DEFAULTS.keySet());
  }

  static Stream<Arguments> keyDefaults() {
    return Stream.of(
        Arguments.of("collar colour", UnleashedDogEntity.COLLAR_COLOR, DyeColor.RED.getId()),
        Arguments.of("command", UnleashedDogEntity.COMMAND, DogCommand.FOLLOW.id()),
        Arguments.of(
            "growth stage", UnleashedDogEntity.GROWTH_STAGE, DireWolfGrowthStage.YEARLING.number()),
        Arguments.of("fur stage", UnleashedDogEntity.FUR_STAGE, DogFurStage.NATURAL.ordinal()),
        Arguments.of("scent phase", UnleashedDogEntity.SCENT_PHASE, ScentPhase.NONE.id()),
        Arguments.of("napping", UnleashedDogEntity.NAPPING, false),
        Arguments.of("bond step", UnleashedDogEntity.BOND_STEP, DogBondStep.STRANGER.ordinal()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("keyDefaults")
  @DisplayName("a new dog starts with the values it always has")
  void keyDefaultsAreUnchanged(
      final String label, final TrackedData<?> data, final Object expected) {
    assertEquals(expected, DEFAULTS.get(data), label);
  }
}
