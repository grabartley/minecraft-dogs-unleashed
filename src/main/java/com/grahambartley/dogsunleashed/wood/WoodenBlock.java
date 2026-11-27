package com.grahambartley.dogsunleashed.wood;

import net.minecraft.block.BlockState;
import net.minecraft.block.WoodType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface WoodenBlock {

  WoodType woodAt(BlockView world, BlockPos pos, BlockState state);
}
