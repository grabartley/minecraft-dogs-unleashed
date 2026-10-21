package com.grahambartley.dogsunleashed.entity.herding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SheepHerdingTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("only the border collie sends sheep running")
  void onlyTheBorderCollieHerds(final UnleashedDogBreed breed) {
    assertEquals(breed == UnleashedDogBreed.BORDER_COLLIE, SheepHerding.herds(breed));
  }

  @Test
  @DisplayName("anything that is not a dog never herds sheep")
  void nonDogsNeverHerd() {
    assertFalse(SheepHerding.herdsSheep(null));
  }

  @Test
  @DisplayName("sheep keep a short berth and bolt faster when the collie is close")
  void fleeTuningIsSane() {
    assertTrue(SheepHerding.FLEE_DISTANCE > 0.0f);
    assertTrue(SheepHerding.FAST_SPEED > SheepHerding.SLOW_SPEED);
  }
}
