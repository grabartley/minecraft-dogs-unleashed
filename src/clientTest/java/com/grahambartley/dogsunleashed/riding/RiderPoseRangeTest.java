package com.grahambartley.dogsunleashed.riding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiderPoseRangeTest {

  private static RiderPoseRange range(final String json) {
    return RiderPoseRange.parse(JsonParser.parseString(json).getAsJsonObject());
  }

  @ParameterizedTest(name = "limb speed {0} leans {1}")
  @CsvSource({"0, 10", "0.3, 20", "0.6, 30", "2, 30"})
  @DisplayName("the rider leans from the rest pose toward the gallop pose as the mount speeds up")
  void blendsWithPace(final float limbSpeed, final float lean) {
    assertEquals(
        lean,
        range("{\"rest\": {\"lean\": 10}, \"gallop\": {\"lean\": 30}}").at(limbSpeed).lean(),
        1.0e-4f);
  }

  @Test
  @DisplayName("a gallop pose fills its gaps from the rest pose, and no poses at all sits upright")
  void fallsBack() {
    final RiderPoseRange onlyRest =
        range("{\"rest\": {\"lean\": 10, \"right_arm\": [-50, 0, 0]}, \"gallop\": {\"lean\": 30}}");
    assertEquals(onlyRest.rest().rightArm(), onlyRest.gallop().rightArm());
    assertEquals(RiderPose.UPRIGHT, range("{}").at(0.6f));
  }
}
