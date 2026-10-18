package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

class MouthFrameTest {

  private static final float TOLERANCE = 1e-4f;

  static Stream<Arguments> restChains() {
    return Stream.of(
        Arguments.of("husky neck and snout", new float[][] {{-32.5f, 0, 0}, {0, 0, 0}, {5f, 0, 0}}),
        Arguments.of("level rig", new float[][] {{0, 0, 0}, {0, 0, 0}}),
        Arguments.of("tilted on every axis", new float[][] {{-20f, 15f, 0}, {10f, 0, -25f}}));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("restChains")
  @DisplayName(
      "a mouth at rest is levelled to the model frame whatever tilt the rig was built with")
  void restPoseIsLevelled(final String rig, final float[][] restDegrees) {
    final List<GeoBone> chain = chain(restDegrees);

    final MatrixStack stack = drawnAtMouth(chain);
    MouthFrame.level(stack, chain.get(chain.size() - 1));

    assertRotation(new Quaternionf(), stack);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("restChains")
  @DisplayName("a mouth animated away from rest keeps the animated part of its rotation")
  void animationIsKept(final String rig, final float[][] restDegrees) {
    final List<GeoBone> chain = chain(restDegrees);
    final GeoBone mouth = chain.get(chain.size() - 1);
    final float nod = 0.3f;
    mouth.setRotX(mouth.getRotX() + nod);

    final MatrixStack stack = drawnAtMouth(chain);
    MouthFrame.level(stack, mouth);

    final Quaternionf ancestors = new Quaternionf();
    for (final GeoBone link : chain.subList(0, chain.size() - 1)) {
      ancestors.mul(rest(link));
    }
    final Quaternionf expected =
        new Quaternionf(ancestors)
            .mul(MouthFrame.rotationOf(mouth.getRotX(), mouth.getRotY(), mouth.getRotZ()))
            .mul(rest(mouth).conjugate())
            .mul(new Quaternionf(ancestors).conjugate());
    assertRotation(expected, stack);
  }

  private static List<GeoBone> chain(final float[][] restDegrees) {
    final List<GeoBone> chain = new ArrayList<>();
    GeoBone parent = null;
    for (int i = 0; i < restDegrees.length; i++) {
      final GeoBone bone = new GeoBone(parent, "bone" + i, false, null, false, false);
      bone.updatePivot(0.5f * i, 9f + i, -3f * i);
      bone.updateRotation(
          restDegrees[i][0] * MathHelper.RADIANS_PER_DEGREE,
          restDegrees[i][1] * MathHelper.RADIANS_PER_DEGREE,
          restDegrees[i][2] * MathHelper.RADIANS_PER_DEGREE);
      bone.saveInitialSnapshot();
      chain.add(bone);
      parent = bone;
    }
    return chain;
  }

  private static MatrixStack drawnAtMouth(final List<GeoBone> chain) {
    final MatrixStack stack = new MatrixStack();
    for (final GeoBone link : chain) {
      RenderUtil.prepMatrixForBone(stack, link);
    }
    RenderUtil.translateAndRotateMatrixForBone(stack, chain.get(chain.size() - 1));
    return stack;
  }

  private static Quaternionf rest(final GeoBone bone) {
    return MouthFrame.rotationOf(
        bone.getInitialSnapshot().getRotX(),
        bone.getInitialSnapshot().getRotY(),
        bone.getInitialSnapshot().getRotZ());
  }

  private static void assertRotation(final Quaternionf expected, final MatrixStack stack) {
    final Quaternionf actual =
        stack.peek().getPositionMatrix().getNormalizedRotation(new Quaternionf());
    for (final Vector3f axis :
        List.of(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1))) {
      final Vector3f want = expected.transform(new Vector3f(axis));
      final Vector3f got = actual.transform(new Vector3f(axis));
      assertEquals(want.x, got.x, TOLERANCE);
      assertEquals(want.y, got.y, TOLERANCE);
      assertEquals(want.z, got.z, TOLERANCE);
    }
  }
}
