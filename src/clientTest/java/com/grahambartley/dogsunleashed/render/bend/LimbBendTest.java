package com.grahambartley.dogsunleashed.render.bend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LimbBendTest {

  private static final float EPSILON = 1.0e-4f;
  private static final float JOINT = 6.0f;

  private static LimbBend.Corner corner(final float y, final float z, final float v) {
    return new LimbBend.Corner(new Vector3f(-2.0f, y, z), 0.0f, v);
  }

  private static LimbBend.Corner[] frontFace(final float top, final float bottom) {
    return new LimbBend.Corner[] {
      corner(top, -2.0f, 20.0f),
      corner(bottom, -2.0f, 32.0f),
      new LimbBend.Corner(new Vector3f(2.0f, bottom, -2.0f), 4.0f, 32.0f),
      new LimbBend.Corner(new Vector3f(2.0f, top, -2.0f), 4.0f, 20.0f)
    };
  }

  @ParameterizedTest(name = "face from {0} to {1}")
  @CsvSource({"0, 12, 2", "-0.25, 12.25, 2", "0, 6, 1", "6, 12, 1", "12, 12, 1"})
  @DisplayName("a face across the joint splits in two, the rest stay whole")
  void facesAcrossTheJointSplit(final float top, final float bottom, final int pieces) {
    assertEquals(pieces, LimbBend.split(frontFace(top, bottom), JOINT).size());
  }

  @ParameterizedTest(name = "face from {0} to {1}")
  @CsvSource({"0, 12", "-0.25, 12.25", "-1, 13"})
  @DisplayName("the split meets at the joint and keeps the skin texture continuous")
  void theSplitKeepsTheTexture(final float top, final float bottom) {
    final List<LimbBend.Corner[]> pieces = LimbBend.split(frontFace(top, bottom), JOINT);
    final float jointV = 20.0f + 12.0f * (JOINT - top) / (bottom - top);
    for (final LimbBend.Corner[] piece : pieces) {
      assertEquals(4, piece.length);
      boolean touchesJoint = false;
      for (final LimbBend.Corner corner : piece) {
        if (Math.abs(corner.pos().y - JOINT) < EPSILON) {
          touchesJoint = true;
          assertEquals(jointV, corner.v(), 1.0e-3f);
        }
      }
      assertTrue(touchesJoint, "each half ends on the joint");
    }
    assertEquals(false, LimbBend.isLower(pieces.get(0), JOINT));
    assertEquals(true, LimbBend.isLower(pieces.get(1), JOINT));
  }

  @ParameterizedTest(name = "bend {0} degrees")
  @CsvSource({"0", "45", "90", "-60", "120"})
  @DisplayName("above the joint stays put and below it turns with the bend")
  void theLowerHalfTurns(final float degrees) {
    final float angle = (float) Math.toRadians(degrees);
    final Vector3f above = new Vector3f(1.0f, 2.0f, -2.0f);
    assertEquals(0.0f, LimbBend.bend(above, JOINT, angle).distance(above), EPSILON);
    final Vector3f foot = LimbBend.bend(new Vector3f(0.0f, 12.0f, 0.0f), JOINT, angle);
    assertEquals(JOINT + 6.0f * (float) Math.cos(angle), foot.y, EPSILON);
    assertEquals(6.0f * (float) Math.sin(angle), foot.z, EPSILON);
  }

  @ParameterizedTest(name = "bend {0} degrees, edge at z {1}")
  @CsvSource({"90, -2", "90, 2", "60, 2", "-45, -2.25"})
  @DisplayName("the joint is mitred, so both halves meet with no gap or pinch")
  void theJointIsMitred(final float degrees, final float z) {
    final float angle = (float) Math.toRadians(degrees);
    final Vector3f mitre = LimbBend.bend(new Vector3f(0.0f, JOINT, z), JOINT, angle);
    final Vector3f fromJoint = new Vector3f(mitre).sub(0.0f, JOINT, 0.0f);
    final Vector3f lowerSide = new Vector3f(0.0f, 0.0f, 1.0f).rotateX(angle);
    assertEquals(z, fromJoint.z, 1.0e-3f, "on the upper half's face");
    assertEquals(z, fromJoint.dot(lowerSide), 1.0e-3f, "on the lower half's face");
  }

  @ParameterizedTest(name = "bend {0} degrees")
  @CsvSource({"0", "90", "-90"})
  @DisplayName("faces below the joint turn their lighting with the bend")
  void lowerNormalsTurn(final float degrees) {
    final float angle = (float) Math.toRadians(degrees);
    final Vector3f front = new Vector3f(0.0f, 0.0f, -1.0f);
    assertEquals(0.0f, LimbBend.bendNormal(front, false, angle).distance(front), EPSILON);
    final Vector3f turned = LimbBend.bendNormal(front, true, angle);
    assertEquals((float) Math.sin(angle), turned.y, EPSILON);
    assertEquals(-(float) Math.cos(angle), turned.z, EPSILON);
  }
}
