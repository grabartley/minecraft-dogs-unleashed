package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class HeadTopLocatorTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  @Test
  @DisplayName("a point inside a quad is covered and one outside is not")
  void coversInsideOnly() {
    final Vector3f[] square = {
      new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(1, 0, 1), new Vector3f(0, 0, 1)
    };

    assertTrue(HeadTopLocator.coversXz(square, 0.5f, 0.5f));
    assertFalse(HeadTopLocator.coversXz(square, 1.5f, 0.5f));
  }

  static Stream<UnleashedDogBreed> rigs() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(rig -> rig != UnleashedDogBreed.CROSS_BREED);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every rig has a head top on one of its head bones")
  void everyRigHasAHeadTop(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final HeadTop top = HeadTopLocator.locate(model, BUNDLED.profileOf(rig)).orElseThrow();

    assertTrue(BUNDLED.profileOf(rig).bonesIn(Slot.HEAD).contains(top.boneName()));
    assertTrue(top.headWidth() > 0.0f);
  }

  @ParameterizedTest(name = "{0} has {1} rising ears")
  @CsvSource({
    "HUSKY, 2",
    "SHIBA_INU, 2",
    "PINSCHER, 2",
    "DIRE_WOLF, 2",
    "GOLDEN_RETRIEVER, 0",
    "BEAGLE, 0",
    "DACHSHUND, 0"
  })
  @DisplayName("pointed ears rise above the crown and drop ears do not")
  void pointedEarsRise(final UnleashedDogBreed rig, final int rising) {
    assertEquals(rising, risingEarCount(rig));
  }

  private static int risingEarCount(final UnleashedDogBreed rig) {
    return HeadTopLocator.locate(ShippedRigs.model(rig.serializedId()), BUNDLED.profileOf(rig))
        .orElseThrow()
        .risingEars()
        .size();
  }
}
