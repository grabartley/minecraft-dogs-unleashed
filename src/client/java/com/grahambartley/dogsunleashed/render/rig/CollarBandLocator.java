package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class CollarBandLocator {

  static final float FRONT_FACING = 0.7f;
  static final float MIDLINE_TOLERANCE = 1.5f / 16.0f;

  public interface CollarMask {
    int width();

    int height();

    boolean painted(int x, int y);
  }

  public record CollarSpot(String boneName, Vector3f point, boolean facesNegativeZ) {}

  private record Candidate(String boneName, Vector3f point) {}

  private CollarBandLocator() {}

  public static Optional<CollarSpot> locate(
      final BakedGeoModel model, final String mouthBone, final CollarMask mask) {
    final List<CubeGeometry.Face> faces = new ArrayList<>();
    collectFaces(model.topLevelBones(), faces);
    final Optional<GeoBone> mouth = model.getBone(mouthBone);
    if (mouth.isEmpty() || faces.isEmpty()) {
      return Optional.empty();
    }
    return locate(faces, CubeGeometry.boundsOf(mouth.get()).center(), mask);
  }

  static Optional<CollarSpot> locate(
      final List<CubeGeometry.Face> faces, final Vector3f mouthCenter, final CollarMask mask) {
    final CubeGeometry.Bounds whole = new CubeGeometry.Bounds();
    faces.forEach(face -> java.util.Arrays.stream(face.corners()).forEach(whole::include));
    final Vector3f center = whole.center();
    final boolean facesNegativeZ = mouthCenter.z < center.z;
    final float frontZ = facesNegativeZ ? -1.0f : 1.0f;
    final List<Candidate> band = new ArrayList<>();
    for (final CubeGeometry.Face face : faces) {
      if (face.normal().z * frontZ < FRONT_FACING) {
        continue;
      }
      collectPaintedPoints(face, mask, band);
    }
    final List<Candidate> nearMidline =
        band.stream()
            .filter(candidate -> Math.abs(candidate.point().x - mouthCenter.x) <= MIDLINE_TOLERANCE)
            .toList();
    return (nearMidline.isEmpty() ? band : nearMidline)
        .stream()
            .min(
                Comparator.comparingDouble((Candidate candidate) -> candidate.point().y)
                    .thenComparingDouble(candidate -> candidate.point().z * frontZ * -1.0f))
            .map(
                candidate ->
                    new CollarSpot(candidate.boneName(), candidate.point(), facesNegativeZ));
  }

  private static void collectPaintedPoints(
      final CubeGeometry.Face face, final CollarMask mask, final List<Candidate> out) {
    for (int y = 0; y < mask.height(); y++) {
      for (int x = 0; x < mask.width(); x++) {
        if (!mask.painted(x, y)) {
          continue;
        }
        final float u = (x + 0.5f) / mask.width();
        final float v = (y + 0.5f) / mask.height();
        if (QuadUv.covers(face, u, v)) {
          QuadUv.pointAt(face, u, v)
              .ifPresent(point -> out.add(new Candidate(face.boneName(), point)));
        }
      }
    }
  }

  private static void collectFaces(final List<GeoBone> bones, final List<CubeGeometry.Face> out) {
    for (final GeoBone bone : bones) {
      out.addAll(CubeGeometry.facesOf(bone));
      collectFaces(bone.getChildBones(), out);
    }
  }
}
