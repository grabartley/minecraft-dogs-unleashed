package com.grahambartley.dogsunleashed.block;

import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;

public final class DogBowlShape {

  public static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 5.0, 14.0);

  private DogBowlShape() {}
}
