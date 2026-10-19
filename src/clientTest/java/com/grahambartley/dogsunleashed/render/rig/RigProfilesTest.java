package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.overlay.OverlaySheet;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Overlay;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;

class RigProfilesTest {

  private static final float INSET = 0.5f / 16.0f / 16.0f;

  private static final float TOUCHING = -0.001f / 16.0f;

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private static final List<UnleashedDogBreed> RIGS =
      Arrays.stream(UnleashedDogBreed.values())
          .filter(breed -> breed != UnleashedDogBreed.CROSS_BREED)
          .toList();

  private static final Set<Slot> BODY_SLOTS =
      EnumSet.complementOf(EnumSet.of(Slot.FUR_SHAGGY, Slot.FUR_OVERGROWN));

  static Stream<UnleashedDogBreed> rigs() {
    return RIGS.stream();
  }

  static Stream<Arguments> rigsAndBodySlots() {
    return RIGS.stream().flatMap(rig -> BODY_SLOTS.stream().map(slot -> Arguments.of(rig, slot)));
  }

  static Stream<Arguments> rigsAndSidedPairs() {
    return RIGS.stream()
        .flatMap(
            rig ->
                Stream.of(
                    Arguments.of(rig, Slot.EAR_LEFT, Slot.EAR_RIGHT),
                    Arguments.of(rig, Slot.LEG_FRONT_LEFT, Slot.LEG_FRONT_RIGHT),
                    Arguments.of(rig, Slot.LEG_BACK_LEFT, Slot.LEG_BACK_RIGHT)));
  }

