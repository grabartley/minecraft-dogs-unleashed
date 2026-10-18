package com.grahambartley.dogsunleashed.render.layer;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;

public final class MouthFrame {

  private MouthFrame() {}

  public static void level(final MatrixStack poseStack, final GeoBone mouth) {
    poseStack.multiply(rotationOf(mouth.getRotX(), mouth.getRotY(), mouth.getRotZ()).conjugate());
    poseStack.multiply(restRotationOf(mouth).conjugate());
  }

  static Quaternionf restRotationOf(final GeoBone mouth) {
    final Quaternionf chain = new Quaternionf();
    for (GeoBone link = mouth; link != null; link = link.getParent()) {
      final BoneSnapshot rest = link.getInitialSnapshot();
      chain.premul(rotationOf(rest.getRotX(), rest.getRotY(), rest.getRotZ()));
    }
    return chain;
  }

  static Quaternionf rotationOf(final float x, final float y, final float z) {
    return new Quaternionf().rotateZ(z).rotateY(y).rotateX(x);
  }
}
