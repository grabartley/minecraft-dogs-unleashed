package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DogBedBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogBedBlock> CODEC = createCodec(DogBedBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);

  public static void setPendingAssignment(UUID playerUuid, UUID dogUuid) {
    DogSleepSpotAssignment.setPendingAssignment(playerUuid, dogUuid);
  }

  public static UUID consumePendingAssignment(UUID playerUuid) {
    return DogSleepSpotAssignment.consumePendingAssignment(playerUuid);
  }

  public static void clearPendingAssignments() {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  public DogBedBlock(Settings settings) {
    super(settings);
    this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return SHAPE;
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
  }

  @Override
  protected BlockState rotate(BlockState state, BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(BlockState state, BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new DogBedBlockEntity(pos, state);
  }

  @Override
  public void onPlaced(
      World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    if (itemStack.contains(ModComponents.DOG_BED_COLOR)) {
      final BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof DogBedBlockEntity dogBedBlockEntity) {
        final DyeColor color = itemStack.get(ModComponents.DOG_BED_COLOR);
        if (color != null) {
          dogBedBlockEntity.setColor(color);
        }
      }
    }

    super.onPlaced(world, pos, state, placer, itemStack);
  }

  @Override
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }

    final BlockEntity blockEntity = world.getBlockEntity(pos);
    if (!(blockEntity instanceof DogBedBlockEntity dogBedBlockEntity)) {
      return ActionResult.PASS;
    }

    final ItemStack heldStack = player.getStackInHand(Hand.MAIN_HAND);
    if (heldStack.getItem() instanceof DyeItem dyeItem) {
      dogBedBlockEntity.setColor(dyeItem.getColor());
      heldStack.decrementUnlessCreative(1, player);
      return ActionResult.SUCCESS;
    }

    return DogSleepSpotAssignment.handleUse(
        world, pos, player, dogBedBlockEntity, state.getBlock().getTranslationKey());
  }

  @Override
  protected void onStateReplaced(
      BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (!state.isOf(newState.getBlock())) {
      final BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof DogBedBlockEntity dogBedBlockEntity) {
        dogBedBlockEntity.clearAssignedDog(world);
      }
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  @Override
  protected float calcBlockBreakingDelta(
      BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
    final float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
    final ItemStack heldStack = player.getMainHandStack();

    if (heldStack.getItem() instanceof ShearsItem) {
      return baseSpeed * 5.0f;
    }

    return baseSpeed;
  }
}
