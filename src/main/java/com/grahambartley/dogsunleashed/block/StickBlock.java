package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.entity.StickBlockEntity;
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

public class StickBlock extends FallingBlock implements BlockEntityProvider {

  public static final MapCodec<StickBlock> CODEC = createCodec(StickBlock::new);
  private static final VoxelShape SHAPE =
      VoxelShapes.cuboid(0.0625, 0.0, 0.375, 0.9375, 0.125, 0.625);

  public StickBlock(Settings settings) {
    super(settings);
  }

  @Override
  public MapCodec<StickBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return SHAPE;
  }

  @Override
  public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new StickBlockEntity(pos, state);
  }
}
