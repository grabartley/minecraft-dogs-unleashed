package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class PoseClearanceTest {

  private static final float EPSILON = 1.0e-5f;
  private static final Vector3f FORWARD = new Vector3f(0, 0, -1);

  private static PoseClearance.Box box(final Vector3f min, final Vector3f max) {
    return new PoseClearance.Box(new Matrix4f(), min, max);
  }

  static Stream<Arguments> exits() {
    return Stream.of(
        Arguments.of("straight out the front", new Vector3f(0, 0, 0.5f), FORWARD, 1.5f),
        Arguments.of(
            "out the side it is heading for",
            new Vector3f(0.5f, 0, 0),
            new Vector3f(1, 0, 0),
            0.5f),
        Arguments.of(
            "on a diagonal, through the nearer wall",
            new Vector3f(0.8f, 0, 0),
            new Vector3f(1, 0, -1).normalize(),
            0.2f * (float) Math.sqrt(2)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exits")
  @DisplayName("measures how far a point has to travel to leave a box")
  void exitDistance(
      final String label, final Vector3f from, final Vector3f direction, final float expected) {
    final PoseClearance.Box unit = box(new Vector3f(-1), new Vector3f(1));

    assertEquals(expected, unit.exitAlong(from, direction), EPSILON);
  }

  @Test
  @DisplayName("a turned box is measured in its own frame")
  void turnedBox() {
    final Matrix4f quarterTurn = new Matrix4f().rotationY((float) (Math.PI / 2)).invert();
    final PoseClearance.Box turned =
        new PoseClearance.Box(quarterTurn, new Vector3f(-2, -1, -0.5f), new Vector3f(2, 1, 0.5f));

    assertTrue(turned.contains(new Vector3f(0, 0, -1.5f)));
    assertFalse(turned.contains(new Vector3f(1.5f, 0, 0)));
    assertEquals(2.0f, turned.exitAlong(new Vector3f(), FORWARD), EPSILON);
  }

  @Test
  @DisplayName("a point outside every box is not moved")
  void outsideStays() {
    final float pushed =
        PoseClearance.pushOut(
            List.of(box(new Vector3f(-1), new Vector3f(1))),
            List.of(new Vector3f(0, 0, -2)),
            FORWARD);

    assertEquals(0.0f, pushed, EPSILON);
  }

  @Test
  @DisplayName("a point is pushed through stacked boxes until it is clear of all of them")
  void clearsStackedBoxes() {
    final List<PoseClearance.Box> stacked =
        List.of(
            box(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1)),
            box(new Vector3f(-1, -1, -3), new Vector3f(1, 1, -0.9f)));

    final float pushed = PoseClearance.pushOut(stacked, List.of(new Vector3f()), FORWARD);

    assertTrue(pushed > 3.0f && pushed < 3.0f + 3 * PoseClearance.CLEARANCE, "pushed " + pushed);
  }

  @Test
  @DisplayName("the deepest of several points decides the push")
  void deepestPointDecides() {
    final float pushed =
        PoseClearance.pushOut(
            List.of(box(new Vector3f(-1), new Vector3f(1))),
            List.of(new Vector3f(0, 0, -0.5f), new Vector3f(0, 0.5f, 0.5f)),
            FORWARD);

    assertEquals(1.5f + PoseClearance.CLEARANCE, pushed, EPSILON);
  }

  @Test
  @DisplayName("only the named bones become boxes, and a hidden bone casts none")
  void liveBoxesFollowTheNamedBones() {
    final BakedGeoModel beagle = ShippedRigs.model("beagle");
    final GeoBone torso = beagle.getBone("Torso").orElseThrow();
    final int torsoCubes = torso.getCubes().size();
    final int tailCubes = beagle.getBone("rabo").orElseThrow().getCubes().size();

    assertEquals(torsoCubes, PoseClearance.liveBoxesOf(beagle, Set.of("Torso")).size());
    assertEquals(
        torsoCubes + tailCubes, PoseClearance.liveBoxesOf(beagle, Set.of("Torso", "rabo")).size());

    torso.setHidden(true);

    assertEquals(0, PoseClearance.liveBoxesOf(beagle, Set.of("Torso", "rabo")).size());
  }

  @Test
  @DisplayName("a live box is built from the bone's current pose, not its rest pose")
  void liveBoxesFollowThePose() {
    final BakedGeoModel beagle = ShippedRigs.model("beagle");
    final GeoBone torso = beagle.getBone("Torso").orElseThrow();
    ShippedRigs.pose(beagle, "beagle", "sit");

    final PoseClearance.Box sitting = PoseClearance.liveBoxesOf(beagle, Set.of("Torso")).get(0);
    final Matrix4f expected =
        PoseClearance.boxOf(LivePose.of(torso), torso.getCubes().get(0))
            .orElseThrow()
            .modelToLocal();

    assertTrue(expected.equals(sitting.modelToLocal(), EPSILON));
    assertFalse(
        PoseClearance.boxOf(new Matrix4f(), torso.getCubes().get(0))
            .orElseThrow()
            .modelToLocal()
            .equals(sitting.modelToLocal(), EPSILON));
  }
}
