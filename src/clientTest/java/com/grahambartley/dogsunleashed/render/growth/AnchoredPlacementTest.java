package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnchoredPlacementTest {

  private static final float EPSILON = 1.0e-5f;
  private static final RigAnchors ANCHORS =
      new RigAnchors("body", Map.of("neck", "body", "head", "neck", "leg", "body"));
  private static final Map<String, Vector3f> PIVOTS =
      Map.of(
          "body", new Vector3f(0, 8, 0),
          "neck", new Vector3f(0, 10, -4),
          "head", new Vector3f(0, 12, -6),
          "leg", new Vector3f(2, 6, -3));
  private static final Function<String, Vector3f> POSED = name -> new Vector3f(PIVOTS.get(name));

  @ParameterizedTest(name = "torso {0}")
  @CsvSource({"0.8", "1.0", "1.25"})
  @DisplayName("a leg's joint moves with the torso's scale around the torso pivot")
  void legJointFollowsTheTorso(final float torso) {
    final AnchoredPlacement placement =
        placement(Map.of("body", torso, "neck", torso, "leg", 1.1f));

    final AnchoredPlacement.Shift shift = placement.shiftOf("leg", POSED).orElseThrow();

    assertVector(new Vector3f(2 * torso, 8 - 2 * torso, -3 * torso), shift.to());
    assertVector(new Vector3f(2, 6, -3), shift.from());
    assertEquals(1.1f, shift.scale(), EPSILON);
  }

  @Test
  @DisplayName("a head's joint is carried through the neck, which is carried by the torso")
  void headJointIsCarriedThroughTheChain() {
    final AnchoredPlacement placement = placement(Map.of("body", 0.5f, "neck", 0.5f, "head", 2.0f));

    final AnchoredPlacement.Shift shift = placement.shiftOf("head", POSED).orElseThrow();

    assertVector(new Vector3f(0, 10, -3), shift.to());
    assertEquals(2.0f, shift.scale(), EPSILON);
  }

  @Test
  @DisplayName("a posed part's joint is taken from where the pose put it")
  void posedPivotIsCarried() {
    final AnchoredPlacement placement = placement(Map.of("body", 0.5f, "leg", 1.0f));
    final Map<String, Vector3f> posed = new HashMap<>(PIVOTS);
    posed.put("leg", new Vector3f(2, 4, -3));

    final AnchoredPlacement.Shift shift =
        placement.shiftOf("leg", name -> new Vector3f(posed.get(name))).orElseThrow();

    assertVector(new Vector3f(1, 6, -1.5f), shift.to());
    assertVector(new Vector3f(2, 4, -3), shift.from());
  }

  @Test
  @DisplayName("unscaled slots leave every joint where it was")
  void unscaledSlotsMoveNothing() {
    final AnchoredPlacement placement = placement(Map.of());

    final AnchoredPlacement.Shift shift = placement.shiftOf("head", POSED).orElseThrow();

    assertVector(shift.from(), shift.to());
    assertEquals(1.0f, shift.scale(), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"body", "eyelid", "missing"})
  @DisplayName("the root and bones without an anchor get no shift")
  void unanchoredBonesGetNoShift(final String bone) {
    assertTrue(placement(Map.of("body", 0.5f)).shiftOf(bone, POSED).isEmpty());
  }

  private static AnchoredPlacement placement(final Map<String, Float> scales) {
    final Map<String, String> parents = new HashMap<>();
    parents.put("body", null);
    parents.put("eyelid", "head");
    final GrowthScaleTree tree = GrowthScaleTree.of(ANCHORS.logicalParents(parents), scales);
    return new AnchoredPlacement(ANCHORS, PIVOTS.get("body"), tree);
  }

  private static void assertVector(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON, "x");
    assertEquals(expected.y, actual.y, EPSILON, "y");
    assertEquals(expected.z, actual.z, EPSILON, "z");
  }
}
