package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

class GrowthRigScaleTest {

  private static final float EPSILON = 1.0e-5f;
  private static final float JOINT_EPSILON = 1.0e-4f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final RigProfile PROFILE =
      RigProfile.parse(
          JsonParser.parseString(
                  """
                  {
                    "head": ["head"],
                    "leg_front_left": ["leg"]
                  }
                  """)
              .getAsJsonObject());

  @Test
  @DisplayName("unscaled slots leave every bone, the lift and the height alone")
  void unscaledSlotsChangeNothing() {
    final GrowthRigScale scale = GrowthRigScale.build(wolf(), PROFILE, SlotScales.NONE);

    assertEquals(1.0f, scale.localScale("leg"), EPSILON);
    assertEquals(1.0f, scale.localScale("head"), EPSILON);
    assertEquals(0.0f, scale.lift(), EPSILON);
    assertEquals(1.0f, scale.heightScale(), EPSILON);
  }

  @Test
  @DisplayName("each profile slot gets its own scale, and bones outside every slot keep theirs")
  void slotsScaleTheirBones() {
    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), PROFILE, new SlotScales(1.25f, 1.0f, 0.8f, 1.0f));

    assertEquals(1.25f, scale.localScale("head"), EPSILON);
    assertEquals(0.8f, scale.localScale("leg"), EPSILON);
    assertEquals(1.0f, scale.localScale("body"), EPSILON);
  }

  @Test
  @DisplayName("longer legs lift the body by the extra length of the leg")
  void longerLegsLiftTheBody() {
    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), PROFILE, new SlotScales(1.0f, 1.0f, 1.2f, 1.0f));

    assertEquals(0.375f * 0.2f, scale.lift(), EPSILON);
  }

  @Test
  @DisplayName("a rig whose profile names no legs gets no ground fit")
  void noLegsMeansNoGroundFit() {
    final RigProfile headOnly =
        RigProfile.parse(JsonParser.parseString("{\"head\": [\"head\"]}").getAsJsonObject());

    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), headOnly, new SlotScales(1.0f, 1.0f, 1.2f, 1.0f));

    assertEquals(0.0f, scale.lift(), EPSILON);
    assertEquals(1.0f, scale.heightScale(), EPSILON);
  }

  @ParameterizedTest(name = "head {0}, legs {1}")
  @CsvSource({"1.1, 0.9", "0.8, 1.2", "1.0, 1.0"})
  @DisplayName("the same slot scales on the same rig are built once and shared")
  void sameScalesAreShared(final float head, final float legs) {
    final BakedGeoModel model = wolf();

    final GrowthRigScale first =
        GrowthRigScale.of(model, PROFILE, new SlotScales(head, 1.0f, legs, 1.0f));

    assertSame(first, GrowthRigScale.of(model, PROFILE, new SlotScales(head, 1.0f, legs, 1.0f)));
    assertEquals(head, first.localScale("head"), EPSILON);
    assertEquals(legs, first.localScale("leg"), EPSILON);
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("flatRigPoses")
  @DisplayName("on a flat rig every loose part stays joined to the part it hangs off")
  void looseRigPartsStayJoined(final UnleashedDogBreed rig, final String animation) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final GrowthRigScale scale =
        GrowthRigScale.build(
            model, BUNDLED.profileOf(rig), new SlotScales(1.2f, 0.8f, 1.25f, 0.8f));
    ShippedRigs.pose(model, rig.serializedId(), animation, name -> model.getBone(name).isPresent());

    assertFalse(anchorsOf(model, scale).isEmpty(), rig + " has loose parts");
    for (final Map.Entry<String, String> joint : anchorsOf(model, scale).entrySet()) {
      final GeoBone part = model.getBone(joint.getKey()).orElseThrow();
      final GeoBone anchor = model.getBone(joint.getValue()).orElseThrow();
      final Vector3f joinedAt = animated(part).transformPosition(pivotOf(part));
      final Vector3f onPart = rendered(scale, part).transformPosition(pivotOf(part));
      final Vector3f onAnchor =
          rendered(scale, anchor)
              .transformPosition(animated(anchor).invert().transformPosition(joinedAt));

      assertEquals(onAnchor.x, onPart.x, JOINT_EPSILON, joint + " x");
      assertEquals(onAnchor.y, onPart.y, JOINT_EPSILON, joint + " y");
      assertEquals(onAnchor.z, onPart.z, JOINT_EPSILON, joint + " z");
    }
  }

  @Test
  @DisplayName("shorter legs on a flat rig lower the body so the feet stay on the ground")
  void flatRigLegsKeepFeetOnTheGround() {
    final GrowthRigScale scale =
        GrowthRigScale.build(
            ShippedRigs.model("beagle"),
            BUNDLED.profileOf(UnleashedDogBreed.BEAGLE),
            new SlotScales(1.0f, 1.0f, 0.8f, 1.0f));

    assertTrue(scale.lift() < 0.0f, "lift " + scale.lift());
  }

  @Test
  @DisplayName("a rig nested under one root bone anchors nothing and shifts nothing")
  void nestedRigShiftsNothing() {
    final GrowthRigScale scale =
        GrowthRigScale.build(wolf(), PROFILE, new SlotScales(1.25f, 1.0f, 0.8f, 1.0f));

    assertTrue(scale.shiftOf("head").isEmpty());
    assertTrue(scale.shiftOf("leg").isEmpty());
  }

  static Stream<Arguments> flatRigPoses() {
    return Stream.of(
            UnleashedDogBreed.BEAGLE,
            UnleashedDogBreed.DACHSHUND,
            UnleashedDogBreed.GOLDEN_RETRIEVER,
            UnleashedDogBreed.SHIBA_INU,
            UnleashedDogBreed.HUSKY)
        .flatMap(
            rig ->
                Stream.of("idle", "sit", "sleep", "play_dead")
                    .map(animation -> Arguments.of(rig, animation)));
  }

  private static Map<String, String> anchorsOf(
      final BakedGeoModel model, final GrowthRigScale scale) {
    final Map<String, String> anchors = new HashMap<>();
    for (final GeoBone bone : model.topLevelBones()) {
      scale
          .shiftOf(bone.getName())
          .flatMap(ignored -> scale.anchorOf(bone.getName()))
          .ifPresent(anchor -> anchors.put(bone.getName(), anchor));
    }
    return anchors;
  }

  private static Vector3f pivotOf(final GeoBone bone) {
    return new Vector3f(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ()).div(16.0f);
  }

  private static Matrix4f animated(final GeoBone bone) {
    final MatrixStack stack = new MatrixStack();
    RenderUtil.prepMatrixForBone(stack, bone);
    return new Matrix4f(stack.peek().getPositionMatrix());
  }

  private static Matrix4f rendered(final GrowthRigScale scale, final GeoBone bone) {
    final MatrixStack stack = new MatrixStack();
    final Optional<AnchoredPlacement.Shift> shift = scale.shiftOf(bone.getName());
    if (shift.isPresent()) {
      final Vector3f to = shift.get().to().div(16.0f);
      final Vector3f from = shift.get().from().div(16.0f);
      stack.translate(to.x, to.y, to.z);
      stack.scale(shift.get().scale(), shift.get().scale(), shift.get().scale());
      stack.translate(-from.x, -from.y, -from.z);
    } else {
      final Vector3f pivot = pivotOf(bone);
      final float local = scale.localScale(bone.getName());
      stack.translate(pivot.x, pivot.y, pivot.z);
      stack.scale(local, local, local);
      stack.translate(-pivot.x, -pivot.y, -pivot.z);
    }
    RenderUtil.prepMatrixForBone(stack, bone);
    return new Matrix4f(stack.peek().getPositionMatrix());
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