  static Stream<Arguments> rigsAndLegPairs() {
    return RIGS.stream()
        .flatMap(
            rig ->
                Stream.of(
                    Arguments.of(rig, Slot.LEG_FRONT_LEFT, Slot.LEG_BACK_LEFT),
                    Arguments.of(rig, Slot.LEG_FRONT_RIGHT, Slot.LEG_BACK_RIGHT)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every bone a profile names exists in that rig")
  void profileBonesExistInTheRig(final UnleashedDogBreed rig) {
    final Set<String> bones = new HashSet<>();
    collect(ShippedRigs.model(rig.serializedId()).topLevelBones(), bones);
    final RigProfile profile = BUNDLED.profileOf(rig);
    for (final Slot slot : Slot.values()) {
      assertTrue(
          bones.containsAll(profile.bonesIn(slot)),
          rig + " " + slot + " names " + profile.bonesIn(slot) + " but the rig has " + bones);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("a rideable rig leans its rider further at a gallop than at rest")
  void ridersLeanIntoTheGallop(final UnleashedDogBreed rig) {
    final RigProfile profile = BUNDLED.profileOf(rig);
    if (!profile.seats().isEmpty()) {
      assertTrue(profile.riderPose().gallop().lean() > profile.riderPose().rest().lean(), rig + "");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every seat a profile names is a bone in that rig")
  void seatsExistInTheRig(final UnleashedDogBreed rig) {
    final Set<String> bones = new HashSet<>();
    collect(ShippedRigs.model(rig.serializedId()).topLevelBones(), bones);
    final List<String> seats = BUNDLED.profileOf(rig).seats();
    assertTrue(
        bones.containsAll(seats), rig + " seats " + seats + " are not all bones in " + bones);
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndBodySlots")
  @DisplayName("every rig names at least one bone for every body slot")
  void everyBodySlotResolves(final UnleashedDogBreed rig, final Slot slot) {
    assertFalse(BUNDLED.profileOf(rig).bonesIn(slot).isEmpty(), rig + " leaves out " + slot);
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("rigsAndSidedPairs")
  @DisplayName("a left slot sits on the dog's left and its right twin on the dog's right")
  void leftAndRightSitOnTheirOwnSide(
      final UnleashedDogBreed rig, final Slot left, final Slot right) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final Vector3f forward =
        centerOf(model, profile.bonesIn(Slot.HEAD))
            .sub(centerOf(model, profile.bonesIn(Slot.TORSO)))
            .setComponent(1, 0.0f)
            .normalize();
    assertTrue(forward.isFinite(), rig + " has its head straight above its torso");
    final Vector3f leftward = new Vector3f(0.0f, 1.0f, 0.0f).cross(forward);
    final Vector3f middle = centerOf(model, profile.bonesIn(Slot.TORSO));

    assertTrue(
        centerOf(model, profile.bonesIn(left)).sub(middle).dot(leftward) > 0.0f,
        rig + " " + left + " sits on the dog's right");
    assertTrue(
        centerOf(model, profile.bonesIn(right)).sub(middle).dot(leftward) < 0.0f,
        rig + " " + right + " sits on the dog's left");
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("rigsAndLegPairs")
  @DisplayName("a front leg sits nearer the head than the back leg on the same side")
  void frontLegsSitAheadOfBackLegs(final UnleashedDogBreed rig, final Slot front, final Slot back) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final Vector3f head = centerOf(model, profile.bonesIn(Slot.HEAD));

    assertTrue(
        centerOf(model, profile.bonesIn(front)).distance(head)
            < centerOf(model, profile.bonesIn(back)).distance(head),
        rig + " " + front + " sits behind " + back);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every overlay a profile names is a texture that ships")
  void profileOverlaysExist(final UnleashedDogBreed rig) {
    for (final Overlay overlay : Overlay.values()) {
      BUNDLED
          .profileOf(rig)
          .overlay(overlay)
          .ifPresent(
              id ->
                  assertNotNull(
                      RigProfilesTest.class.getResource(
                          "/assets/" + id.getNamespace() + "/" + id.getPath()),
                      rig + " names a missing overlay " + id));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every sheet override a profile names is a texture that ships")
  void profileSheetOverridesExist(final UnleashedDogBreed rig) {
    BUNDLED.profileOf(rig).sheetOverrides().values().stream()
        .flatMap(slots -> slots.values().stream())
        .forEach(
            id ->
                assertNotNull(
                    RigProfilesTest.class.getResource(
                        "/assets/" + id.getNamespace() + "/" + id.getPath()),
                    rig + " names a missing sheet override " + id));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every overridden sheet fits the overlay layout and every override fits the rig")
  void profileSheetOverridesFit(final UnleashedDogBreed rig) throws IOException {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final int atlasWidth = (int) model.properties().textureWidth();
    final int atlasHeight = (int) model.properties().textureHeight();
    for (final Map.Entry<Identifier, Map<Slot, Identifier>> sheet :
        BUNDLED.profileOf(rig).sheetOverrides().entrySet()) {
      final BufferedImage layout = image(sheet.getKey());
      assertEquals(OverlaySheet.WIDTH, layout.getWidth(), sheet.getKey() + " width");
      assertEquals(OverlaySheet.HEIGHT, layout.getHeight(), sheet.getKey() + " height");
      for (final Identifier override : sheet.getValue().values()) {
        final BufferedImage image = image(override);
        assertEquals(atlasWidth, image.getWidth(), rig + " " + override + " width");
        assertEquals(atlasHeight, image.getHeight(), rig + " " + override + " height");
      }
    }
  }

  private static BufferedImage image(final Identifier id) throws IOException {
    try (InputStream in =
        RigProfilesTest.class.getResourceAsStream(
            "/assets/" + id.getNamespace() + "/" + id.getPath())) {
      assertNotNull(in, "missing texture " + id);
      return ImageIO.read(in);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every eyelid is a child of a head bone, so it follows the head")
  void eyelidsFollowTheHead(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    for (final String lid : profile.bonesIn(Slot.EYES)) {
      final GeoBone parent = model.getBone(lid).orElseThrow().getParent();
      assertTrue(
          parent != null && profile.bonesIn(Slot.HEAD).contains(parent.getName()),
          rig + " " + lid + " hangs from " + (parent == null ? "nothing" : parent.getName()));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("every eyelid sits inside a head cube at rest, so the eyes are open")
  void eyelidsHideInsideTheHeadAtRest(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    for (final String lid : BUNDLED.profileOf(rig).bonesIn(Slot.EYES)) {
      final GeoBone bone = model.getBone(lid).orElseThrow();
      assertNotNull(bone.getParent(), rig + " " + lid + " has no parent");
      for (final GeoCube cube : bone.getCubes()) {
        final List<Vector3f> corners = ShippedRigs.liveCorners(bone, cube);
        assertTrue(
            bone.getParent().getCubes().stream()
                .anyMatch(
                    head ->
                        sidesOutside(
                                head,
                                ShippedRigs.liveMatrix(bone.getParent(), head),
                                corners,
                                INSET)
                            .isEmpty()),
            rig + " " + lid + " pokes out of " + bone.getParent().getName() + " at rest");
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("closed eyelids only step out of the front of the head, so the rest keeps its fur")
  void closedEyelidsOnlyStepOutOfTheFrontOfTheHead(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final RigProfile profile = BUNDLED.profileOf(rig);
    final Direction front = faceOfThePupils(model, profile);
    ShippedRigs.pose(model, rig.serializedId(), "sleep", profile.bonesIn(Slot.EYES)::contains);
    final List<String> past = new ArrayList<>();
    for (final String lid : profile.bonesIn(Slot.EYES)) {
      final GeoBone bone = model.getBone(lid).orElseThrow();
      assertNotNull(bone.getParent(), rig + " " + lid + " has no parent");
      for (final GeoCube cube : bone.getCubes()) {
        final List<Vector3f> corners = ShippedRigs.liveCorners(bone, cube);
        final List<Map<Direction, Float>> outside =
            bone.getParent().getCubes().stream()
                .map(
                    head ->
                        sidesOutside(
                            head,
                            ShippedRigs.liveMatrix(bone.getParent(), head),
                            corners,
                            TOUCHING))
                .peek(sides -> sides.remove(front))
                .toList();
        if (outside.stream().noneMatch(Map::isEmpty)) {
          past.add(lid + " " + outside);
        }
      }
    }
    assertTrue(past.isEmpty(), rig + " closes eyelids past the head: " + past);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rigs")
  @DisplayName("the idle blink closes every eyelid to the pose sleep holds it in")
  void idleBlinksToTheSleepPose(final UnleashedDogBreed rig) {
    final Animation sleep = ShippedRigs.animation(rig.serializedId(), "sleep");
    final Animation idle = ShippedRigs.animation(rig.serializedId(), "idle");
    assertNotNull(sleep, rig + " has no sleep clip");
    assertNotNull(idle, rig + " has no idle clip");
    for (final String lid : BUNDLED.profileOf(rig).bonesIn(Slot.EYES)) {
      final BoneAnimation asleep = ShippedRigs.boneAnimation(sleep, lid);
      final BoneAnimation blink = ShippedRigs.boneAnimation(idle, lid);
      assertNotNull(asleep, rig + " sleep leaves " + lid + " open");
      assertNotNull(blink, rig + " idle never blinks " + lid);
      assertTrue(
          ShippedRigs.keyframeValues(asleep.rotationKeyFrames()).isEmpty()
              && ShippedRigs.keyframeValues(blink.rotationKeyFrames()).isEmpty(),
          rig + " turns " + lid + " to close it, but a closed pose is only a position and a scale");
      final List<Vector3f> closedPosition = ShippedRigs.keyframeValues(asleep.positionKeyFrames());
      final List<Vector3f> closedScale = ShippedRigs.keyframeValues(asleep.scaleKeyFrames());
      assertFalse(
          closedPosition.isEmpty() && closedScale.isEmpty(),
          rig + " sleep leaves " + lid + " open");
      assertBlinksTo(
          rig + " " + lid + " position",
          ShippedRigs.keyframeValues(blink.positionKeyFrames()),
          closedPosition,
          new Vector3f());
      assertBlinksTo(
          rig + " " + lid + " scale",
          ShippedRigs.keyframeValues(blink.scaleKeyFrames()),
          closedScale,
          new Vector3f(1.0f));
    }
  }

  @Test
  @DisplayName("a rig without a profile file gets the empty profile")
  void missingProfileIsEmpty() {
    final RigProfiles none = new RigProfiles(id -> Optional.empty());
    assertEquals(RigProfile.EMPTY, none.profileOf(UnleashedDogBreed.HUSKY));
  }

  @Test
  @DisplayName("a broken profile file is treated as empty rather than crashing the client")
  void brokenProfileIsEmpty() {
    final RigProfiles broken =
        new RigProfiles(id -> Optional.<Reader>of(new StringReader("{ not json")));
    assertEquals(RigProfile.EMPTY, broken.profileOf(UnleashedDogBreed.HUSKY));
  }

  @Test
  @DisplayName("profiles are read once and read again after a clear")
  void cachesUntilCleared() {
    final int[] reads = {0};
    final RigProfiles counting =
        new RigProfiles(
            id -> {
              reads[0]++;
              return Optional.<Reader>of(new StringReader("{\"torso\": [\"main\"]}"));
            });

    counting.profileOf(UnleashedDogBreed.HUSKY);
    counting.profileOf(UnleashedDogBreed.HUSKY);
    counting.clear();
    counting.profileOf(UnleashedDogBreed.HUSKY);

    assertEquals(2, reads[0]);
  }

  private static Vector3f centerOf(final BakedGeoModel model, final Set<String> boneNames) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final String name : boneNames) {
      for (final CubeGeometry.Face face : CubeGeometry.facesOf(model.getBone(name).orElseThrow())) {
        Arrays.stream(face.corners()).map(face.pose()::point).forEach(bounds::include);
      }
    }
    assertFalse(bounds.isEmpty(), boneNames + " have no cubes");
    return bounds.center();
  }

  private static void assertBlinksTo(
      final String what,
      final List<Vector3f> blink,
      final List<Vector3f> asleep,
      final Vector3f open) {
    if (asleep.isEmpty()) {
      assertTrue(
          blink.stream().allMatch(value -> near(value, open)), what + " moves but sleep does not");
      return;
    }
    final Vector3f closed = asleep.get(asleep.size() - 1);
    assertTrue(
        asleep.stream().allMatch(value -> near(value, closed)), what + " drifts while asleep");
    assertFalse(blink.isEmpty(), what + " never closes in idle");
    assertTrue(near(blink.get(0), open), what + " starts the idle loop closed");
    assertTrue(near(blink.get(blink.size() - 1), open), what + " ends the idle loop closed");
    assertTrue(
        blink.stream().anyMatch(value -> near(value, closed)),
        what + " blinks to " + blink + " but sleeps at " + closed);
  }

  private static boolean near(final Vector3f a, final Vector3f b) {
    return a.distance(b) < 1.0e-4f;
  }

  private static Map<Direction, Float> sidesOutside(
      final GeoCube head, final Matrix4f pose, final List<Vector3f> corners, final float margin) {
    final CubeGeometry.Bounds box = CubeGeometry.localBoundsOf(head);
    final Matrix4f toHead = new Matrix4f(pose).invert();
    final Map<Direction, Float> outside = new EnumMap<>(Direction.class);
    for (final Vector3f corner : corners) {
      final Vector3f local = toHead.transformPosition(new Vector3f(corner));
      final Vector3f below = new Vector3f(box.min()).sub(local);
      final Vector3f above = new Vector3f(local).sub(box.max());
      record(outside, Direction.WEST, below.x, margin);
      record(outside, Direction.DOWN, below.y, margin);
      record(outside, Direction.NORTH, below.z, margin);
      record(outside, Direction.EAST, above.x, margin);
      record(outside, Direction.UP, above.y, margin);
      record(outside, Direction.SOUTH, above.z, margin);
    }
    return outside;
  }

  private static void record(
      final Map<Direction, Float> outside,
      final Direction side,
      final float overshoot,
      final float margin) {
    if (overshoot > -margin) {
      outside.merge(side, overshoot * 16.0f, Math::max);
    }
  }

  private static Direction faceOfThePupils(final BakedGeoModel model, final RigProfile profile) {
    final List<SlotRegion> head = SlotRegions.of(model, profile, Slot.HEAD);
    final Set<Direction> faces = new HashSet<>();
    for (final Texel pupil : profile.pupils()) {
      head.stream()
          .filter(region -> region.contains(pupil))
          .forEach(region -> faces.add(region.face()));
    }
    assertEquals(1, faces.size(), "the pupils should sit on one head face, not " + faces);
    return faces.iterator().next();
  }

  private static void collect(final Iterable<GeoBone> bones, final Set<String> into) {
    for (final GeoBone bone : bones) {
      into.add(bone.getName());
      collect(bone.getChildBones(), into);
    }
  }
}
