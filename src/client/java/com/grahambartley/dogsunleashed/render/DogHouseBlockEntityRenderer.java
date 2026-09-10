package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class DogHouseBlockEntityRenderer extends CushionTintedDogHouseRenderer {

  private static final double CELL_CENTRE = 0.5;

  private static final float AUTHORED_FACING_ROTATION = 180.0f;

  public DogHouseBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

  @Override
  protected Direction getFacing(final DogHouseBlockEntity blockEntity) {
    final BlockState state = blockEntity.getCachedState();
    return state.contains(DogHouseBlock.FACING) ? state.get(DogHouseBlock.FACING) : Direction.NORTH;
  }

  @Override
  protected void rotateBlock(final Direction facing, final MatrixStack poseStack) {
    final Direction right = facing.rotateYClockwise();
    final Direction back = facing.getOpposite();
    poseStack.translate(
        (right.getOffsetX() + back.getOffsetX()) * CELL_CENTRE,
        0.0,
        (right.getOffsetZ() + back.getOffsetZ()) * CELL_CENTRE);
    poseStack.multiply(
        RotationAxis.POSITIVE_Y.rotationDegrees(AUTHORED_FACING_ROTATION - facing.asRotation()));
  }

  @Override
  public boolean rendersOutsideBoundingBox(final DogHouseBlockEntity blockEntity) {
    return true;
  }

  @Override
  public int getRenderDistance() {
    return 128;
  }
}
