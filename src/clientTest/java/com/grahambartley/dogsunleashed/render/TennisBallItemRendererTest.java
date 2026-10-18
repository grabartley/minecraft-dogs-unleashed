package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.util.math.Box;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TennisBallItemRendererTest {

  private static final double TOLERANCE = 1e-5;
  private static final double BLOCK_CENTRE = 0.5;

  @Test
  @DisplayName("the held ball sits at the centre of the block space, like a vanilla block")
  void heldBallIsCentredInTheBlock() {
    final Box ball = UnrotatedGeoBounds.of(ShippedRigs.model("tennis_ball"), 1.0f);

    assertEquals(
        BLOCK_CENTRE, ball.getCenter().y + TennisBallItemRenderer.LIFT_TO_BLOCK_CENTRE, TOLERANCE);
    assertEquals(BLOCK_CENTRE, ball.getCenter().x, TOLERANCE);
    assertEquals(BLOCK_CENTRE, ball.getCenter().z, TOLERANCE);
  }
}
