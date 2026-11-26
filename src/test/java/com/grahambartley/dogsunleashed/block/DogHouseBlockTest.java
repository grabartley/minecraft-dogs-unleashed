package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("partsAndNavigation")
  @DisplayName("no mob plans a path through any cell of the house, so dogs walk round to the door")
  void noMobPathsThroughTheHouse(final DogHousePart part, final NavigationType type) {
    final BlockState state = ModBlocks.DOG_HOUSE.getDefaultState().with(DogHouseBlock.PART, part);
    assertFalse(state.canPathfindThrough(type));
  }

  static Stream<Arguments> partsAndNavigation() {
    return Arrays.stream(DogHousePart.values())
        .flatMap(
            part -> Arrays.stream(NavigationType.values()).map(type -> Arguments.of(part, type)));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"oak", "spruce", "acacia", "crimson", "bamboo"})
  @DisplayName("the block reports the wood its block entity was built from")
  void woodComesFromTheBlockEntity(final String name) {
    final WoodType wood =
        WoodType.stream().filter(type -> type.name().equals(name)).findFirst().orElseThrow();
    final BlockPos origin = new BlockPos(5, 64, 5);
    final DogHouseBlockEntity entity =
        new DogHouseBlockEntity(origin, ModBlocks.DOG_HOUSE.getDefaultState());
    entity.setWood(wood);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockEntity(origin)).thenReturn(entity);

    assertSame(
        wood,
        ((WoodenBlock) ModBlocks.DOG_HOUSE)
            .woodAt(world, origin, ModBlocks.DOG_HOUSE.getDefaultState()));
  }

  @Test
  @DisplayName("with no block entity to ask, the block counts as oak")
  void missingBlockEntityIsOak() {
    final BlockPos pos = new BlockPos(5, 64, 5);
    assertSame(
        WoodType.OAK,
        ((WoodenBlock) ModBlocks.DOG_HOUSE)
            .woodAt(mock(BlockView.class), pos, ModBlocks.DOG_HOUSE.getDefaultState()));
  }
}
