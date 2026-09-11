package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class DogHouseBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogHouseBlock> CODEC = createCodec(DogHouseBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<DogHousePart> PART = EnumProperty.of("part", DogHousePart.class);

  public DogHouseBlock(Settings settings) {
    super(settings);
    this.setDefaultState(
        this.stateManager
            .getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(PART, DogHousePart.ORIGIN));
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, PART);
  }

  public static BlockPos originOf(final BlockState state, final BlockPos pos) {
    return DogHouseLayout.originOf(state.get(PART), state.get(FACING), pos);
  }

  @Override
  @Nullable
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    final Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
    final BlockPos origin = ctx.getBlockPos();
    final World world = ctx.getWorld();
    for (final BlockPos cell : DogHouseLayout.cellsOf(origin, facing)) {
      if (cell.getY() >= world.getTopY() || !world.getBlockState(cell).canReplace(ctx)) {
        return null;
      }
    }
    return this.getDefaultState().with(FACING, facing).with(PART, DogHousePart.ORIGIN);
  }

  @Override
  public void onPlaced(
      World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    final Direction facing = state.get(FACING);
    for (final DogHousePart part : DogHousePart.values()) {
      if (part == DogHousePart.ORIGIN) {
        continue;
      }
      world.setBlockState(
          pos.add(DogHouseLayout.offsetFromOrigin(part, facing)),
          state.with(PART, part),
          Block.NOTIFY_ALL | Block.FORCE_STATE);
    }
    super.onPlaced(world, pos, state, placer, itemStack);
  }

  @Override
  protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
    final BlockPos origin = originOf(state, pos);
    if (origin.equals(pos)) {
      return super.canPlaceAt(state, world, pos);
    }
    final BlockState originState = world.getBlockState(origin);
    return originState.isOf(this) && originState.get(PART) == DogHousePart.ORIGIN;
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      BlockState state,
      Direction direction,
      BlockState neighborState,
      WorldAccess world,
      BlockPos pos,
      BlockPos neighborPos) {
    return canPlaceAt(state, world, pos) ? state : Blocks.AIR.getDefaultState();
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return VoxelShapes.fullCube();
  }

  @Override
  protected VoxelShape getCollisionShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return DogHouseShape.collision(state.get(PART), state.get(FACING));
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
  @Nullable
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return state.get(PART) == DogHousePart.ORIGIN ? new DogHouseBlockEntity(pos, state) : null;
  }

  @Override
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }

    final BlockPos origin = originOf(state, pos);
    if (!(world.getBlockEntity(origin) instanceof DogHouseBlockEntity houseBlockEntity)) {
      return ActionResult.PASS;
    }

    final ItemStack heldStack = player.getStackInHand(Hand.MAIN_HAND);
    if (heldStack.getItem() instanceof DyeItem dyeItem) {
      houseBlockEntity.setColor(dyeItem.getColor());
      heldStack.decrementUnlessCreative(1, player);
      return ActionResult.SUCCESS;
    }

    return DogSleepSpotAssignment.handleUse(
        world, origin, player, houseBlockEntity, state.getBlock().getTranslationKey());
  }

  @Override
  public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
    final ItemStack stack = super.getPickStack(world, pos, state);
    if (world.getBlockEntity(originOf(state, pos))
        instanceof DogHouseBlockEntity houseBlockEntity) {
      stack.set(ModComponents.DOG_HOUSE_COLOR, houseBlockEntity.getColor());
    }
    return stack;
  }

  @Override
  public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    if (!world.isClient && player.isCreative()) {
      final BlockPos origin = originOf(state, pos);
      if (world.getBlockState(origin).isOf(this)) {
        world.setBlockState(
            origin, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
      }
    }
    return super.onBreak(world, pos, state, player);
  }

  @Override
  protected void onStateReplaced(
      BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (!state.isOf(newState.getBlock())) {
      dropFromOrigin(world, pos, state);
      if (world.getBlockEntity(pos) instanceof DogHouseBlockEntity houseBlockEntity) {
        houseBlockEntity.clearAssignedDog(world);
      }
      clearSiblings(world, pos, state);
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  private void dropFromOrigin(final World world, final BlockPos pos, final BlockState state) {
    if (state.get(PART) == DogHousePart.ORIGIN) {
      return;
    }
    final BlockPos origin = originOf(state, pos);
    if (world.getBlockState(origin).isOf(this)) {
      world.breakBlock(origin, true);
    }
  }

  private void clearSiblings(final World world, final BlockPos pos, final BlockState state) {
    final BlockPos origin = originOf(state, pos);
    for (final BlockPos cell : DogHouseLayout.cellsOf(origin, state.get(FACING))) {
      if (cell.equals(pos)) {
        continue;
      }
      if (world.getBlockState(cell).isOf(this)) {
        world.setBlockState(cell, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      }
    }
  }
}
