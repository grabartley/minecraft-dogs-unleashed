package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
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
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * A raycast only ever tests a block's shape while the ray is inside that block's own cell, so a
 * model taller than its cell can never be aimed at above the cell line. The headstone is therefore
 * sized to stand within one block, and its hitbox is that whole block: every part of the stone is
 * targetable, and a lightning rod placed on top lands in the cell the stone's tip pokes into, which
 * buries the rod's base in the stone.
 */
public class DogGraveBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogGraveBlock> CODEC = createCodec(DogGraveBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  private static final VoxelShape SHAPE_NORTH_SOUTH =
      VoxelShapes.cuboid(0.0, 0.0, 0.3125, 1.0, 1.0, 0.6875);
  private static final VoxelShape SHAPE_EAST_WEST =
      VoxelShapes.cuboid(0.3125, 0.0, 0.0, 0.6875, 1.0, 1.0);

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
      case EAST, WEST -> SHAPE_EAST_WEST;
      default -> SHAPE_NORTH_SOUTH;
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
  public void onPlaced(
      World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    final BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof DogGraveBlockEntity graveBlockEntity) {
      if (itemStack.contains(ModComponents.DOG_GRAVE_UUID)) {
        final UUID dogUuid = itemStack.get(ModComponents.DOG_GRAVE_UUID);
        if (dogUuid != null) {
          graveBlockEntity.setDogUuid(dogUuid);
        }
      }

      if (itemStack.contains(ModComponents.DOG_GRAVE_NAME)) {
        final String dogName = itemStack.get(ModComponents.DOG_GRAVE_NAME);
        if (dogName != null) {
          graveBlockEntity.setDogName(dogName);
        }
      }

      if (itemStack.contains(ModComponents.DOG_GRAVE_FLOWER_COLOR)) {
        final DyeColor flowerColor = itemStack.get(ModComponents.DOG_GRAVE_FLOWER_COLOR);
        if (flowerColor != null) {
          graveBlockEntity.setFlowerColor(flowerColor);
        }
      }

      graveBlockEntity.markDirty();

      if (!world.isClient) {
        world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
      }
    }

    super.onPlaced(world, pos, state, placer, itemStack);
  }

  /**
   * The grave holds a single Totem of Undying, the offering the resurrection ritual consumes. A
   * totem in hand installs one and an empty hand takes it back; every other item passes straight
   * through, so the ritual's own lightning rod can still be placed on a grave already holding a
   * totem.
   */
  @Override
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }

    final BlockEntity blockEntity = world.getBlockEntity(pos);
    if (!(blockEntity instanceof DogGraveBlockEntity graveBlockEntity)) {
      return ActionResult.PASS;
    }

    final ItemStack heldStack = player.getStackInHand(Hand.MAIN_HAND);

    if (!graveBlockEntity.hasTotem() && heldStack.isOf(Items.TOTEM_OF_UNDYING)) {
      graveBlockEntity.installTotem(player.getUuid());
      heldStack.decrementUnlessCreative(1, player);
      player.sendMessage(
          Text.translatable(
              "block.dogs-unleashed.dog_grave.totem_installed", graveBlockEntity.getDogName()),
          true);
      return ActionResult.SUCCESS;
    }

    if (graveBlockEntity.hasTotem() && heldStack.isEmpty()) {
      graveBlockEntity.clearTotem();
      player.giveItemStack(new ItemStack(Items.TOTEM_OF_UNDYING));
      player.sendMessage(
          Text.translatable(
              "block.dogs-unleashed.dog_grave.totem_removed", graveBlockEntity.getDogName()),
          true);
      return ActionResult.SUCCESS;
    }

    return ActionResult.PASS;
  }

  /** An installed totem belongs to the player, so it survives the grave however the grave goes. */
  @Override
  protected void onStateReplaced(
      BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (!world.isClient
        && !state.isOf(newState.getBlock())
        && world.getBlockEntity(pos) instanceof DogGraveBlockEntity graveBlockEntity
        && graveBlockEntity.hasTotem()) {
      graveBlockEntity.clearTotem();
      dropStack(world, pos, new ItemStack(Items.TOTEM_OF_UNDYING));
    }
    super.onStateReplaced(state, world, pos, newState, moved);
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
  public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    if (!world.isClient
        && !player.isCreative()
        && world.getBlockEntity(pos) instanceof DogGraveBlockEntity graveBlockEntity
        && player.getMainHandStack().getItem() instanceof PickaxeItem) {
      final ItemStack stack = new ItemStack(this);
      addGraveDataToStack(stack, graveBlockEntity);
      dropStack(world, pos, stack);
    }
    return super.onBreak(world, pos, state, player);
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
    player.addExhaustion(0.005F);
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
