package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.growth.RigAnchors.Extent;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class RigAnchorsTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  @ParameterizedTest(name = "{0}: {1} hangs off {2}")
  @CsvSource({
    "BEAGLE, Torso, ",
    "BEAGLE, bone, Torso",
    "BEAGLE, pescoco, Torso",
    "BEAGLE, head, pescoco",
    "BEAGLE, snout, head",
    "BEAGLE, ear2, head",
    "BEAGLE, frontleg1, Torso",
    "BEAGLE, bone3, Torso",
    "GOLDEN_RETRIEVER, head, pescoco",
    "GOLDEN_RETRIEVER, ear1, head",
    "SHIBA_INU, EAR1, head",
    "SHIBA_INU, fur, Torso",
    "DACHSHUND, head, bone4",
    "DACHSHUND, nose, head",
    "DACHSHUND, bunda, torso",
    "DACHSHUND, front_leg_1, torso",
    "HUSKY, main, ",
    "HUSKY, backleftleg, main",
    "HUSKY, frontrightleg, main"
  })
  @DisplayName("each loose part of a shipped rig hangs off the part it joins")
  void shippedRigsAnchorLoosePartsToTheirJoint(
      final UnleashedDogBreed rig, final String bone, final String anchor) {
    final RigAnchors anchors = shipped(rig);

    assertEquals(anchor, anchors.anchors().get(bone));
  }

  @Test
  @DisplayName("a rig nested under one root bone has nothing to anchor")
  void nestedRigAnchorsNothing() {
    final RigAnchors anchors = shipped(UnleashedDogBreed.DIRE_WOLF);

    assertEquals("body", anchors.root());
    assertTrue(anchors.anchors().isEmpty());
  }

  @Test
  @DisplayName("a rig with no torso bones has no root and anchors nothing")
  void noTorsoMeansNoAnchors() {
    final RigAnchors anchors =
        RigAnchors.of(
            parents("head", null, "leg", null), profile("{\"head\": [\"head\"]}"), Map.of());

    assertNull(anchors.root());
    assertTrue(anchors.anchors().isEmpty());
  }

  @ParameterizedTest(name = "a head with no neck hangs off the torso: {0}")
  @CsvSource({"head", "snout"})
  @DisplayName("a missing neck is skipped on the way up to the torso")
  void missingNeckFallsBackToTheTorso(final String bone) {
    final RigAnchors anchors =
        RigAnchors.of(
            parents("body", null, "head", null, "snout", null),
            profile("{\"torso\": [\"body\"], \"head\": [\"head\"], \"mouth\": [\"snout\"]}"),
            Map.of());

    assertEquals(bone.equals("head") ? "body" : "head", anchors.anchors().get(bone));
  }

  @Test
  @DisplayName("of two loose torso pieces the bigger one is the root")
  void biggestTorsoPieceIsTheRoot() {
    final RigAnchors anchors =
        RigAnchors.of(
            parents("chest", null, "body", null),
            profile("{\"torso\": [\"chest\", \"body\"]}"),
            Map.of(
                "chest", new Extent(new Vector3f(), 1.0f),
                "body", new Extent(new Vector3f(), 8.0f)));

    assertEquals("body", anchors.root());
    assertEquals("body", anchors.anchors().get("chest"));
  }

  @ParameterizedTest(name = "a loose part at z {0} hangs off {1}")
  @CsvSource({"-5.0, head", "0.5, body"})
  @DisplayName("a part in no slot hangs off whichever of head and torso it sits nearer")
  void unslottedPartsHangOffTheNearestBodyPart(final float z, final String anchor) {
    final RigAnchors anchors =
        RigAnchors.of(
            parents("body", null, "head", null, "tuft", null),
            profile("{\"torso\": [\"body\"], \"head\": [\"head\"]}"),
            Map.of(
                "body", new Extent(new Vector3f(0, 0, 0), 8.0f),
                "head", new Extent(new Vector3f(0, 0, -6), 1.0f),
                "tuft", new Extent(new Vector3f(0, 0, z), 0.1f)));

    assertEquals(anchor, anchors.anchors().get("tuft"));
  }

  @Test
  @DisplayName("logical parents keep real parents and add the anchors of loose parts")
  void logicalParentsAddAnchors() {
    final Map<String, String> real = parents("body", null, "head", null, "eyelid", "head");
    final RigAnchors anchors =
        RigAnchors.of(real, profile("{\"torso\": [\"body\"], \"head\": [\"head\"]}"), Map.of());

    final Map<String, String> logical = anchors.logicalParents(real);

    assertNull(logical.get("body"));
    assertEquals("body", logical.get("head"));
    assertEquals("head", logical.get("eyelid"));
  }

  private static RigAnchors shipped(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final Map<String, String> parents = new HashMap<>();
    final Map<String, Extent> extents = new HashMap<>();
    collect(model.topLevelBones(), parents, extents);
    return RigAnchors.of(parents, BUNDLED.profileOf(rig), extents);
  }

  private static void collect(
      final List<GeoBone> bones, final Map<String, String> parents, final Map<String, Extent> out) {
    for (final GeoBone bone : bones) {
      parents.put(bone.getName(), bone.getParent() == null ? null : bone.getParent().getName());
      Extent.of(bone).ifPresent(extent -> out.put(bone.getName(), extent));
      collect(bone.getChildBones(), parents, out);
    }
  }

  private static Map<String, String> parents(final String... pairs) {
    final Map<String, String> parents = new HashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      parents.put(pairs[i], pairs[i + 1]);
    }
    return parents;
  }

  private static RigProfile profile(final String json) {
    return RigProfile.parse(JsonParser.parseString(json).getAsJsonObject());
  }
}
