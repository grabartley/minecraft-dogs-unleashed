package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class RidingViewTest {

  @ParameterizedTest
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"DIRE_WOLF"},
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("breeds that never grow big enough show nothing")
  void otherBreeds(final UnleashedDogBreed breed) {
    assertEquals(RidingView.NONE, RidingView.of(breed, 4, 80));
  }

  @ParameterizedTest
  @EnumSource(
      value = DireWolfGrowthStage.class,
      names = {"PUP", "YEARLING", "ADULT"})
  @DisplayName("young dire wolves are too young")
  void youngDireWolves(final DireWolfGrowthStage stage) {
    assertEquals(
        RidingView.TOO_YOUNG, RidingView.of(UnleashedDogBreed.DIRE_WOLF, stage.number(), 80));
  }

  @ParameterizedTest
  @EnumSource(
      value = DireWolfGrowthStage.class,
      names = {"PRIME", "ALPHA"})
  @DisplayName("primes and alphas are ready with their stamina")
  void grownDireWolves(final DireWolfGrowthStage stage) {
    assertEquals(
        new RidingView(RidingView.Kind.READY, 65),
        RidingView.of(UnleashedDogBreed.DIRE_WOLF, stage.number(), 65));
  }

  @Test
  @DisplayName("stamina shows between 0 and 100 percent")
  void clamps() {
    assertEquals(100, RidingView.of(UnleashedDogBreed.DIRE_WOLF, 5, 140).staminaPercent());
    assertEquals(0, RidingView.of(UnleashedDogBreed.DIRE_WOLF, 5, -3).staminaPercent());
  }

  static Stream<RidingView> views() {
    return Stream.of(
        RidingView.NONE, RidingView.TOO_YOUNG, new RidingView(RidingView.Kind.READY, 42));
  }

  @ParameterizedTest
  @MethodSource("views")
  @DisplayName("the view survives a write and read")
  void roundTrips(final RidingView view) {
    final RegistryByteBuf buf = newBuf();
    view.write(buf);
    assertEquals(view, RidingView.read(buf));
  }
}
