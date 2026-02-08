package com.grahambartley.block;

import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.DogGraveBlockEntity;
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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class DogGraveBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogGraveBlock> CODEC = createCodec(DogGraveBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  // Visual size after 2x scaling: 1.125 x 1.5 x 0.375 blocks - CENTERED
  private static final VoxelShape SHAPE_NORTH =
      VoxelShapes.cuboid(0.0, 0.0, 0.3125, 1.0, 1.5, 0.6875);
  private static final VoxelShape SHAPE_SOUTH =
      VoxelShapes.cuboid(0.0, 0.0, 0.3125, 1.0, 1.5, 0.6875);
  private static final VoxelShape SHAPE_EAST =
      VoxelShapes.cuboid(0.3125, 0.0, 0.0, 0.6875, 1.5, 1.0);
  private static final VoxelShape SHAPE_WEST =
      VoxelShapes.cuboid(0.3125, 0.0, 0.0, 0.6875, 1.5, 1.0);

  public DogGraveBlock(Settings settings) {
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
    return switch (state.get(FACING)) {
      case NORTH -> SHAPE_NORTH;
      case SOUTH -> SHAPE_SOUTH;
      case EAST -> SHAPE_EAST;
      case WEST -> SHAPE_WEST;
      default -> SHAPE_NORTH;
    };
  }

  @Override
  protected VoxelShape getCollisionShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return getOutlineShape(state, world, pos, context);
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
  public void onPlaced(
      World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    final BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof DogGraveBlockEntity graveBlockEntity) {
      // Transfer dog UUID
      if (itemStack.contains(ModComponents.DOG_GRAVE_UUID)) {
        final UUID dogUuid = itemStack.get(ModComponents.DOG_GRAVE_UUID);
        if (dogUuid != null) {
          graveBlockEntity.setDogUuid(dogUuid);
        }
      }

      // Transfer dog name
      if (itemStack.contains(ModComponents.DOG_GRAVE_NAME)) {
        final String dogName = itemStack.get(ModComponents.DOG_GRAVE_NAME);
        if (dogName != null) {
          graveBlockEntity.setDogName(dogName);
        }
      }

      // Transfer flower color
      if (itemStack.contains(ModComponents.DOG_GRAVE_FLOWER_COLOR)) {
        final DyeColor flowerColor = itemStack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR);
        if (flowerColor != null) {
          graveBlockEntity.setFlowerColor(flowerColor);
        }
      }

      // Sync to clients
      if (!world.isClient) {
        world.updateListeners(pos, state, state, 3);
      }
    }

    super.onPlaced(world, pos, state, placer, itemStack);
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new DogGraveBlockEntity(pos, state);
  }

  @Override
  public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
    ItemStack stack = super.getPickStack(world, pos, state);
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof DogGraveBlockEntity graveBlockEntity) {
      addGraveDataToStack(stack, graveBlockEntity);
    }
    return stack;
  }

  @Override
  public void afterBreak(
      World world,
      PlayerEntity player,
      BlockPos pos,
      BlockState state,
      BlockEntity blockEntity,
      ItemStack tool) {
    super.afterBreak(world, player, pos, state, blockEntity, tool);
    // Item dropping is handled by loot table
  }

  private void addGraveDataToStack(ItemStack stack, DogGraveBlockEntity graveBlockEntity) {
    // Transfer UUID
    if (graveBlockEntity.getDogUuid() != null) {
      stack.set(ModComponents.DOG_GRAVE_UUID, graveBlockEntity.getDogUuid());
    }
    // Transfer name
    if (graveBlockEntity.getDogName() != null && !graveBlockEntity.getDogName().isEmpty()) {
      stack.set(ModComponents.DOG_GRAVE_NAME, graveBlockEntity.getDogName());
    }
    // Transfer flower color
    stack.set(ModComponents.DOG_GRAVE_FLOWER_COLOR, graveBlockEntity.getFlowerColor());
  }
}
