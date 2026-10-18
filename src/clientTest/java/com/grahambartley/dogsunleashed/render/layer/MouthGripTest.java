package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.GeoBone;

class MouthGripTest {

  private static final double SLACK = 0.05;

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({
    "husky, snout, -1",
    "dachshund, mouth, 1",
    "beagle, snout, -1",
    "goldenretriever, snout, -1",
    "shibainu, snout, -1",
    "direwolf, snout, -1"
  })
  @DisplayName("the grip sits low in the muzzle, near its front, between its sides")
  void gripSitsAtTheFrontOfTheJaw(final String rig, final String bone, final int forward) {
    final GeoBone mouth = ShippedRigs.model(rig).getBone(bone).orElseThrow();
    final Vec3d grip = MouthGrip.measure(mouth, forward);
    final double[][] box = muzzleBox(rig, bone, mouth);

    final double depth = box[1][2] - box[0][2];
    final double front = forward > 0 ? box[1][2] : box[0][2];
    assertTrue(Math.abs(grip.z - front) <= depth * 0.25 + SLACK, "near the front: " + grip);
    assertTrue(grip.y <= (box[0][1] + box[1][1]) / 2 + SLACK, "in the lower half: " + grip);
    assertTrue(grip.y >= box[0][1] - SLACK, "not below the jaw: " + grip);
    assertTrue(
        grip.x >= box[0][0] - SLACK && grip.x <= box[1][0] + SLACK, "between sides: " + grip);
  }

  private static double[][] muzzleBox(final String rig, final String bone, final GeoBone mouth) {
    final double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    final double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
    final JsonArray bones = geo(rig).getAsJsonArray("bones");
    for (final JsonElement element : bones) {
      final JsonObject json = element.getAsJsonObject();
      if (!descendsFrom(bones, json, bone) || !json.has("cubes")) {
        continue;
      }
      for (final JsonElement cube : json.getAsJsonArray("cubes")) {
        final JsonArray origin = cube.getAsJsonObject().getAsJsonArray("origin");
        final JsonArray size = cube.getAsJsonObject().getAsJsonArray("size");
        final double inflate =
            cube.getAsJsonObject().has("inflate")
                ? cube.getAsJsonObject().get("inflate").getAsDouble()
                : 0.0;
        final double[] lo = {
          -(origin.get(0).getAsDouble() + size.get(0).getAsDouble()),
          origin.get(1).getAsDouble(),
          origin.get(2).getAsDouble()
        };
        final double[] hi = {
          -origin.get(0).getAsDouble(),
          origin.get(1).getAsDouble() + size.get(1).getAsDouble(),
          origin.get(2).getAsDouble() + size.get(2).getAsDouble()
        };
        for (int axis = 0; axis < 3; axis++) {
          min[axis] = Math.min(min[axis], lo[axis] - inflate);
          max[axis] = Math.max(max[axis], hi[axis] + inflate);
        }
      }
    }
    final double[] pivot = {mouth.getPivotX(), mouth.getPivotY(), mouth.getPivotZ()};
    for (int axis = 0; axis < 3; axis++) {
      min[axis] = (min[axis] - pivot[axis]) / 16.0;
      max[axis] = (max[axis] - pivot[axis]) / 16.0;
    }
    return new double[][] {min, max};
  }

  private static boolean descendsFrom(
      final JsonArray bones, final JsonObject json, final String ancestor) {
    JsonObject current = json;
    while (current != null) {
      if (ancestor.equals(current.get("name").getAsString())) {
        return true;
      }
      final String parent = current.has("parent") ? current.get("parent").getAsString() : null;
      current = null;
      for (final JsonElement element : bones) {
        if (element.getAsJsonObject().get("name").getAsString().equals(parent)) {
          current = element.getAsJsonObject();
        }
      }
    }
    return false;
  }

  private static JsonObject geo(final String rig) {
    try (var reader =
        new InputStreamReader(
            MouthGripTest.class.getResourceAsStream(
                "/assets/dogs-unleashed/geo/" + rig + ".geo.json"),
            StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader)
          .getAsJsonObject()
          .getAsJsonArray("minecraft:geometry")
          .get(0)
          .getAsJsonObject();
    } catch (final java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
