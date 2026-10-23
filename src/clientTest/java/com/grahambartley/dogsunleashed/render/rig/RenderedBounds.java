package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

public final class RenderedBounds {

  private RenderedBounds() {}

  public static CubeGeometry.Bounds of(final BakedGeoModel model, final Set<String> bones) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final CubeGeometry.Bounds cube : cubesOf(model, bones)) {
      bounds.include(cube.min());
      bounds.include(cube.max());
    }
    return bounds;
  }

  public static List<CubeGeometry.Bounds> cubesOf(
      final BakedGeoModel model, final Set<String> bones) {
    final List<CubeGeometry.Bounds> cubes = new ArrayList<>();
    for (final String name : bones) {
      model
          .getBone(name)
          .ifPresent(
              bone -> {
                final Matrix4f boneMatrix = LivePose.of(bone);
                for (final GeoCube cube : bone.getCubes()) {
                  final MatrixStack stack = new MatrixStack();
                  stack.peek().getPositionMatrix().set(boneMatrix);
                  RenderUtil.translateToPivotPoint(stack, cube);
                  RenderUtil.rotateMatrixAroundCube(stack, cube);
                  RenderUtil.translateAwayFromPivotPoint(stack, cube);
                  final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
                  for (final GeoQuad quad : cube.quads()) {
                    if (quad == null) {
                      continue;
                    }
                    for (final GeoVertex vertex : quad.vertices()) {
                      bounds.include(
                          stack
                              .peek()
                              .getPositionMatrix()
                              .transformPosition(new Vector3f(vertex.position())));
                    }
                  }
                  if (!bounds.isEmpty()) {
                    cubes.add(bounds);
                  }
                }
              });
    }
    return cubes;
  }
}
