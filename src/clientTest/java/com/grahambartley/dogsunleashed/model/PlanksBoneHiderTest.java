package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class PlanksBoneHiderTest {

  private final PlanksBoneHider hider = new PlanksBoneHider();

  @ParameterizedTest(name = "{0} reads")
  @ValueSource(ints = {1, 2, 5})
  @DisplayName("the planks bones of a baked model are hidden and the model is handed back")
  void hidesPlanksOfANewModel(final int reads) {
    final GeoBone planks = bone("planks");
    final GeoBone frame = bone("bb_main");
    final BakedGeoModel model = new BakedGeoModel(List.of(frame, planks), null);

    for (int read = 0; read < reads; read++) {
      assertSame(model, this.hider.prepare(model));
    }

    assertTrue(planks.isHidden());
    assertFalse(frame.isHidden());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"planks", "bed_planks"})
  @DisplayName("the same model is hidden once, so a bone shown again later stays shown")
  void hidesEachModelOnce(final String name) {
    final GeoBone planks = bone(name);
    final BakedGeoModel model = new BakedGeoModel(List.of(planks), null);
    this.hider.prepare(model);
    planks.setHidden(false);

    this.hider.prepare(model);

    assertFalse(planks.isHidden());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"planks", "runners_planks"})
  @DisplayName("a model baked again after a resource reload gets its planks hidden too")
  void hidesAReloadedModel(final String name) {
    this.hider.prepare(new BakedGeoModel(List.of(bone(name)), null));
    final GeoBone reloaded = bone(name);

    this.hider.prepare(new BakedGeoModel(List.of(reloaded), null));

    assertTrue(reloaded.isHidden());
  }

  private static GeoBone bone(final String name) {
    return new GeoBone(null, name, false, null, null, null);
  }
}
