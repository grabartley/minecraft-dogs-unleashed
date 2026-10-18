package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import net.minecraft.block.BlockState;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class DogHouseTestHelper {

  private DogHouseTestHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void placeHouse(
      final TestContext context, final BlockPos relOrigin, final Direction facing) {
    final BlockState origin =
        ModBlocks.DOG_HOUSE
            .getDefaultState()
            .with(DogHouseBlock.FACING, facing)
            .with(DogHouseBlock.PART, DogHousePart.ORIGIN);
    for (final DogHousePart part : DogHousePart.values()) {
      context.setBlockState(
          relOrigin.add(DogHouseLayout.offsetFromOrigin(part, facing)),
          origin.with(DogHouseBlock.PART, part));
    }
  }
}
