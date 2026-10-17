package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuadUvTest {

  private static final float EPSILON = 1.0e-5f;

  static CubeGeometry.Face unitFace(final float[] us, final float[] vs) {
    return new CubeGeometry.Face(
        "neck",
        new Vector3f[] {
          new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(1, 1, 0), new Vector3f(0, 1, 0)
        },
        us,
        vs,
        new Vector3f(0, 0, -1),
        RestPose.NONE);
  }

  @Test
  @DisplayName("a texture point maps to the matching spot on the face")
  void mapsTextureToFace() {
    final CubeGeometry.Face face = unitFace(new float[] {0, 1, 1, 0}, new float[] {0, 0, 1, 1});

    final Vector3f point = QuadUv.pointAt(face, 0.25f, 0.75f).orElseThrow();

    assertEquals(0.25f, point.x, EPSILON);
    assertEquals(0.75f, point.y, EPSILON);
    assertEquals(0.0f, point.z, EPSILON);
  }

  @Test
  @DisplayName("a face whose texture runs backwards maps mirrored, as GeckoLib lays faces out")
  void mapsMirroredTexture() {
    final CubeGeometry.Face face = unitFace(new float[] {1, 0, 0, 1}, new float[] {1, 1, 0, 0});

    final Vector3f point = QuadUv.pointAt(face, 0.25f, 0.75f).orElseThrow();

    assertEquals(0.75f, point.x, EPSILON);
    assertEquals(0.25f, point.y, EPSILON);
  }

  @Test
  @DisplayName("covers only texture points inside the face's patch")
  void coversOnlyItsPatch() {
    final CubeGeometry.Face face =
        unitFace(new float[] {0, 0.5f, 0.5f, 0}, new float[] {0, 0, 0.5f, 0.5f});

    assertTrue(QuadUv.covers(face, 0.25f, 0.25f));
    assertFalse(QuadUv.covers(face, 0.75f, 0.25f));
  }

  @Test
  @DisplayName("a face with no texture area maps nowhere")
  void degenerateFaceMapsNowhere() {
    final CubeGeometry.Face face =
        unitFace(new float[] {0.5f, 0.5f, 0.5f, 0.5f}, new float[] {0, 0, 1, 1});

    assertTrue(QuadUv.pointAt(face, 0.5f, 0.5f).isEmpty());
  }
}
