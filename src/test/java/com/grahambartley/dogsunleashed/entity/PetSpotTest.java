package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @ParameterizedTest(name = "yaw {0}, right handed {1}, kneel {2}")
  @CsvSource({"0, true, 1", "90, true, 1", "-135, false, 1", "0, true, 0", "200, false, 0.5"})
  @DisplayName("the dog stops in front of the player, clear of their body, facing past the hand")
  void theDogStopsInFront(final float yaw, final boolean right, final float kneel) {
    final PetSpot.Place place = PetSpot.of(0, 0, yaw, right, SMALL, 0.4, kneel);
    assertTrue(forwardOf(place, yaw) > 0.0, "in front");
    assertTrue(
        Math.hypot(place.x(), place.z()) >= PetSpot.PLAYER_HALF_WIDTH + 0.4 - 1.0e-6,
        "the bodies do not overlap");
    final double faceSide = rightOf(place.faceX(), place.faceZ(), yaw);
    assertEquals(right ? 1.0 : -1.0, Math.signum(faceSide), "it faces past the patting side");
  }

  @ParameterizedTest(name = "yaw {0}, right handed {1}")
  @CsvSource({"0, true", "45, false", "180, true"})
  @DisplayName("its neck lands at the patting hand")
  void theNeckLandsAtTheHand(final float yaw, final boolean right) {
    for (final float kneel : new float[] {0.0f, 1.0f}) {
      final PetSpot.Place place = PetSpot.of(0, 0, yaw, right, SMALL, 0.2, kneel);
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
  @DisplayName("a huge dog comes alongside, so its head passes beside the player, not into them")
  void aHugeDogComesAlongside(final float yaw) {
    final PetSpot.Place place = PetSpot.of(0, 0, yaw, true, HUGE, 0.575, 0.0f);
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
}
