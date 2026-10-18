package com.grahambartley.dogsunleashed.riding;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeatTransformTest {

  private static final float EPSILON = 1.0e-4f;

  private static Matrix4f cameraView() {
    return new Matrix4f().rotateX(0.4f).rotateY(-1.1f).translate(3.0f, -2.0f, 5.0f);
  }

  private static void assertClose(final Vector3f expected, final Vector3f actual) {
    assertTrue(expected.distance(actual) < EPSILON, "expected " + expected + " but was " + actual);
  }

  private static void assertSameRotation(final Quaternionf expected, final Quaternionf actual) {
    assertTrue(
        Math.abs(Math.abs(expected.dot(actual)) - 1.0f) < EPSILON,
        "expected " + expected + " but was " + actual);
  }

  @Test
  @DisplayName("the seat comes out relative to the entity, whatever the camera was doing")
  void seatIsRelativeToTheEntity() {
    final Matrix4f origin = cameraView();
    final Quaternionf turn = new Quaternionf().rotateY(0.7f).rotateX(-0.2f);
    final Matrix4f seat = new Matrix4f(origin).translate(0.1f, 1.4f, -0.3f).rotate(turn);

    final SeatTransform transform = SeatTransform.between(origin, seat, 0.0f);

    assertClose(new Vector3f(0.1f, 1.4f, -0.3f), transform.offset());
    assertSameRotation(turn, transform.rotation());
  }

  @Test
  @DisplayName("the mount's render scale never stretches the rider")
  void scaleIsDropped() {
    final Matrix4f origin = cameraView();
    final Quaternionf turn = new Quaternionf().rotateY(-2.0f);
    final Matrix4f seat =
        new Matrix4f(origin).scale(2.5f).rotate(turn).translate(0.0f, 0.6f, 0.0f).scale(1.3f);

    final SeatTransform transform = SeatTransform.between(origin, seat, 0.0f);

    assertClose(turn.transform(new Vector3f(0.0f, 1.5f, 0.0f)), transform.offset());
    assertSameRotation(turn, transform.rotation());
  }

  @ParameterizedTest(name = "model turned {0} degrees")
  @CsvSource({"0", "90", "180", "-90"})
  @DisplayName("a rig drawn turned round still seats its rider facing the mount's way")
  void modelYawOffsetIsUndone(final float offset) {
    final Matrix4f origin = new Matrix4f();
    final Quaternionf facing = new Quaternionf().rotateY(0.3f);
    final Quaternionf pitch = new Quaternionf().rotateX(0.25f);
    final Matrix4f seat =
        new Matrix4f(origin)
            .rotate(facing)
            .rotateY((float) Math.toRadians(offset))
            .rotate(pitch)
            .translate(0.0f, 0.5f, 0.0f);

    final SeatTransform transform = SeatTransform.between(origin, seat, offset);

    final Quaternionf pitchSeenFromTheMount =
        new Quaternionf()
            .rotateY((float) Math.toRadians(offset))
            .mul(pitch)
            .rotateY((float) Math.toRadians(-offset));
    assertSameRotation(new Quaternionf(facing).mul(pitchSeenFromTheMount), transform.rotation());
  }

  @Test
  @DisplayName("the rider's origin hangs below the seat along the seat's own up")
  void riderOriginFollowsTheSeatTilt() {
    final Quaternionf tilt = new Quaternionf().rotateX((float) Math.toRadians(20.0));
    final SeatTransform transform = new SeatTransform(new Vector3f(0.0f, 2.0f, 0.5f), tilt);

    final Vector3f origin = transform.riderOrigin(new Vec3d(0.0, 0.6, 0.0), 0.0f);

    assertClose(
        new Vector3f(0.0f, 2.0f, 0.5f).add(tilt.transform(new Vector3f(0.0f, -0.6f, 0.0f))),
        origin);
  }

  @Test
  @DisplayName("moving a matrix stack to the rider lands on the same point as riderOrigin")
  void moveToRiderMatchesRiderOrigin() {
    final SeatTransform transform =
        new SeatTransform(
            new Vector3f(0.2f, 1.7f, -0.4f), new Quaternionf().rotateY(1.2f).rotateX(0.3f));
    final Vec3d attachment = new Vec3d(0.0, 0.6, 0.0);
    final MatrixStack matrices = new MatrixStack();

    transform.moveToRider(matrices, attachment, 25.0f);

    assertClose(
        transform.riderOrigin(attachment, 25.0f),
        matrices.peek().getPositionMatrix().getTranslation(new Vector3f()));
  }

  @Test
  @DisplayName(
      "leaning forward tips the rider's head toward the mount's front, pivoting at the seat")
  void leanTipsTheRiderForward() {
    final SeatTransform transform =
        new SeatTransform(new Vector3f(0.0f, 2.0f, 0.0f), new Quaternionf());

    final Vector3f upright = transform.riderRotation(0.0f).transform(new Vector3f(0, 1, 0));
    final Vector3f leaning = transform.riderRotation(30.0f).transform(new Vector3f(0, 1, 0));

    assertClose(new Vector3f(0, 1, 0), upright);
    assertTrue(leaning.z < -0.4f && leaning.y < 0.9f, "the lean tipped the rider to " + leaning);
  }
}
