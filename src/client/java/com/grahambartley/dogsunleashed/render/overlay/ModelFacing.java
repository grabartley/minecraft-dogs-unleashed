package com.grahambartley.dogsunleashed.render.overlay;

import java.util.function.UnaryOperator;
import net.minecraft.util.math.Direction;

public final class ModelFacing {

  private static final float QUARTER_TURN = 90.0f;

  private ModelFacing() {}

  public static UnaryOperator<Direction> turnedBy(final float yawDegrees) {
    final int quarterTurns = Math.floorMod(Math.round(yawDegrees / QUARTER_TURN), 4);
    return face -> {
      if (face.getAxis() == Direction.Axis.Y) {
        return face;
      }
      Direction turned = face;
      for (int turn = 0; turn < quarterTurns; turn++) {
        turned = turned.rotateYCounterclockwise();
      }
      return turned;
    };
  }
}
