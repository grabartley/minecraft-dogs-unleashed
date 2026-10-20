package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModBlockEntitiesTest {

  static Stream<Arguments> blockEntities() {
    return Stream.of(
        Arguments.of("dog_grave", ModBlockEntities.DOG_GRAVE, ModBlocks.DOG_GRAVE),
        Arguments.of("food_bowl", ModBlockEntities.FOOD_BOWL, ModBlocks.FOOD_BOWL),
        Arguments.of("dog_statue", ModBlockEntities.DOG_STATUE, ModStatueBlocks.STONE),
        Arguments.of(
            "dog_statue", ModBlockEntities.DOG_STATUE, ModStatueBlocks.WAXED_OXIDIZED_COPPER));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0} is registered for its block")
  @MethodSource("blockEntities")
  @DisplayName("each block entity type is registered under its id and attaches to its block")
  void blockEntityIsRegisteredForItsBlock(
      final String path, final BlockEntityType<?> type, final Block block) {
    assertSame(type, Registries.BLOCK_ENTITY_TYPE.get(Identifier.of(DogsUnleashed.MOD_ID, path)));
    for (final var state : block.getStateManager().getStates()) {
      assertTrue(type.supports(state), path + " does not support " + state);
    }
  }
}
