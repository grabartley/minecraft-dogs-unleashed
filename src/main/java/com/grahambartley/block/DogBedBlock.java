package com.grahambartley.block;

import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import com.mojang.serialization.MapCodec;
import java.util.Comparator;
import java.util.List;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DogBedBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  public static final MapCodec<DogBedBlock> CODEC = createCodec(DogBedBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);

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
    return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
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
    super.onPlaced(world, pos, state, placer, itemStack);

    if (world.isClient) {
      return;
    }

    if (itemStack.contains(ModComponents.DOG_BED_COLOR)) {
      final BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof DogBedBlockEntity dogBedBlockEntity) {
        final DyeColor color = itemStack.get(ModComponents.DOG_BED_COLOR);
        if (color != null) {
          dogBedBlockEntity.setColor(color);
        }
      }
    }
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

    if (player.isSneaking() && heldStack.isEmpty()) {
      if (dogBedBlockEntity.hasAssignedDog()) {
        dogBedBlockEntity.clearAssignedDog(world);
        return ActionResult.SUCCESS;
      }

      final UnleashedDogEntity nearestDog = findNearestOwnedDog(world, player, pos);
      if (nearestDog != null) {
        if (nearestDog.hasAssignedBed()) {
          nearestDog.clearAssignedBed();
        }
        dogBedBlockEntity.setAssignedDog(nearestDog);
        nearestDog.setAssignedBedPos(pos);
        return ActionResult.SUCCESS;
      }
      return ActionResult.PASS;
    }

    if (dogBedBlockEntity.hasAssignedDog()) {
      final UnleashedDogEntity dog = dogBedBlockEntity.getAssignedDog(world);
      if (dog != null && dog.isOwner(player)) {
        dog.commandToSleep(pos);
        return ActionResult.SUCCESS;
      }
    }

    return ActionResult.PASS;
  }

  private UnleashedDogEntity findNearestOwnedDog(
      World world, PlayerEntity player, BlockPos bedPos) {
    final Box searchBox = new Box(bedPos).expand(16.0);
    final List<UnleashedDogEntity> dogs =
        world.getEntitiesByClass(UnleashedDogEntity.class, searchBox, dog -> dog.isOwner(player));

    return dogs.stream()
        .min(Comparator.comparingDouble(dog -> dog.squaredDistanceTo(player)))
        .orElse(null);
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
}
