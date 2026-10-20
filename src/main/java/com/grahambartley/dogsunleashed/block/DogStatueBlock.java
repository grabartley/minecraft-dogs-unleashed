package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DogStatueBlock extends HorizontalFacingBlock implements BlockEntityProvider {

  private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

  private final StatueMaterial material;

  public DogStatueBlock(final StatueMaterial material, final Settings settings) {
    super(settings);
    this.material = material;
    this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
  }

  public StatueMaterial material() {
    return this.material;
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return createCodec(settings -> new DogStatueBlock(this.material, settings));
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext ctx) {
    return SHAPE;
  }

  @Override
  protected boolean hasSidedTransparency(final BlockState state) {
    return true;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext ctx) {
    return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!player.getMainHandStack().isEmpty()) {
      return ActionResult.PASS;
    }
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }
    if (!(world.getBlockEntity(pos) instanceof DogStatueBlockEntity statue)) {
      return ActionResult.PASS;
    }
    if (player.isSneaking()) {
      final Optional<DogStatue> carving =
          DogStatueSources.nearest((ServerWorld) world, pos, player.getUuid());
      if (carving.isEmpty()) {
        player.sendMessage(Text.translatable("block.dogs-unleashed.dog_statue.no_dog"), true);
        return ActionResult.CONSUME;
      }
      statue.carve(carving.get());
      return ActionResult.SUCCESS;
    }
    if (statue.cyclePose()) {
      return ActionResult.SUCCESS;
    }
    player.sendMessage(Text.translatable("block.dogs-unleashed.dog_statue.hint"), true);
    return ActionResult.CONSUME;
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (newState.getBlock() instanceof DogStatueBlock) {
      return;
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new DogStatueBlockEntity(pos, state);
  }
}
