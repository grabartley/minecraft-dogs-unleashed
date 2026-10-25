package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class GrowthBoneTransformTest {

  private static final float EPSILON = 1.0e-5f;
  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  @Test
  @DisplayName("no growth leaves the stack alone")
  void noGrowthPushesNothing() {
    final MatrixStack stack = new MatrixStack();
    final GeoBone bone = beagle().getBone("head").orElseThrow();

    assertFalse(GrowthBoneTransform.push(stack, null, bone));
    assertTrue(stack.isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"head", "frontleg1", "snout", "Torso"})
  @DisplayName("unscaled slots leave every pivot where it was")
  void unscaledSlotsMoveNothing(final String name) {
    final BakedGeoModel model = beagle();
    final GrowthRigScale growth = growth(model, SlotScales.NONE);
    final MatrixStack stack = new MatrixStack();
    final GeoBone bone = model.getBone(name).orElseThrow();

    GrowthBoneTransform.push(stack, growth, bone);

    final Vector3f pivot = pivotOf(bone);
    assertVector(pivot, matrix(stack).transformPosition(new Vector3f(pivot)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"head", "frontleg1", "snout", "ear2", "pescoco"})
  @DisplayName("an anchored bone's joint lands on its shift target at its own scale")
  void anchoredBoneLandsOnItsShift(final String name) {
    final BakedGeoModel model = beagle();
    final GrowthRigScale growth = growth(model, new SlotScales(1.2f, 0.8f, 1.25f, 0.8f));
    final MatrixStack stack = new MatrixStack();
    final GeoBone bone = model.getBone(name).orElseThrow();
    final AnchoredPlacement.Shift shift = growth.shiftOf(name).orElseThrow();

    assertTrue(GrowthBoneTransform.push(stack, growth, bone));

    final Matrix4f matrix = matrix(stack);
    assertVector(shift.to().div(16.0f), matrix.transformPosition(shift.from().div(16.0f)));
    assertEquals(shift.scale(), matrix.getScale(new Vector3f()).x, EPSILON);
  }

  @Test
  @DisplayName("the torso root scales around its own pivot")
  void rootScalesAroundItsPivot() {
    final BakedGeoModel model = beagle();
    final GrowthRigScale growth = growth(model, new SlotScales(1.0f, 0.8f, 1.0f, 1.0f));
    final MatrixStack stack = new MatrixStack();
    final GeoBone torso = model.getBone("Torso").orElseThrow();

    assertTrue(GrowthBoneTransform.push(stack, growth, torso));

    final Vector3f pivot = pivotOf(torso);
    assertVector(pivot, matrix(stack).transformPosition(new Vector3f(pivot)));
    assertEquals(0.8f, matrix(stack).getScale(new Vector3f()).x, EPSILON);
  }

  private static BakedGeoModel beagle() {
    return ShippedRigs.model("beagle");
  }

  private static GrowthRigScale growth(final BakedGeoModel model, final SlotScales scales) {
    return GrowthRigScale.build(model, BUNDLED.profileOf(UnleashedDogBreed.BEAGLE), scales);
  }

  private static Vector3f pivotOf(final GeoBone bone) {
    return new Vector3f(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ()).div(16.0f);
  }

  private static Matrix4f matrix(final MatrixStack stack) {
    return new Matrix4f(stack.peek().getPositionMatrix());
  }

  private static void assertVector(final Vector3f expected, final Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON, "x");
    assertEquals(expected.y, actual.y, EPSILON, "y");
    assertEquals(expected.z, actual.z, EPSILON, "z");
  }
}
