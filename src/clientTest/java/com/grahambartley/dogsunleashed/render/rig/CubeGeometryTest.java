package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class CubeGeometryTest {

  private static final float EPSILON = 1.0e-5f;
  private static final float QUARTER_TURN = (float) (Math.PI / 2);

  static Stream<Arguments> rotations() {
    return Stream.of(
        Arguments.of("no rotation", new Vector3f(1, 2, 3), new Vector3f(), new Vector3f(1, 2, 3)),
        Arguments.of(
            "quarter turn about x sends +y to +z",
            new Vector3f(0, 1, 0),
            new Vector3f(QUARTER_TURN, 0, 0),
            new Vector3f(0, 0, 1)),
        Arguments.of(
            "quarter turn about y sends +z to +x",
            new Vector3f(0, 0, 1),
            new Vector3f(0, QUARTER_TURN, 0),
            new Vector3f(1, 0, 0)),
        Arguments.of(
            "x is applied before z, matching GeckoLib's Z then Y then X matrix",
            new Vector3f(1, 0, 0),
            new Vector3f(QUARTER_TURN, 0, QUARTER_TURN),
            new Vector3f(0, 1, 0)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rotations")
  @DisplayName("rotates a point around the origin in GeckoLib's order")
  void rotatesAroundOrigin(
      final String label, final Vector3f point, final Vector3f rotation, final Vector3f expected) {
    assertClose(expected, CubeGeometry.rotateAroundPivot(point, new Vector3f(), rotation));
  }

  @Test
  @DisplayName("rotation happens around the pivot, not the origin")
  void rotatesAroundThePivot() {
    final Vector3f rotated =
        CubeGeometry.rotateAroundPivot(
            new Vector3f(2, 3, 0), new Vector3f(2, 2, 0), new Vector3f(QUARTER_TURN, 0, 0));

    assertClose(new Vector3f(2, 2, 1), rotated);
  }

  @Test
  @DisplayName("a bone's bounds cover every vertex of its cubes after the cube rotation")
  void boneBoundsCoverRotatedVertices() {
    final GeoBone bone = new GeoBone(null, "neck", false, 0.0, false, false);
    final GeoVertex[] vertices = {
      new GeoVertex(0, 0, 0), new GeoVertex(0.125, 0, 0),
      new GeoVertex(0.125, 0.25, 0), new GeoVertex(0, 0.25, 0)
    };
    bone.getCubes()
        .add(
            new GeoCube(
                new GeoQuad[] {new GeoQuad(vertices, new Vector3f(0, 0, -1), Direction.NORTH)},
                Vec3d.ZERO,
                new Vec3d(QUARTER_TURN, 0, 0),
                new Vec3d(2, 4, 0),
                0,
                false));

    final CubeGeometry.Bounds bounds = CubeGeometry.posedBoundsOf(bone);

    assertClose(new Vector3f(0, 0, 0), bounds.min());
    assertClose(new Vector3f(0.125f, 0, 0.25f), bounds.max());
  }

  @Test
  @DisplayName("a bone with no cubes has empty bounds")
  void boneWithoutCubesIsEmpty() {
    assertTrue(
        CubeGeometry.posedBoundsOf(new GeoBone(null, "group", false, 0.0, false, false)).isEmpty());
  }

  private static void assertClose(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON, "x of " + actual);
    assertEquals(expected.y, actual.y, EPSILON, "y of " + actual);
    assertEquals(expected.z, actual.z, EPSILON, "z of " + actual);
  }
}
