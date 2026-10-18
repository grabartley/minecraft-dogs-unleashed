package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class SlotBounds {

  private SlotBounds() {}

  public static List<CubeGeometry.Face> facesIn(
      final BakedGeoModel model, final Set<String> boneNames) {
    final List<CubeGeometry.Face> faces = new ArrayList<>();
    for (final String name : boneNames) {
      model.getBone(name).ifPresent(bone -> faces.addAll(CubeGeometry.facesOf(bone)));
    }
    return faces;
  }

  public static CubeGeometry.Bounds of(final BakedGeoModel model, final Set<String> boneNames) {
    return boundsOf(facesIn(model, boneNames));
  }

  public static CubeGeometry.Bounds boundsOf(final List<CubeGeometry.Face> faces) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final CubeGeometry.Face face : faces) {
      for (final Vector3f corner : face.corners()) {
        bounds.include(face.pose().point(corner));
      }
    }
    return bounds;
  }
}
