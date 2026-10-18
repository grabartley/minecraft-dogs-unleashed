package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

class ToyHoldTest {

  private static final double TOLERANCE = 1e-5;

  @Test
  @DisplayName("the ball is held by its centre")
  void ballIsHeldByItsCentre() {
    final Box ball = bounds(ShippedRigs.model("tennis_ball"));

    assertEquals(ball.getCenter(), ToyHold.BALL.point(-1));
  }

  @ParameterizedTest(name = "facing {0}")
  @ValueSource(ints = {-1, 1})
  @DisplayName("the frisbee is held by the middle of its rim nearest the dog")
  void frisbeeIsHeldByItsNearRim(final int forward) {
    final Box disc = bounds(ShippedRigs.model("frisbee"));
    final Vec3d hold = ToyHold.FRISBEE.point(forward);

    assertEquals(disc.getCenter().x, hold.x, TOLERANCE);
    assertEquals(disc.getCenter().y, hold.y, TOLERANCE);
    assertEquals(forward > 0 ? disc.minZ : disc.maxZ, hold.z, TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @org.junit.jupiter.params.provider.CsvSource({
    "tennis_ball, BALL",
    "frisbee, FRISBEE",
    "stick, STICK"
  })
  @DisplayName("each fetch toy id maps to its own hold")
  void toyIdsMapToTheirHold(final String path, final ToyHold expected) {
    assertEquals(expected, ToyHold.forToy(path));
  }

  @Test
  @DisplayName("the stick is held by its middle")
  void stickIsHeldByItsMiddle() {
    assertEquals(bounds(ShippedRigs.model("stick")).getCenter(), ToyHold.STICK.point(-1));
  }

  @ParameterizedTest(name = "facing {0}")
  @ValueSource(ints = {-1, 1})
  @DisplayName("only the frisbee droops, and always away from the dog")
  void onlyTheFrisbeeDroops(final int forward) {
    assertEquals(0.0f, ToyHold.BALL.droopDegrees(forward));
    assertEquals(0.0f, ToyHold.STICK.droopDegrees(forward));
    assertEquals(Math.signum(forward), Math.signum(ToyHold.FRISBEE.droopDegrees(forward)));
  }

  private static Box bounds(final BakedGeoModel model) {
    final double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    final double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
    for (final GeoBone bone : model.topLevelBones()) {
      for (final GeoCube cube : bone.getCubes()) {
        for (final GeoQuad quad : cube.quads()) {
          if (quad == null) {
            continue;
          }
          for (final GeoVertex vertex : quad.vertices()) {
            final float[] p = {vertex.position().x, vertex.position().y, vertex.position().z};
            for (int axis = 0; axis < 3; axis++) {
              min[axis] = Math.min(min[axis], p[axis]);
              max[axis] = Math.max(max[axis], p[axis]);
            }
          }
        }
      }
    }
    return new Box(min[0], min[1], min[2], max[0], max[1], max[2]);
  }
}
