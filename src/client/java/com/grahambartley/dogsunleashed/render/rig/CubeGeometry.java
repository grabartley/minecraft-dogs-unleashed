package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public final class CubeGeometry {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;
  static final float DETAIL_EXTENT = 0.7f / MODEL_UNITS_PER_BLOCK;

  private CubeGeometry() {}

  public static Vector3f rotateAroundPivot(
      final Vector3f point, final Vector3f pivot, final Vector3f rotationRadians) {
    final Quaternionf rotation =
        new Quaternionf()
            .rotationZ(rotationRadians.z)
            .rotateY(rotationRadians.y)
            .rotateX(rotationRadians.x);
    return new Vector3f(point).sub(pivot).rotate(rotation).add(pivot);
  }

  public static Bounds posedBoundsOf(final GeoBone bone) {
    final Bounds bounds = new Bounds();
    for (final Face face : facesOf(bone)) {
      for (final Vector3f corner : face.corners()) {
        bounds.include(face.pose().point(corner));
      }
    }
    return bounds;
  }

  public static List<Face> facesOf(final GeoBone bone) {
    return facesByCube(bone).stream().flatMap(List::stream).toList();
  }

  public static Bounds localBoundsOf(final GeoCube cube) {
    final Bounds bounds = new Bounds();
    for (final GeoQuad quad : cube.quads()) {
      if (quad == null) {
        continue;
      }
      for (final GeoVertex vertex : quad.vertices()) {
        bounds.include(vertex.position());
      }
    }
    return bounds;
  }

  public static boolean isDetail(final GeoCube cube) {
    final Bounds bounds = localBoundsOf(cube);
    if (bounds.isEmpty()) {
      return true;
    }
    final Vector3f extent = bounds.max().sub(bounds.min());
    return Math.min(extent.x, Math.min(extent.y, extent.z)) < DETAIL_EXTENT;
  }

  public static List<List<Face>> facesByCube(final GeoBone bone) {
    final RestPose pose = RestPose.of(bone);
    final List<List<Face>> cubes = new ArrayList<>();
    for (final GeoCube cube : bone.getCubes()) {
      final List<Face> faces = new ArrayList<>();
      final Vector3f pivot =
          new Vector3f(
              (float) cube.pivot().x / MODEL_UNITS_PER_BLOCK,
              (float) cube.pivot().y / MODEL_UNITS_PER_BLOCK,
              (float) cube.pivot().z / MODEL_UNITS_PER_BLOCK);
      final Vector3f rotation =
          new Vector3f(
              (float) cube.rotation().x, (float) cube.rotation().y, (float) cube.rotation().z);
      for (final GeoQuad quad : cube.quads()) {
        if (quad == null) {
          continue;
        }
        final GeoVertex[] vertices = quad.vertices();
        final Vector3f[] corners = new Vector3f[vertices.length];
        final float[] us = new float[vertices.length];
        final float[] vs = new float[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
          corners[i] = rotateAroundPivot(vertices[i].position(), pivot, rotation);
          us[i] = vertices[i].texU();
          vs[i] = vertices[i].texV();
        }
        final Vector3f normal =
            rotateAroundPivot(new Vector3f(quad.normal()), new Vector3f(), rotation).normalize();
        faces.add(new Face(bone.getName(), corners, us, vs, normal, pose));
      }
      cubes.add(faces);
    }
    return cubes;
  }

  public record Face(
      String boneName, Vector3f[] corners, float[] us, float[] vs, Vector3f normal, RestPose pose) {

    public Vector3f posedNormal() {
      return this.pose.direction(this.normal);
    }
  }

  public static final class Bounds {
    private final Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
    private final Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

    public void include(final Vector3f point) {
      this.min.min(point);
      this.max.max(point);
    }

    public boolean isEmpty() {
      return this.min.x > this.max.x;
    }

    public boolean contains(final Vector3f point) {
      return point.x > this.min.x
          && point.x < this.max.x
          && point.y > this.min.y
          && point.y < this.max.y
          && point.z > this.min.z
          && point.z < this.max.z;
    }

    public boolean containsXy(final float x, final float y) {
      return x >= this.min.x && x <= this.max.x && y >= this.min.y && y <= this.max.y;
    }

    public Vector3f min() {
      return new Vector3f(this.min);
    }

    public Vector3f max() {
      return new Vector3f(this.max);
    }

    public Vector3f center() {
      return new Vector3f(this.min).add(this.max).mul(0.5f);
    }
  }
}
