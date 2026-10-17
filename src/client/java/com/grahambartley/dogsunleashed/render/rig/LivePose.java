package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

public final class LivePose {

  private LivePose() {}

  public static Matrix4f of(final GeoBone bone) {
    final Deque<GeoBone> rootToBone = new ArrayDeque<>();
    for (GeoBone current = bone; current != null; current = current.getParent()) {
      rootToBone.push(current);
    }
    final MatrixStack stack = new MatrixStack();
    rootToBone.forEach(current -> RenderUtil.prepMatrixForBone(stack, current));
    return new Matrix4f(stack.peek().getPositionMatrix());
  }
}
