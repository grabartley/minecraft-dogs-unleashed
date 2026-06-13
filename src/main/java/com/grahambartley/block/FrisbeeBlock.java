package com.grahambartley.block;

import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class FrisbeeBlock extends FallingBlock implements BlockEntityProvider {

  public static final MapCodec<FrisbeeBlock> CODEC = createCodec(FrisbeeBlock::new);
  private static final VoxelShape SHAPE =
      VoxelShapes.cuboid(0.125, 0.0, 0.125, 0.875, 0.1875, 0.875);

  public FrisbeeBlock(Settings settings) {
    super(settings);
  }

  @Override
  public MapCodec<FrisbeeBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return SHAPE;
  }

  @Override
  public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new FrisbeeBlockEntity(pos, state);
  }
}
