package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class HeadTopLocatorTest {

  private static final float EPSILON = 1.0e-5f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static CubeGeometry.Bounds box(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    bounds.include(new Vector3f(minX, minY, minZ));
    bounds.include(new Vector3f(maxX, maxY, maxZ));
    return bounds;
  }

  private static HeadTop topWith(final List<HeadTopLocator.RisingEar> ears) {
    return new HeadTop(
        "head", new Vector3f(), new Quaternionf(), 4.0f, new Vector3f(0, 1, 0), ears);
  }

  @Test
  @DisplayName("an ear under the hat's footprint is hidden and one outside it is kept")
  void earsUnderTheHatAreHidden() {
    final HeadTop top =
        topWith(
            List.of(
                new HeadTopLocator.RisingEar(Set.of("inner"), box(0.5f, 1, -0.5f, 1.5f, 3, 0.5f)),
                new HeadTopLocator.RisingEar(Set.of("outer"), box(2.5f, 1, -0.5f, 3.5f, 3, 0.5f))));

    assertEquals(Set.of("inner"), top.earsUnder(2.0f));
    assertEquals(Set.of("inner", "outer"), top.earsUnder(6.0f));
  }

  @Test
  @DisplayName("a head with no rising ears hides nothing")
  void noRisingEarsHidesNothing() {
    assertTrue(topWith(List.of()).earsUnder(10.0f).isEmpty());
  }

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("only ears that stand above the crown count as rising")
  void risingEarsStandAboveTheCrown(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final HeadTop top = HeadTopLocator.locate(model, BUNDLED.profileOf(rig)).orElseThrow();

    for (final HeadTopLocator.RisingEar ear : top.risingEars()) {
      assertTrue(ear.bounds().max().y > top.crown().y + HeadTopLocator.EAR_RISE);
    }
  }

  @Test
  @DisplayName("pointed ears rise above the crown and drop ears do not")
  void pointedEarsRise() {
    assertEquals(2, risingEarCount(UnleashedDogBreed.HUSKY));
    assertEquals(2, risingEarCount(UnleashedDogBreed.DIRE_WOLF));
    assertEquals(0, risingEarCount(UnleashedDogBreed.GOLDEN_RETRIEVER));
  }

  private static int risingEarCount(final UnleashedDogBreed rig) {
    return HeadTopLocator.locate(ShippedRigs.model(rig.serializedId()), BUNDLED.profileOf(rig))
        .orElseThrow()
        .risingEars()
        .size();
  }
}
