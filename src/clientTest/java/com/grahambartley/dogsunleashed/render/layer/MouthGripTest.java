package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.GeoBone;

class MouthGripTest {

  private static final double TOLERANCE = 0.005;

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({
    "husky, snout, -1",
    "dachshund, mouth, 1",
    "beagle, snout, -1",
    "goldenretriever, snout, -1",
    "shibainu, snout, -1",
    "direwolf, snout, -1"
  })
  @DisplayName("the grip sits low in the drawn muzzle, just behind its tip, between its sides")
  void gripSitsAtTheFrontOfTheJaw(final String rig, final String bone, final int forward) {
    final GeoBone mouth = ShippedRigs.model(rig).getBone(bone).orElseThrow();
    final Vec3d grip = MouthGrip.of(mouth, forward);
    final double[][] box = muzzleBox(rig, bone, mouth);

    final double front = forward > 0 ? box[1][2] : box[0][2];
    final double depth = box[1][2] - box[0][2];

    assertEquals((box[0][0] + box[1][0]) / 2, grip.x, TOLERANCE, "centred between the sides");
    assertEquals(
        box[0][1] + (box[1][1] - box[0][1]) * MouthGrip.JAW_HEIGHT_FRACTION,
        grip.y,
        TOLERANCE,
        "low in the jaw");
    assertEquals(
        front - forward * depth * MouthGrip.BITE_DEPTH_FRACTION,
        grip.z,
        TOLERANCE,
        "just behind the muzzle tip");
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
      for (final JsonElement cubeElement : json.getAsJsonArray("cubes")) {
        final JsonObject cube = cubeElement.getAsJsonObject();
        final double inflate = cube.has("inflate") ? cube.get("inflate").getAsDouble() : 0.0;
        final double[] origin = triple(cube.getAsJsonArray("origin"));
        final double[] size = triple(cube.getAsJsonArray("size"));
        final double[] rotation =
            cube.has("rotation") ? triple(cube.getAsJsonArray("rotation")) : new double[3];
        final double[] cubePivot =
            cube.has("pivot")
                ? triple(cube.getAsJsonArray("pivot"))
                : new double[] {
                  origin[0] + size[0] / 2, origin[1] + size[1] / 2, origin[2] + size[2] / 2
                };
        for (int corner = 0; corner < 8; corner++) {
          final double[] p = new double[3];
          for (int axis = 0; axis < 3; axis++) {
            final boolean high = ((corner >> axis) & 1) == 1;
            p[axis] = high ? origin[axis] + size[axis] + inflate : origin[axis] - inflate;
          }
          p[0] = -p[0];
          final double[] turned =
              geckolibRotate(p, new double[] {-cubePivot[0], cubePivot[1], cubePivot[2]}, rotation);
          for (int axis = 0; axis < 3; axis++) {
            min[axis] = Math.min(min[axis], turned[axis]);
            max[axis] = Math.max(max[axis], turned[axis]);
          }
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

  private static double[] triple(final JsonArray array) {
    return new double[] {
      array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()
    };
  }

  private static double[] geckolibRotate(
      final double[] mirrored, final double[] mirroredPivot, final double[] degrees) {
    final double rx = Math.toRadians(-degrees[0]);
    final double ry = Math.toRadians(-degrees[1]);
    final double rz = Math.toRadians(degrees[2]);
    final double x = mirrored[0] - mirroredPivot[0];
    final double y = mirrored[1] - mirroredPivot[1];
    final double z = mirrored[2] - mirroredPivot[2];
    final double y1 = y * Math.cos(rx) - z * Math.sin(rx);
    final double z1 = y * Math.sin(rx) + z * Math.cos(rx);
    final double x2 = x * Math.cos(ry) + z1 * Math.sin(ry);
    final double z2 = -x * Math.sin(ry) + z1 * Math.cos(ry);
    final double x3 = x2 * Math.cos(rz) - y1 * Math.sin(rz);
    final double y3 = x2 * Math.sin(rz) + y1 * Math.cos(rz);
    return new double[] {x3 + mirroredPivot[0], y3 + mirroredPivot[1], z2 + mirroredPivot[2]};
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
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
