package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.block.TennisBallBlock;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.util.math.Box;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TennisBallBlockEntityRendererTest {

  private static final double TOLERANCE = 1e-5;

  @Test
  @DisplayName("the placed ball fills its block outline exactly and rests on the floor")
  void placedBallFillsItsOutline() {
    final Box placed =
        PlacedGeoBounds.of(
            ShippedRigs.model("tennis_ball"), TennisBallBlockEntityRenderer.PLACED_SCALE);
    final Box outline = TennisBallBlock.SHAPE.getBoundingBox();

    assertEquals(outline.minX, placed.minX, TOLERANCE);
    assertEquals(outline.maxX, placed.maxX, TOLERANCE);
    assertEquals(outline.minY, placed.minY, TOLERANCE);
    assertEquals(outline.maxY, placed.maxY, TOLERANCE);
    assertEquals(outline.minZ, placed.minZ, TOLERANCE);
    assertEquals(outline.maxZ, placed.maxZ, TOLERANCE);
  }
}
