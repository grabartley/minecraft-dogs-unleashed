package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

class LivePoseTest {

  private static final float EPSILON = 1.0e-5f;

  static Stream<Arguments> posedBones() {
    return Stream.of(
        Arguments.of("husky", "sit", "neck"),
        Arguments.of("husky", "sleep", "head"),
        Arguments.of("husky", "sit", "lowestbackleftleg"),
        Arguments.of("beagle", "sit", "pescoco"),
        Arguments.of("beagle", "sleep", "eyelid"),
        Arguments.of("goldenretriever", "sit", "Torso"),
        Arguments.of("dachshund", "sleep", "bone4"));
  }

  @ParameterizedTest(name = "{0} {1} {2}")
  @MethodSource("posedBones")
  @DisplayName("a posed bone lands where GeckoLib's own renderer puts it")
  void matchesGeckoLibsBoneMatrix(final String rig, final String animation, final String bone) {
    final BakedGeoModel model = ShippedRigs.model(rig);
    ShippedRigs.pose(model, rig, animation);
    final GeoBone target = model.getBone(bone).orElseThrow();

    final Deque<GeoBone> rootToBone = new ArrayDeque<>();
    for (GeoBone current = target; current != null; current = current.getParent()) {
      rootToBone.push(current);
    }
    final MatrixStack stack = new MatrixStack();
    rootToBone.forEach(current -> RenderUtil.prepMatrixForBone(stack, current));
    final Matrix4f expected = stack.peek().getPositionMatrix();

    final Matrix4f actual = LivePose.of(target);

    assertTrue(expected.equals(actual, EPSILON), expected + " vs " + actual);
  }
}
