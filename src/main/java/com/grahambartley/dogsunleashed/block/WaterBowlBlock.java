package com.grahambartley.dogsunleashed.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class WaterBowlBlock extends Block {

  public static final MapCodec<WaterBowlBlock> CODEC = createCodec(WaterBowlBlock::new);
  public static final IntProperty DRINKS = IntProperty.of("drinks", 0, DogBowlRules.WATER_DRINKS);

  public WaterBowlBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.stateManager.getDefaultState().with(DRINKS, 0));
  }

  @Override
  protected MapCodec<? extends Block> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(DRINKS);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext ctx) {
    return DogBowlShape.SHAPE;
  }

  public static boolean hasWater(final BlockState state) {
    return state.getBlock() instanceof WaterBowlBlock && state.get(DRINKS) > 0;
  }

  public static void drink(final World world, final BlockPos pos, final BlockState state) {
    world.setBlockState(pos, state.with(DRINKS, DogBowlRules.waterAfterDrink(state.get(DRINKS))));
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
    final int drinks = state.get(DRINKS);
    if (drinks >= DogBowlRules.WATER_DRINKS) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (stack.isOf(Items.WATER_BUCKET)) {
      return fill(
          world,
          pos,
          state,
          player,
          hand,
          stack,
          Items.BUCKET,
          DogBowlRules.WATER_DRINKS,
          SoundEvents.ITEM_BUCKET_EMPTY);
    }
    if (isWaterBottle(stack)) {
      return fill(
          world,
          pos,
          state,
          player,
          hand,
          stack,
          Items.GLASS_BOTTLE,
          DogBowlRules.waterAfterBottle(drinks),
          SoundEvents.ITEM_BOTTLE_EMPTY);
    }
    return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  private static boolean isWaterBottle(final ItemStack stack) {
    final PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
    return stack.isOf(Items.POTION) && contents != null && contents.matches(Potions.WATER);
  }

  private static ItemActionResult fill(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final PlayerEntity player,
      final Hand hand,
      final ItemStack stack,
      final Item emptied,
      final int drinks,
      final SoundEvent sound) {
    if (!world.isClient) {
      player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(emptied)));
      world.setBlockState(pos, state.with(DRINKS, drinks));
      world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
      world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
    }
    return ItemActionResult.success(world.isClient);
  }

  @Override
  protected boolean hasComparatorOutput(final BlockState state) {
    return true;
  }

  @Override
  protected int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
    return DogBowlRules.waterComparatorOutput(state.get(DRINKS));
  }
}
