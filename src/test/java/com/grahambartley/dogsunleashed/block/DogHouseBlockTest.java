package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogHouseBlockTest {

  private static final BlockPos ORIGIN = new BlockPos(4, 70, -9);

  @Test
  @DisplayName("placing the house puts the origin cell down, which is the cell that carries state")
  void defaultStateIsTheOriginCell() {
    assertEquals(
        DogHousePart.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState().get(DogHouseBlock.PART));
  }

  @Test
  @DisplayName("dog house hardness matches the dog bed, so both break in a comparable time")
  void hardnessMatchesTheDogBed() {
    assertEquals(ModBlocks.DOG_BED.getHardness(), ModBlocks.DOG_HOUSE.getHardness(), 0.0f);
  }

  /**
   * Clicking the roof, or any far corner, has to reach the same block entity as clicking the
   * doorway, or most of the house would silently do nothing.
   */
  @ParameterizedTest(name = "{0} resolves to the cell carrying the block entity")
  @EnumSource(DogHousePart.class)
  @DisplayName("every cell resolves to the origin, so the whole house is interactive")
  void everyCellResolvesToTheOrigin(final DogHousePart part) {
    final Direction facing = Direction.EAST;
    final BlockState state =
        ModBlocks.DOG_HOUSE
            .getDefaultState()
            .with(DogHouseBlock.FACING, facing)
            .with(DogHouseBlock.PART, part);
    final BlockPos cell = ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, facing));

    assertEquals(ORIGIN, DogHouseBlock.originOf(state, cell));
  }

  @ParameterizedTest(name = "facing {0} survives a state round trip")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("every cell of the house carries the facing it was placed with")
  void facingIsCarriedByEveryCell(final Direction facing) {
    final BlockState origin =
        ModBlocks.DOG_HOUSE.getDefaultState().with(DogHouseBlock.FACING, facing);

    for (final DogHousePart part : DogHousePart.values()) {
      assertEquals(facing, origin.with(DogHouseBlock.PART, part).get(DogHouseBlock.FACING));
    }
  }
}
