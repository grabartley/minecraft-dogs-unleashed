package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Bounds;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ToyHoldTest {

  private static final Bounds TOY = bounds(-0.5f, 0.0f, -0.25f, 0.5f, 0.2f, 0.75f);

  @ParameterizedTest(name = "{0}")
  @CsvSource({"tennis_ball, BALL", "frisbee, FRISBEE", "stick, STICK"})
  @DisplayName("each fetch toy id maps to its own hold")
  void toyIdsMapToTheirHold(final String path, final ToyHold expected) {
    assertEquals(expected, ToyHold.forToy(path));
  }

  @Test
  @DisplayName("an unknown toy fails loudly rather than borrowing another toy's hold")
  void unknownToyFails() {
    assertThrows(IllegalArgumentException.class, () -> ToyHold.forToy("bone"));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ToyHold.class,
      names = {"BALL", "STICK"})
  @DisplayName("the ball and stick are held by their centre")
  void ballAndStickAreHeldByTheirCentre(final ToyHold hold) {
    final Vec3d point = hold.point(TOY, -1);

    assertEquals(0.0, point.x, 1e-6);
    assertEquals(0.1, point.y, 1e-6);
    assertEquals(0.25, point.z, 1e-6);
  }

  @ParameterizedTest(name = "facing {0}")
  @CsvSource({"-1, 0.75", "1, -0.25"})
  @DisplayName("the frisbee is held by the middle of its rim nearest the dog")
  void frisbeeIsHeldByItsNearRim(final int forward, final double rimZ) {
    final Vec3d hold = ToyHold.FRISBEE.point(TOY, forward);

    assertEquals(0.0, hold.x, 1e-6);
    assertEquals(0.1, hold.y, 1e-6);
    assertEquals(rimZ, hold.z, 1e-6);
  }

  @ParameterizedTest(name = "facing {0}")
  @ValueSource(ints = {-1, 1})
  @DisplayName("only the frisbee droops, and always away from the dog")
  void onlyTheFrisbeeDroops(final int forward) {
    assertEquals(0.0f, ToyHold.BALL.droopDegrees(forward));
    assertEquals(0.0f, ToyHold.STICK.droopDegrees(forward));
    assertEquals(Math.signum(forward), Math.signum(ToyHold.FRISBEE.droopDegrees(forward)));
  }

  private static Bounds bounds(
      final float x0,
      final float y0,
      final float z0,
      final float x1,
      final float y1,
      final float z1) {
    final Bounds bounds = new Bounds();
    bounds.include(new Vector3f(x0, y0, z0));
    bounds.include(new Vector3f(x1, y1, z1));
    return bounds;
  }
}
