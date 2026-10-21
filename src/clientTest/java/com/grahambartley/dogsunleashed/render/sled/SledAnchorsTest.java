package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledAnchorsTest {

  @Test
  @DisplayName("the shipped sled has six harness eyes across the front bar, three a side")
  void shippedSledHasSixEyes() {
    final List<Vector3f> eyes = new SledAnchors(RigProfiles::openBundled).eyes();
    assertEquals(6, eyes.size());
    assertEquals(3, eyes.stream().filter(eye -> eye.x > 0.0f).count());
    assertEquals(3, eyes.stream().filter(eye -> eye.x < 0.0f).count());
    eyes.forEach(eye -> assertEquals(17.0f / 16.0f, eye.z, 1.0e-6f));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"harness_2\":[3,2,-17],\"harness_1\":[5,2,-17]}|2|0.3125",
        "{\"seat\":[0,10,2],\"harness_1\":[5,2,-17]}|1|0.3125",
        "{\"harness_x\":[1,2,3]}|0|0",
        "{}|0|0"
      })
  @DisplayName("harness locators are read in number order, in blocks, with forward toward the team")
  void parses(final String locators, final int count, final float firstLeft) {
    final JsonObject geometry =
        JsonParser.parseString(
                "{\"minecraft:geometry\":[{\"bones\":[{\"name\":\"bridle\",\"locators\":"
                    + locators
                    + "}]}]}")
            .getAsJsonObject();
    final List<Vector3f> eyes = SledAnchors.harnessEyesOf(geometry);
    assertEquals(count, eyes.size());
    if (count > 0) {
      assertEquals(firstLeft, eyes.get(0).x, 1.0e-6f);
      assertEquals(2.0f / 16.0f, eyes.get(0).y, 1.0e-6f);
      assertEquals(17.0f / 16.0f, eyes.get(0).z, 1.0e-6f);
    }
  }

  @Test
  @DisplayName("a missing geometry file gives no eyes rather than failing")
  void missingFile() {
    assertEquals(List.of(), new SledAnchors(id -> Optional.empty()).eyes());
  }

  @Test
  @DisplayName("a broken geometry file gives no eyes rather than failing")
  void brokenFile() {
    assertEquals(
        List.of(), new SledAnchors(id -> Optional.of(new StringReader("{not json"))).eyes());
  }
}
