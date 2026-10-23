package com.grahambartley.dogsunleashed.render.layer;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

public final class HeldFrisbee {

  static final float HEAD_WIDTHS = 1.25f;
  static final float NOSE_DOWN_DEGREES = 15.0f;
  private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
  private static final float PIXELS_PER_BLOCK = 16.0f;

  private HeldFrisbee() {}

  public static void place(
      final MatrixStack stack,
      final GeoBone mouth,
      final MouthGrip grip,
      final float modelDiameter) {
    MouthFrame.undoBoneRotation(stack, mouth);
    final Vector3f point =
        grip.point()
            .sub(
                mouth.getPivotX() / PIXELS_PER_BLOCK,
                mouth.getPivotY() / PIXELS_PER_BLOCK,
                mouth.getPivotZ() / PIXELS_PER_BLOCK);
    stack.translate(point.x, point.y, point.z);
    MouthFrame.levelToRest(stack, mouth);
    stack.multiplyPositionMatrix(gripToDisc(grip, modelDiameter));
  }

  public static Matrix4f gripToDisc(final MouthGrip grip, final float modelDiameter) {
    final float diameter = HEAD_WIDTHS * grip.headWidth();
    final Vector3f forward = grip.forward();
    return new Matrix4f()
        .rotate((float) Math.toRadians(NOSE_DOWN_DEGREES), new Vector3f(UP).cross(forward))
        .translate(forward.mul(diameter / 2.0f))
        .scale(diameter / modelDiameter);
  }
}
