package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModPointsOfInterestTest {

  static Stream<Arguments> bowls() {
    return Stream.of(
        Arguments.of(ModBlocks.FOOD_BOWL, ModPointsOfInterest.FOOD_BOWL),
        Arguments.of(ModBlocks.WATER_BOWL, ModPointsOfInterest.WATER_BOWL));
  }

  @ParameterizedTest(name = "every state of {0} is a {1} point of interest")
  @MethodSource("bowls")
  @DisplayName("dogs find every bowl, however full, through its point of interest")
  void everyBowlStateIsAPointOfInterest(
      final Block block, final RegistryKey<PointOfInterestType> key) {
    for (final BlockState state : block.getStateManager().getStates()) {
      assertTrue(
          PointOfInterestTypes.getTypeForState(state)
              .filter(entry -> entry.matchesKey(key))
              .isPresent(),
          state + " is not registered as " + key.getValue());
    }
  }
}
