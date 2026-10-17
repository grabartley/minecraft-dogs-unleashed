package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.List;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class WolfArmourCubesTest {

  private static final int WIDTH = 64;
  private static final int HEIGHT = 32;
  private static final int[] BODY_UV = {18, 14, 18 + 2 * 6 + 2 * 6, 14 + 6 + 9};
  private static final int[] MANE_UV = {21, 0, 21 + 2 * 7 + 2 * 8, 7 + 6};

  private static List<List<CubeGeometry.Face>> torso() {
    return WolfArmourCubes.torso(
        TexturedModelData.of(
                WolfEntityModel.getTexturedModelData(new Dilation(0.2f)), WIDTH, HEIGHT)
            .createModel());
  }

  private static float centreY(final CubeGeometry.Face face) {
    float sum = 0.0f;
    for (final Vector3f corner : face.corners()) {
      sum += corner.y;
    }
    return sum / face.corners().length;
  }

  @Test
  void takesTheBodyAndTheManeAsTwoWholeCubes() {
    final List<List<CubeGeometry.Face>> cubes = torso();

    assertEquals(2, cubes.size());
    cubes.forEach(cube -> assertEquals(6, cube.size()));
  }

  @Test
  void upwardFacesSitOnTheWolfsBackAboveItsBelly() {
    for (final List<CubeGeometry.Face> cube : torso()) {
      final CubeGeometry.Face back =
          cube.stream().filter(face -> face.posedNormal().y > 0.9f).findFirst().orElseThrow();
      final CubeGeometry.Face belly =
          cube.stream().filter(face -> face.posedNormal().y < -0.9f).findFirst().orElseThrow();

      assertTrue(centreY(back) > centreY(belly), "The back should be above the belly");
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = ArmourLook.class,
      names = {"LEATHER", "IRON", "GOLD", "DIAMOND", "NETHERITE"})
  void paintStaysInsideTheBodyAndManeAreasOfTheTexture(final ArmourLook look) {
    final List<List<CubeGeometry.Face>> cubes = torso();
    final int[] base =
        ArmourPainter.paint(cubes, WIDTH, HEIGHT, look, ArmourPainter.Layer.BASE, false);
    final int[] dye =
        ArmourPainter.paint(cubes, WIDTH, HEIGHT, look, ArmourPainter.Layer.DYE, false);

    int painted = 0;
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        if (base[y * WIDTH + x] == 0 && dye[y * WIDTH + x] == 0) {
          continue;
        }
        painted++;
        assertTrue(inside(BODY_UV, x, y) || inside(MANE_UV, x, y), "Paint at " + x + "," + y);
      }
    }
    assertTrue(painted > 200, look + " painted only " + painted + " texels");
  }

  private static boolean inside(final int[] rect, final int x, final int y) {
    return x >= rect[0] && x < rect[2] && y >= rect[1] && y < rect[3];
  }
}
