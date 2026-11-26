package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PetSpotTest {

  private static final DogHeadReach SMALL = new DogHeadReach(0.28f, 0.59f, 0.1f, 0.66f);
  private static final DogHeadReach HUGE = new DogHeadReach(0.7f, 1.49f, 0.25f, 1.26f);

  private static double forwardOf(final PetSpot.Place place, final float yaw) {
    final double rad = Math.toRadians(yaw);
    return -place.x() * Math.sin(rad) + place.z() * Math.cos(rad);
  }

  private static double rightOf(final double x, final double z, final float yaw) {
    final double rad = Math.toRadians(yaw);
    return -x * Math.cos(rad) - z * Math.sin(rad);
  }

  @ParameterizedTest(name = "yaw {0}, right handed {1}, kneel {2}, bows {3}")
  @CsvSource({
    "0, true, 1, false",
    "90, true, 1, false",
    "-135, false, 1, false",
    "0, true, 0, true",
    "200, false, 0, true"
  })
  @DisplayName(
      "for a kneel or a bow the dog stops in front, clear of the body, facing past the hand")
  void theDogStopsInFront(
      final float yaw, final boolean right, final float kneel, final boolean bows) {
    final PetSpot.Place place = PetSpot.of(0, 0, yaw, right, SMALL, 0.4, kneel, bows);
    assertTrue(forwardOf(place, yaw) > 0.0, "in front");
    assertTrue(
        Math.hypot(place.x(), place.z()) >= PetSpot.PLAYER_HALF_WIDTH + 0.4 - 1.0e-6,
        "the bodies do not overlap");
    final double faceSide = rightOf(place.faceX(), place.faceZ(), yaw);
    assertEquals(right ? 1.0 : -1.0, Math.signum(faceSide), "it faces past the patting side");
  }

  @ParameterizedTest(name = "yaw {0}, right handed {1}")
  @CsvSource({"0, true", "45, false", "180, true"})
  @DisplayName("for a kneel or a bow its neck lands at the patting hand")
  void theNeckLandsAtTheHand(final float yaw, final boolean right) {
    for (final float kneel : new float[] {0.0f, 1.0f}) {
      final PetSpot.Place place = PetSpot.of(0, 0, yaw, right, SMALL, 0.2, kneel, kneel == 0.0f);
      final double dx = place.faceX() - place.x();
      final double dz = place.faceZ() - place.z();
      final double length = Math.hypot(dx, dz);
      final double neckX = place.x() + dx / length * SMALL.neck();
      final double neckZ = place.z() + dz / length * SMALL.neck();
      assertTrue(
          PetSpot.reachesNeck(Math.hypot(neckX, neckZ), 0.0, kneel),
          "neck in reach at kneel " + kneel);
    }
  }

  @ParameterizedTest(name = "yaw {0}")
  @CsvSource({"0", "90", "-60"})
  @DisplayName("a bowing dog's head passes beside the player, not into them")
  void aBowingHeadPassesBeside(final float yaw) {
    final PetSpot.Place place = PetSpot.of(0, 0, yaw, true, HUGE, 0.575, 0.0f, true);
    final double dx = place.faceX() - place.x();
    final double dz = place.faceZ() - place.z();
    final double length = Math.hypot(dx, dz);
    final double snoutX = place.x() + dx / length * HUGE.snout();
    final double snoutZ = place.z() + dz / length * HUGE.snout();
    assertTrue(
        Math.hypot(snoutX, snoutZ) > PetSpot.PLAYER_HALF_WIDTH,
        "snout at " + snoutX + ", " + snoutZ);
    assertTrue(rightOf(snoutX, snoutZ, yaw) > 0.0, "the head passes on the patting side");
  }

  @ParameterizedTest(name = "yaw {0}, right handed {1}")
  @CsvSource({"0, true", "90, false", "-135, true", "200, false"})
  @DisplayName(
      "a dog the player stands for comes alongside on the patting side, its neck by the hand")
  void aStandingPatBringsTheDogAlongside(final float yaw, final boolean right) {
    final PetSpot.Place place = PetSpot.of(0, 0, yaw, right, HUGE, 0.575, 0.0f, false);
    final double side = rightOf(place.x(), place.z(), yaw);
    assertEquals(right ? 1.0 : -1.0, Math.signum(side), "on the patting side");
    assertTrue(Math.abs(side) >= PetSpot.PLAYER_HALF_WIDTH + 0.575 - 1.0e-6, "clear of the player");
    final float turn = MathHelper.wrapDegrees(place.faceYawDegrees() - yaw);
    assertTrue(Math.abs(turn) < 20.0f, "it faces the way the player faces, turned " + turn);
    final double neckForward = forwardOf(place, yaw) + HUGE.neck() * Math.cos(Math.toRadians(turn));
    assertEquals(PetSpot.ALONGSIDE_NECK_FORWARD, neckForward, 0.05, "its neck is by the hand");
  }

  @ParameterizedTest(name = "{0} blocks from the spot -> arrived {1}")
  @CsvSource({"0, true", "0.3, true", "0.31, false", "2, false"})
  @DisplayName("a dog close to its spot has arrived")
  void aDogNearItsSpotHasArrived(final double distance, final boolean arrived) {
    assertEquals(arrived, PetSpot.arrived(distance));
  }

  @ParameterizedTest(name = "neck {0} away, {1} up, kneel {2} -> reach {3}")
  @CsvSource({
    "0.4, 0, 1, true",
    "0.7, 0, 1, true",
    "0.8, 0, 1, false",
    "0.8, 0, 0, true",
    "0.4, 1.2, 1, false",
    "0.4, -1.2, 0, false"
  })
  @DisplayName("a dog whose neck is already within the hand's reach is petted where it stands")
  void aNeckInReachIsPettedInPlace(
      final double away, final double rise, final float kneel, final boolean reach) {
    assertEquals(reach, PetSpot.reachesNeck(away, rise, kneel));
  }

  @ParameterizedTest(name = "face point {0},{1} -> yaw {2}")
  @CsvSource({"0, 1, 0", "-1, 0, 90", "0, -1, 180", "1, 0, -90", "1, 1, -45"})
  @DisplayName("the facing yaw points the dog at its face point")
  void theFacingYawPointsAtTheFacePoint(final double dx, final double dz, final float yaw) {
    final float faced = new PetSpot.Place(2.0, 3.0, 2.0 + dx, 3.0 + dz).faceYawDegrees();
    assertEquals(0.0f, MathHelper.wrapDegrees(faced - yaw), 1.0e-4f);
  }
}
