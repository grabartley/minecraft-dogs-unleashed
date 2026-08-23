package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.model.DogHouseModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * The whole house is drawn once, from the origin cell that owns the block entity; the other seven
 * cells carry no block entity and so no second copy.
 *
 * <p>Placing a model that is bigger than its block takes care, because {@code GeoBlockRenderer}
 * turns for the block's facing about the model's own origin and only afterwards steps half a block
 * to reach the cell's middle. A one-cell model is authored centred on zero, so spinning it about
 * that origin leaves it where it started and the arrangement works. A two-by-two footprint has no
 * such luck: its middle is the corner the four lower cells share, a half block away from any cell's
 * middle, so the default order swings the house into the wrong cells on three of the four facings.
 *
 * <p>So the model is authored centred on the footprint, and the turn is taken here after stepping
 * to the cell's middle in world axes. That leaves the renderer's own half-block step to carry the
 * footprint's middle onto the shared corner, and the house lands on its own cells whichever way it
 * faces.
 */
public class DogHouseBlockEntityRenderer extends GeoBlockRenderer<DogHouseBlockEntity> {

  private static final double CELL_CENTRE = 0.5;

  /** North is the facing the model is authored in, so its turn has to come out as none at all. */
  private static final float AUTHORED_FACING_ROTATION = 180.0f;

  public DogHouseBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new DogHouseModel());
  }

  /**
   * The renderer's own facing lookup reports north for every dog house, so the house has to read
   * its facing from its own block state. Left to the default, all four facings drew north-facing
   * and three of them landed a cell away from the ground they stand on.
   */
  @Override
  protected Direction getFacing(final DogHouseBlockEntity blockEntity) {
    final BlockState state = blockEntity.getCachedState();
    return state.contains(DogHouseBlock.FACING) ? state.get(DogHouseBlock.FACING) : Direction.NORTH;
  }

  /**
   * Placement is taken over here because the renderer's own half-block step to the cell's middle
   * lands after the turn. Stepping to the middle of this house's footprint first, in world axes,
   * leaves that step to finish the job and the house lands on its own cells on every facing.
   */
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

  /**
   * The house reaches well beyond the cell its block entity sits in, so without this it vanishes as
   * soon as that one cell leaves the view.
   */
  @Override
  public boolean rendersOutsideBoundingBox(final DogHouseBlockEntity blockEntity) {
    return true;
  }

  @Override
  public int getRenderDistance() {
    return 128;
  }
}
