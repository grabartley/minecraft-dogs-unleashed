package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.cross.CrossProportions.SlotSizes;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class RigSlotSizesTest {

  private static final float EPSILON = 1.0e-5f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final RigProfile PROFILE =
      RigProfile.parse(
          JsonParser.parseString(
                  """
                  {
                    "torso": ["body"],
                    "head": ["head"],
                    "leg_front_left": ["leg"]
                  }
                  """)
              .getAsJsonObject());

  @Test
  @DisplayName(
      "each slot is measured by its longest side, legs by the belly's height off the ground")
  void slotsAreMeasuredAgainstHeight() {
    final SlotSizes sizes = RigSlotSizes.measure(wolf(), PROFILE);

    assertEquals(0.25f / 0.875f, sizes.head(), EPSILON);
    assertEquals(0.375f / 0.875f, sizes.torso(), EPSILON);
    assertEquals(0.375f / 0.875f, sizes.legs(), EPSILON);
    assertEquals(0.0f, sizes.tail(), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"CROSS_BREED"},
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("every shipped rig has a head, torso, legs and tail to pull toward")
  void everyRigMeasures(final UnleashedDogBreed rig) {
    final SlotSizes sizes =
        RigSlotSizes.measure(ShippedRigs.model(rig.serializedId()), BUNDLED.profileOf(rig));

    assertTrue(sizes.head() > 0.0f && sizes.torso() > 0.0f, rig + " " + sizes);
    assertTrue(sizes.legs() > 0.0f && sizes.tail() > 0.0f, rig + " " + sizes);
  }

  @Test
  @DisplayName("a dachshund stands on shorter legs for its height than a golden retriever")
  void dachshundLegsAreShorterThanGoldenLegs() {
    final SlotSizes dachshund =
        RigSlotSizes.measure(
            ShippedRigs.model("dachshund"), BUNDLED.profileOf(UnleashedDogBreed.DACHSHUND));
    final SlotSizes golden =
        RigSlotSizes.measure(
            ShippedRigs.model("goldenretriever"),
            BUNDLED.profileOf(UnleashedDogBreed.GOLDEN_RETRIEVER));

    assertTrue(dachshund.legs() < golden.legs(), dachshund + " against " + golden);
  }

  @Test
  @DisplayName("a rig is measured once until the cache is cleared")
  void measuresOncePerRig() {
    final BakedGeoModel model = wolf();

    final SlotSizes first = RigSlotSizes.of(model, PROFILE);
    assertSame(first, RigSlotSizes.of(model, PROFILE));
    RigSlotSizes.clear();
    assertEquals(first, RigSlotSizes.of(model, PROFILE));
  }

  @Test
  @DisplayName("two separately built copies of a rig are measured and cached on their own")
  void copiesOfARigAreCachedApart() {
    final BakedGeoModel live = ShippedRigs.model("goldenretriever");
    final BakedGeoModel copy = ShippedRigs.model("goldenretriever");
    final RigProfile profile = BUNDLED.profileOf(UnleashedDogBreed.GOLDEN_RETRIEVER);

    assertNotSame(RigSlotSizes.of(live, profile), RigSlotSizes.of(copy, profile));
  }

  private static BakedGeoModel wolf() {
    final GeoBone body = bone(null, "body", 8, 0.375f, 0.75f);
    final GeoBone head = bone(body, "head", 10, 0.625f, 0.875f);
    final GeoBone leg = bone(body, "leg", 6, 0.0f, 0.375f);
    body.getChildBones().add(head);
    body.getChildBones().add(leg);
    return new BakedGeoModel(List.of(body), null);
  }

  private static GeoBone bone(
      final GeoBone parent,
      final String name,
      final float pivotY,
      final float bottom,
      final float top) {
    final GeoBone bone = new GeoBone(parent, name, false, 0.0, false, false);
    bone.updatePivot(0, pivotY, 0);
    bone.getCubes()
        .add(
            new GeoCube(
                new GeoQuad[] {face(bottom, Direction.DOWN), face(top, Direction.UP)},
                Vec3d.ZERO,
                Vec3d.ZERO,
                new Vec3d(2, (top - bottom) * 16, 2),
                0,
                false));
    return bone;
  }

  private static GeoQuad face(final float y, final Direction direction) {
    final GeoVertex[] vertices = {
      new GeoVertex(0, y, 0),
      new GeoVertex(0.125, y, 0),
      new GeoVertex(0.125, y, 0.125),
      new GeoVertex(0, y, 0.125)
    };
    return new GeoQuad(vertices, new Vector3f(0, direction.getOffsetY(), 0), direction);
  }
}
