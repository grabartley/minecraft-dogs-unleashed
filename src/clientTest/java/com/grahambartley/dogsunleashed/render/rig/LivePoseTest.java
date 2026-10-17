package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.GeoBone;

class LivePoseTest {

  private static final float EPSILON = 1.0e-5f;
  private static final float QUARTER_TURN = (float) (Math.PI / 2);

  @Test
  @DisplayName("a child bone is posed by its own joint and then by its parent's")
  void childThenParent() {
    final GeoBone body = new GeoBone(null, "body", false, 0.0, false, false);
    final GeoBone neck = new GeoBone(body, "neck", false, 0.0, false, false);
    body.getChildBones().add(neck);
    body.setRotY(QUARTER_TURN);
    neck.updatePivot(0, 16, 0);
    neck.setRotX(QUARTER_TURN);
    neck.setPosY(16);

    final Vector3f tip = LivePose.of(neck).transformPosition(new Vector3f(0, 2, 0));

    assertEquals(1.0f, tip.x, EPSILON);
    assertEquals(2.0f, tip.y, EPSILON);
    assertEquals(0.0f, tip.z, EPSILON);
  }

  @Test
  @DisplayName("a bone at rest leaves points where they are")
  void restIsIdentity() {
    final GeoBone bone = new GeoBone(null, "body", false, 0.0, false, false);

    final Vector3f point = LivePose.of(bone).transformPosition(new Vector3f(0.3f, 0.2f, -0.1f));

    assertEquals(new Vector3f(0.3f, 0.2f, -0.1f).distance(point), 0.0f, EPSILON);
  }
}
