package com.grahambartley.block;

import com.grahambartley.block.entity.TennisBallBlockEntity;
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

public class TennisBallBlock extends FallingBlock implements BlockEntityProvider {

  public static final MapCodec<TennisBallBlock> CODEC = createCodec(TennisBallBlock::new);
  private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.25, 0.0, 0.25, 0.75, 0.5, 0.75);

  public TennisBallBlock(Settings settings) {
    super(settings);
  }

  @Override
  public MapCodec<TennisBallBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return SHAPE;
  }

  @Override
  public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new TennisBallBlockEntity(pos, state);
  }
}
