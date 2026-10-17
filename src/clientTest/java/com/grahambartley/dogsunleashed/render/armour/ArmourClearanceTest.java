package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.List;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArmourClearanceTest {

  private static final float EPSILON = 1.0e-6f;
  private static final CubeGeometry.Bounds TORSO = box(-1.6f, 2.4f, -2.0f, 1.6f, 6.0f, 3.0f);

  private static CubeGeometry.Bounds box(
      final float x0,
      final float y0,
      final float z0,
      final float x1,
      final float y1,
      final float z1) {
    final CubeGeometry.Bounds box = new CubeGeometry.Bounds();
    box.include(new Vector3f(x0, y0, z0).div(16.0f));
    box.include(new Vector3f(x1, y1, z1).div(16.0f));
    return box;
  }

  @Test
  @DisplayName("with nothing beside the torso the armour keeps its usual thickness")
  void defaultsToTheBaseThickness() {
    assertEquals(
        ArmourClearance.BASE, ArmourClearance.inflation(List.of(TORSO), List.of()), EPSILON);
  }

  @Test
  @DisplayName("a haunch wider than the torso pushes the armour out past it")
  void clearsAWideHaunch() {
    final CubeGeometry.Bounds haunch = box(1.3f, 2.0f, 1.3f, 1.9f, 4.6f, 2.9f);

    assertEquals(
        0.3f / 16.0f + ArmourClearance.MARGIN,
        ArmourClearance.inflation(List.of(TORSO), List.of(haunch)),
        EPSILON);
  }

  @Test
  @DisplayName("a wide neck beside the torso does not bulk the armour up")
  void ignoresTheNeck() {
    final CubeGeometry.Bounds neck = box(-3.0f, 4.0f, -3.0f, 3.0f, 7.0f, -1.0f);

    assertEquals(
        ArmourClearance.BASE, ArmourClearance.inflation(List.of(TORSO), List.of(neck)), EPSILON);
  }
}
