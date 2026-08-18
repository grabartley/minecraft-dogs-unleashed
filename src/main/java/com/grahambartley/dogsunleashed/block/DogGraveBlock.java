package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
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
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * The headstone stands a block and a half tall, and a raycast only tests a shape inside its own
 * block cell, so the grave occupies two cells the way a door does: this block with {@code
 * HALF=LOWER} carrying the block entity, and an {@code UPPER} half giving the top of the stone a
 * real hitbox. Graves from before the upper half existed heal themselves on load, see {@link
 * DogGraveBlockEntity#setWorld}.
 */
public class DogGraveBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogGraveBlock> CODEC = createCodec(DogGraveBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
  // Visual size after 2x scaling: 1.125 x 1.5 x 0.375 blocks - CENTERED
  private static final VoxelShape LOWER_SHAPE_NORTH_SOUTH =
      VoxelShapes.cuboid(0.0, 0.0, 0.3125, 1.0, 1.0, 0.6875);
  private static final VoxelShape LOWER_SHAPE_EAST_WEST =
      VoxelShapes.cuboid(0.3125, 0.0, 0.0, 0.6875, 1.0, 1.0);
  private static final VoxelShape UPPER_SHAPE_NORTH_SOUTH =
      VoxelShapes.cuboid(0.0, 0.0, 0.3125, 1.0, 0.5, 0.6875);
  private static final VoxelShape UPPER_SHAPE_EAST_WEST =
      VoxelShapes.cuboid(0.3125, 0.0, 0.0, 0.6875, 0.5, 1.0);

  public DogGraveBlock(Settings settings) {
    super(settings);
    this.setDefaultState(
        this.stateManager
            .getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(HALF, DoubleBlockHalf.LOWER));
  }

  /** The cell holding the block entity, whichever half was targeted. */
  public static BlockPos basePosOf(final BlockState state, final BlockPos pos) {
    return state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    final boolean upper = state.get(HALF) == DoubleBlockHalf.UPPER;
    return switch (state.get(FACING)) {
      case EAST, WEST -> upper ? UPPER_SHAPE_EAST_WEST : LOWER_SHAPE_EAST_WEST;
      default -> upper ? UPPER_SHAPE_NORTH_SOUTH : LOWER_SHAPE_NORTH_SOUTH;
    };
  }

  @Override
  protected VoxelShape getCollisionShape(
      BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return getOutlineShape(state, world, pos, context);
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, HALF);
  }

  @Override
  public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
    final BlockPos pos = ctx.getBlockPos();
    if (pos.getY() >= ctx.getWorld().getTopY() - 1
        || !ctx.getWorld().getBlockState(pos.up()).canReplace(ctx)) {
      return null;
    }
    return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
  }

  /**
   * The upper half exists only on top of its base; the base survives alone, so graves from before
   * the upper half existed keep standing until they heal.
   */
  @Override
  protected BlockState getStateForNeighborUpdate(
      BlockState state,
      Direction direction,
      BlockState neighborState,
      WorldAccess world,
      BlockPos pos,
      BlockPos neighborPos) {
    if (state.get(HALF) == DoubleBlockHalf.UPPER
        && direction == Direction.DOWN
        && (!neighborState.isOf(this) || neighborState.get(HALF) != DoubleBlockHalf.LOWER)) {
      return Blocks.AIR.getDefaultState();
    }
    return super.getStateForNeighborUpdate(
        state, direction, neighborState, world, pos, neighborPos);
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

    world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    super.onPlaced(world, pos, state, placer, itemStack);
  }

  /** Places both halves of a grave, the shape every non-item spawn path should produce. */
  public static void placeGrave(final World world, final BlockPos basePos, final BlockState base) {
    world.setBlockState(basePos, base.with(HALF, DoubleBlockHalf.LOWER));
    world.setBlockState(basePos.up(), base.with(HALF, DoubleBlockHalf.UPPER));
  }

  /**
   * The grave holds a single Totem of Undying, the offering the resurrection ritual consumes. Any
   * right-click on a grave that already holds one takes it back, so the totem is never trapped.
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

    if (graveBlockEntity.hasTotem()) {
      graveBlockEntity.clearTotem();
      player.giveItemStack(new ItemStack(Items.TOTEM_OF_UNDYING));
      player.sendMessage(
          Text.translatable(
              "block.dogs-unleashed.dog_grave.totem_removed", graveBlockEntity.getDogName()),
          true);
      return ActionResult.SUCCESS;
    }

    final ItemStack heldStack = player.getStackInHand(Hand.MAIN_HAND);
    if (heldStack.isOf(Items.TOTEM_OF_UNDYING)) {
      graveBlockEntity.installTotem(player.getUuid());
      heldStack.decrementUnlessCreative(1, player);
      player.sendMessage(
          Text.translatable(
              "block.dogs-unleashed.dog_grave.totem_installed", graveBlockEntity.getDogName()),
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
  public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return state.get(HALF) == DoubleBlockHalf.LOWER ? new DogGraveBlockEntity(pos, state) : null;
  }

  @Override
  public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
    ItemStack stack = super.getPickStack(world, pos, state);
    BlockEntity blockEntity = world.getBlockEntity(basePosOf(state, pos));
    if (blockEntity instanceof DogGraveBlockEntity graveBlockEntity) {
      addGraveDataToStack(stack, graveBlockEntity);
    }
    return stack;
  }

  /** Breaking either half takes the whole grave, preserving its data through the base's entity. */
  @Override
  public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    if (!world.isClient) {
      final BlockPos basePos = basePosOf(state, pos);
      if (!player.isCreative()
          && world.getBlockEntity(basePos) instanceof DogGraveBlockEntity graveBlockEntity
          && player.getMainHandStack().getItem() instanceof PickaxeItem) {
        final ItemStack stack = new ItemStack(this);
        addGraveDataToStack(stack, graveBlockEntity);
        dropStack(world, basePos, stack);
      }
      final BlockPos otherPos = state.get(HALF) == DoubleBlockHalf.UPPER ? basePos : pos.up();
      final BlockState other = world.getBlockState(otherPos);
      if (other.isOf(this) && other.get(HALF) != state.get(HALF)) {
        world.setBlockState(otherPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      }
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
