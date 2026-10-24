package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class CharmMountTest {

  private static final float EPSILON = 1.0e-5f;
  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static BakedGeoModel naturalCoat(final String rig) {
    final RigProfile profile = BUNDLED.profileOf(UnleashedDogBreed.fromSerializedIdOrNull(rig));
    final Set<String> fur = new HashSet<>(profile.bonesIn(Slot.FUR_SHAGGY));
    fur.addAll(profile.bonesIn(Slot.FUR_OVERGROWN));
    return ShippedRigs.modelWithout(rig, fur);
  }

  private static Vector3f place(final String rig, final String mouth, final String animation) {
    final BakedGeoModel dog = naturalCoat(rig);
    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(dog, mouth, ShippedRigs.collarMask(rig)).orElseThrow();
    final CharmMount.Sides sides = CharmMount.sidesOf(dog, spot, mouth);
    if (!animation.equals("rest")) {
      ShippedRigs.pose(dog, rig, animation);
    }
    final GeoBone collar = dog.getBone(spot.boneName()).orElseThrow();
    final Matrix4f boneToModel = LivePose.of(collar);
    final Matrix4f charmToBone = CharmMount.charmToBone(spot);
    final CubeGeometry.Bounds charm =
        CubeGeometry.posedBoundsOf(ShippedRigs.model("pendant_ward").topLevelBones().get(0));
    final Vector3f push =
        CharmMount.clearanceInBone(
            PoseClearance.liveBoxesOf(dog, sides.body()),
            List.of(),
            () -> PoseClearance.liveBoxesOf(dog, sides.head()),
            boneToModel,
            charmToBone,
            charm,
            spot.facesNegativeZ());
    return boneToModel.transformDirection(new Vector3f(push));
  }

  @ParameterizedTest(name = "{0} {2}")
  @CsvSource({
    "beagle, snout, sit",
    "shibainu, snout, sit",
    "goldenretriever, snout, sit",
    "dachshund, mouth, sit",
    "dachshund, mouth, sleep"
  })
  @DisplayName("a charm the body covers comes forward until the body no longer hides it")
  void coveringBodyPushesTheCharmClear(final String rig, final String mouth, final String pose) {
    final Vector3f push = place(rig, mouth, pose);

    assertTrue(push.length() > 0.0f, rig + " should have needed a push");
    assertTrue(push.length() < 1.5f / MODEL_UNITS_PER_BLOCK, rig + " pushed too far: " + push);
  }

  @ParameterizedTest(name = "{0} {2}")
  @CsvSource({
    "beagle, snout, rest",
    "shibainu, snout, rest",
    "goldenretriever, snout, rest",
    "husky, snout, rest",
    "dachshund, mouth, rest",
    "husky, snout, sit",
    "husky, snout, howl_sit",
    "beagle, snout, sleep",
    "shibainu, snout, sleep",
    "goldenretriever, snout, sleep",
    "husky, snout, sleep"
  })
  @DisplayName("a charm the body does not cover stays exactly where the collar puts it")
  void uncoveredCharmDoesNotMove(final String rig, final String mouth, final String pose) {
    assertEquals(0.0f, place(rig, mouth, pose).length(), EPSILON);
  }

  @Test
  @DisplayName(
      "the husky's head is on the head side of its collar, so sitting never pushes through it")
  void huskyHeadIsNotBody() {
    final BakedGeoModel husky = ShippedRigs.model("husky");
    final CollarBandLocator.CollarSpot spot =
        CollarBandLocator.locate(husky, "snout", ShippedRigs.collarMask("husky")).orElseThrow();

    final CharmMount.Sides sides = CharmMount.sidesOf(husky, spot, "snout");

    assertTrue(sides.head().containsAll(Set.of("head", "snout", "nose")));
    assertTrue(sides.body().containsAll(Set.of("main", "frontchest", "belly")));
    assertFalse(sides.body().contains(spot.boneName()));
    assertFalse(sides.head().contains(spot.boneName()));
  }

  @Test
  @DisplayName("bones split by which side of the collar they sit on, measured toward the mouth")
  void bodySideFacesAwayFromTheMouth() {
    final Set<String> body =
        CharmMount.bodySide(
            Map.of(
                "chest", new Vector3f(0, 0, 1),
                "jaw", new Vector3f(0, 0, -1),
                "tail", new Vector3f(0, 1, 5),
                "ear", new Vector3f(0, 2, -0.5f)),
            new Vector3f(),
            new Vector3f(0, 0, -2));

    assertEquals(Set.of("chest", "tail"), body);
  }

  @Test
  @DisplayName("a push that would bury the charm in the head is refused")
  void refusesAPushIntoTheHead() {
    final PoseClearance.Box chest = box(new Vector3f(-1, -1, -0.5f), new Vector3f(1, 1, 0.5f));
    final PoseClearance.Box chin = box(new Vector3f(-1, -1, -3), new Vector3f(1, 1, -0.5f));
    final CubeGeometry.Bounds charm = new CubeGeometry.Bounds();
    charm.include(new Vector3f(-0.1f, -0.1f, -0.03f));
    charm.include(new Vector3f(0.1f, 0.1f, 0.03f));

    final Vector3f clear =
        CharmMount.clearanceInBone(
            List.of(chest), List.of(), List::of, new Matrix4f(), new Matrix4f(), charm, true);
    final Vector3f refused =
        CharmMount.clearanceInBone(
            List.of(chest),
            List.of(),
            () -> List.of(chin),
            new Matrix4f(),
            new Matrix4f(),
            charm,
            true);

    assertTrue(clear.z < -0.45f, "without a head in the way it comes out of the chest: " + clear);
    assertEquals(0.0f, refused.length(), EPSILON);
  }

  @Test
  @DisplayName(
      "the charm is judged by the middle of its front face, one charm depth out from the collar")
  void judgedByItsFrontFace() {
    final CubeGeometry.Bounds deep = new CubeGeometry.Bounds();
    deep.include(new Vector3f(-2.5f, -5, -0.5f).div(MODEL_UNITS_PER_BLOCK));
    deep.include(new Vector3f(2.5f, 0, 3).div(MODEL_UNITS_PER_BLOCK));
    final CollarBandLocator.CollarSpot spot =
        new CollarBandLocator.CollarSpot(
            "neck", new Vector3f(0, 0.5f, -0.25f), true, new Quaternionf());

    final Matrix4f charmToBone = CharmMount.charmToBone(spot);

    for (final Vector3f point : CharmMount.faceCentreLine(deep)) {
      assertEquals(
          -0.25f - 1.0f / (MODEL_UNITS_PER_BLOCK * 4.0f),
          charmToBone.transformPosition(new Vector3f(point)).z,
          EPSILON);
    }
  }

  @Test
  @DisplayName("a charm worn over armour comes out until its back rests on the armour's front")
  void armourShellIsClearedByTheBackFace() {
    final PoseClearance.Box shell = box(new Vector3f(-1, -1, -0.5f), new Vector3f(1, 1, 0.5f));
    final CubeGeometry.Bounds charm = new CubeGeometry.Bounds();
    charm.include(new Vector3f(-0.1f, -0.1f, -0.03f));
    charm.include(new Vector3f(0.1f, 0.1f, 0.03f));

    final Vector3f overFur =
        CharmMount.clearanceInBone(
            List.of(shell), List.of(), List::of, new Matrix4f(), new Matrix4f(), charm, true);
    final Vector3f overArmour =
        CharmMount.clearanceInBone(
            List.of(), List.of(shell), List::of, new Matrix4f(), new Matrix4f(), charm, true);

    assertTrue(overFur.z <= -0.47f && overFur.z > -0.5f, "fur lets the back sink in: " + overFur);
    assertTrue(overArmour.z <= -0.53f, "armour holds the back outside: " + overArmour);
  }

  private static PoseClearance.Box box(final Vector3f min, final Vector3f max) {
    return new PoseClearance.Box(new Matrix4f(), min, max);
  }

  @Test
  @DisplayName("placing at a scale moves to the collar spot and sizes by that scale")
  void placedToBoneScales() {
    final CollarBandLocator.CollarSpot spot =
        new CollarBandLocator.CollarSpot("neck", new Vector3f(1, 2, 3), true, new Quaternionf());

    final Matrix4f placed = CharmMount.placedToBone(spot, 0.5f);

    final Vector3f origin = placed.transformPosition(new Vector3f());
    final Vector3f unitX = placed.transformDirection(new Vector3f(1, 0, 0));
    assertEquals(1.0f, origin.x, EPSILON);
    assertEquals(2.0f, origin.y, EPSILON);
    assertEquals(3.0f, origin.z, EPSILON);
    assertEquals(0.5f, unitX.length(), EPSILON);
  }
}
