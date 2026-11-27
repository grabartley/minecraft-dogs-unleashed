package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class PlanksBonesTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "planks, true",
    "runners_planks, true",
    "handlebar_planks, true",
    "bb_main, false",
    "cushion, false",
    "planksy, false",
    "runners, false"
  })
  @DisplayName("a bone is drawn with planks when it is named planks or ends in _planks")
  void planksBonesAreNamedSo(final String name, final boolean planks) {
    assertEquals(planks, PlanksBones.isPlanks(name));
  }

  @Test
  @DisplayName("only planks bones are hidden from the model's own texture, at any depth")
  void onlyPlanksBonesAreHidden() {
    final GeoBone sled = bone(null, "sled");
    final GeoBone runners = bone(sled, "runners");
    final GeoBone runnersPlanks = bone(runners, "runners_planks");
    final GeoBone topPlanks = bone(null, "planks");
    sled.getChildBones().add(runners);
    runners.getChildBones().add(runnersPlanks);

    PlanksBones.hideIn(new BakedGeoModel(List.of(sled, topPlanks), null));

    assertFalse(sled.isHidden());
    assertFalse(runners.isHidden());
    assertTrue(runnersPlanks.isHidden());
    assertTrue(topPlanks.isHidden());
  }

  private static GeoBone bone(final GeoBone parent, final String name) {
    return new GeoBone(parent, name, false, null, null, null);
  }
}
