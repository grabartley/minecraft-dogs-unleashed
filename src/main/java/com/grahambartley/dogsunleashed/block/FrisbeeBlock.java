package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
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

  @Override
  public void afterBreak(
      World world,
      PlayerEntity player,
      BlockPos pos,
      BlockState state,
      @Nullable BlockEntity blockEntity,
      ItemStack tool) {
    player.incrementStat(Stats.MINED.getOrCreateStat(this));
    player.addExhaustion(0.005f);
    if (!world.isClient) {
      ItemStack drop = new ItemStack(ModItems.FRISBEE);
      if (blockEntity instanceof FrisbeeBlockEntity frisbeeBlock) {
        drop.set(
            com.grahambartley.dogsunleashed.ModComponents.FRISBEE_COLOR, frisbeeBlock.getColor());
      }
      dropStack(world, pos, drop);
    }
  }

  @Override
  public void onBlockAdded(
      BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    // Do not schedule a falling tick — frisbee stays wherever it's placed
  }

  @Override
  public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    // No falling behavior
  }
}
