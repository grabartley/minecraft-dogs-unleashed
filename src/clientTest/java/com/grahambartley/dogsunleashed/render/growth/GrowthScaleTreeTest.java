package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrowthScaleTreeTest {

  private static final Map<String, String> PARENTS = new HashMap<>();

  static {
    PARENTS.put("body", null);
    PARENTS.put("chest", "body");
    PARENTS.put("neck", "chest");
    PARENTS.put("head", "neck");
    PARENTS.put("snout", "head");
    PARENTS.put("leg", "chest");
    PARENTS.put("leg_lower", "leg");
  }

  private static final GrowthScaleTree TREE =
      GrowthScaleTree.of(
          PARENTS, Map.of("chest", 1.1f, "head", 1.25f, "leg", 0.8f, "leg_lower", 0.8f));

  @Test
  @DisplayName("a slot bone ends up at its own slot scale however deep it sits")
  void slotBonesReachTheirOwnScale() {
    assertEquals(1.1f, TREE.cumulativeScale("chest"), 1e-6f);
    assertEquals(1.25f, TREE.cumulativeScale("head"), 1e-6f);
    assertEquals(0.8f, TREE.cumulativeScale("leg_lower"), 1e-6f);
  }

  @Test
  @DisplayName("a bone outside any slot inherits its parent's scale")
  void unslottedBonesInherit() {
    assertEquals(1.0f, TREE.cumulativeScale("body"), 1e-6f);
    assertEquals(1.1f, TREE.cumulativeScale("neck"), 1e-6f);
    assertEquals(1.25f, TREE.cumulativeScale("snout"), 1e-6f);
  }

  @Test
  @DisplayName("local scales undo the parent so children never compound")
  void localScalesUndoTheParent() {
    assertEquals(1.1f, TREE.localScale("chest"), 1e-6f);
    assertEquals(1.0f, TREE.localScale("neck"), 1e-6f);
    assertEquals(1.25f / 1.1f, TREE.localScale("head"), 1e-6f);
    assertEquals(0.8f / 1.1f, TREE.localScale("leg"), 1e-6f);
    assertEquals(1.0f, TREE.localScale("leg_lower"), 1e-6f);
    assertEquals(1.0f, TREE.localScale("snout"), 1e-6f);
  }

  @Test
  @DisplayName("an unknown bone renders at its rest size")
  void unknownBoneIsUnscaled() {
    assertEquals(1.0f, TREE.localScale("missing"), 1e-6f);
  }
}
