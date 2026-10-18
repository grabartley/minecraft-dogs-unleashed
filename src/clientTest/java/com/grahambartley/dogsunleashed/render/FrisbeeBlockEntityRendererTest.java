package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.block.FrisbeeBlock;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.util.math.Box;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrisbeeBlockEntityRendererTest {

  private static final double TOLERANCE = 1e-5;

  private static Box placed() {
    return PlacedGeoBounds.of(
        ShippedRigs.model("frisbee"), FrisbeeBlockEntityRenderer.PLACED_SCALE);
  }

  @Test
  @DisplayName("the placed disc spans its block outline edge to edge")
  void placedDiscSpansTheOutline() {
    final Box outline = FrisbeeBlock.SHAPE.getBoundingBox();

    assertEquals(outline.minX, placed().minX, TOLERANCE);
    assertEquals(outline.maxX, placed().maxX, TOLERANCE);
    assertEquals(outline.minZ, placed().minZ, TOLERANCE);
    assertEquals(outline.maxZ, placed().maxZ, TOLERANCE);
  }

  @Test
  @DisplayName("the placed disc rests on the floor and stays under the outline's top")
  void placedDiscRestsOnTheFloor() {
    final Box outline = FrisbeeBlock.SHAPE.getBoundingBox();

    assertEquals(outline.minY, placed().minY, TOLERANCE);
    assertTrue(placed().maxY <= outline.maxY, "disc top " + placed().maxY);
  }
}
