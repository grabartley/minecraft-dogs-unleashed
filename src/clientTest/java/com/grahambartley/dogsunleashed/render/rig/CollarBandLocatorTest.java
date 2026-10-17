package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CollarBandLocatorTest {

  private static final float EPSILON = 1.0e-5f;
  private static final int SIZE = 4;
  private static final Vector3f MOUTH_AT_NEGATIVE_Z = new Vector3f(0, 0.5f, -2.0f);

  private record Mask(Set<Integer> painted) implements CollarBandLocator.CollarMask {
    @Override
    public int width() {
      return SIZE;
    }

    @Override
    public int height() {
      return SIZE;
    }

    @Override
    public boolean painted(final int x, final int y) {
      return this.painted.contains(y * SIZE + x);
    }
  }

  private static CubeGeometry.Face face(
      final String bone, final float z, final float normalZ, final float x0, final float x1) {
    return new CubeGeometry.Face(
        bone,
        new Vector3f[] {
          new Vector3f(x0, 1, z),
          new Vector3f(x1, 1, z),
          new Vector3f(x1, 0, z),
          new Vector3f(x0, 0, z)
        },
        new float[] {0, 1, 1, 0},
        new float[] {0, 0, 1, 1},
        new Vector3f(0, 0, normalZ));
  }

  private static int texel(final int x, final int y) {
    return y * SIZE + x;
  }

  @Test
  @DisplayName("picks the bottom of the band on the front face, at the throat")
  void picksBottomOfBandAtTheThroat() {
    final CubeGeometry.Face front = face("neck", -0.5f, -1.0f, -0.1f, 0.1f);
    final Mask band = new Mask(Set.of(texel(1, 1), texel(2, 1), texel(1, 2), texel(2, 2)));

    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(List.of(front), MOUTH_AT_NEGATIVE_Z, band).orElseThrow();

    assertEquals("neck", spot.boneName());
    assertEquals(0.375f, spot.point().y, EPSILON);
    assertEquals(-0.5f, spot.point().z, EPSILON);
    assertTrue(spot.facesNegativeZ());
  }

  @Test
  @DisplayName("collar paint on faces turned away from the front is ignored")
  void ignoresBackFaces() {
    final CubeGeometry.Face front = face("neck", -0.5f, -1.0f, -0.1f, 0.1f);
    final CubeGeometry.Face back = face("torso", 0.5f, 1.0f, -0.5f, 0.5f);
    final Mask band = new Mask(Set.of(texel(1, 1), texel(1, 3)));

    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(List.of(back, front), MOUTH_AT_NEGATIVE_Z, band).orElseThrow();

    assertEquals("neck", spot.boneName());
  }

  @Test
  @DisplayName("paint at the throat wins over lower paint off to the side")
  void prefersTheMidline() {
    final CubeGeometry.Face front = face("neck", -0.5f, -1.0f, -0.1f, 0.1f);
    final CubeGeometry.Face shoulder = face("leg", -0.5f, -1.0f, 2.0f, 3.0f);
    final Mask band = new Mask(Set.of(texel(1, 1), texel(1, 3)));

    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(List.of(shoulder, front), MOUTH_AT_NEGATIVE_Z, band).orElseThrow();

    assertEquals("neck", spot.boneName());
  }

  @Test
  @DisplayName("a rig facing positive z reads its front from the other side")
  void positiveZRig() {
    final CubeGeometry.Face front = face("head", 0.5f, 1.0f, -0.1f, 0.1f);
    final Mask band = new Mask(Set.of(texel(1, 2)));

    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(List.of(front), new Vector3f(0, 0.5f, 2.0f), band).orElseThrow();

    assertEquals("head", spot.boneName());
    assertTrue(!spot.facesNegativeZ());
  }

  @Test
  @DisplayName("no collar paint on any front face means no spot")
  void noPaintNoSpot() {
    final CubeGeometry.Face front = face("neck", -0.5f, -1.0f, -0.1f, 0.1f);

    assertTrue(
        CollarBandLocator.locate(List.of(front), MOUTH_AT_NEGATIVE_Z, new Mask(Set.of()))
            .isEmpty());
  }
}
