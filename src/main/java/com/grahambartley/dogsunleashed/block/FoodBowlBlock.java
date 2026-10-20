package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.entity.FoodBowlBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FoodBowlBlock extends BlockWithEntity {

  public static final MapCodec<FoodBowlBlock> CODEC = createCodec(FoodBowlBlock::new);
  public static final EnumProperty<BowlFill> FILL = EnumProperty.of("fill", BowlFill.class);
  static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 5.0, 14.0);

  public FoodBowlBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.stateManager.getDefaultState().with(FILL, BowlFill.EMPTY));
  }

  @Override
  protected MapCodec<? extends BlockWithEntity> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FILL);
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext ctx) {
    return SHAPE;
  }

  @Override
  protected ItemActionResult onUseWithItem(
      final ItemStack stack,
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockHitResult hit) {
    if (!(world.getBlockEntity(pos) instanceof FoodBowlBlockEntity bowl)) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (world.isClient) {
      return bowl.isValid(0, stack)
          ? ItemActionResult.SUCCESS
          : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    final int moved = bowl.addFood(stack);
    if (moved == 0) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    stack.decrementUnlessCreative(moved, player);
    world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
    return ItemActionResult.CONSUME;
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    ItemScatterer.onStateReplaced(state, newState, world, pos);
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  @Override
  protected boolean hasComparatorOutput(final BlockState state) {
    return true;
  }

  @Override
  protected int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
    return world.getBlockEntity(pos) instanceof FoodBowlBlockEntity bowl
        ? DogBowlRules.foodComparatorOutput(bowl.foodCount())
        : 0;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new FoodBowlBlockEntity(pos, state);
  }
}
