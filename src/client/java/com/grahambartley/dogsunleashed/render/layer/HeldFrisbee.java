package com.grahambartley.dogsunleashed.render.layer;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

public final class HeldFrisbee {

  static final float NOSE_DOWN_DEGREES = 15.0f;
  private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);
  private static final float PIXELS_PER_BLOCK = 16.0f;

  private HeldFrisbee() {}

  public static void place(
      final MatrixStack stack,
      final GeoBone mouth,
      final MouthGrip grip,
      final Matrix4f entityOrigin) {
    MouthFrame.undoBoneRotation(stack, mouth);
    final Vector3f point =
        grip.point()
            .sub(
                mouth.getPivotX() / PIXELS_PER_BLOCK,
                mouth.getPivotY() / PIXELS_PER_BLOCK,
                mouth.getPivotZ() / PIXELS_PER_BLOCK);
    stack.translate(point.x, point.y, point.z);
    MouthFrame.levelToRest(stack, mouth);
    final float discScale =
        CarriedToy.FRISBEE.scaleIn(entityOrigin, stack.peek().getPositionMatrix());
    gripToDisc(
        stack, grip.forward(), discScale, CarriedToy.FRISBEE.modelPixels() / PIXELS_PER_BLOCK);
  }

  public static void gripToDisc(
      final MatrixStack stack,
      final Vector3f forward,
      final float discScale,
      final float modelDiameter) {
    stack.multiply(
        new Quaternionf()
            .rotationAxis(
                (float) Math.toRadians(NOSE_DOWN_DEGREES),
                new Vector3f(UP).cross(forward).normalize()));
    final Vector3f out = new Vector3f(forward).mul(discScale * modelDiameter / 2.0f);
    stack.translate(out.x, out.y, out.z);
    stack.scale(discScale, discScale, discScale);
  }
}
