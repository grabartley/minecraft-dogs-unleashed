package com.grahambartley.dogsunleashed.render.armour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RestPose;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArmourPainterTest {

  private static final int ATLAS = 64;
  private static final int SX = 8;
  private static final int SY = 6;
  private static final int SZ = 10;

  private static CubeGeometry.Face face(
      final Vector3f origin,
      final Vector3f along,
      final Vector3f down,
      final Vector3f normal,
      final int u,
      final int v,
      final int width,
      final int height) {
    final Vector3f right = new Vector3f(origin).add(along);
    return new CubeGeometry.Face(
        "torso",
        new Vector3f[] {
          new Vector3f(origin), right, new Vector3f(right).add(down), new Vector3f(origin).add(down)
        },
        new float[] {
          (float) u / ATLAS,
          (float) (u + width) / ATLAS,
          (float) (u + width) / ATLAS,
          (float) u / ATLAS
        },
        new float[] {
          (float) v / ATLAS,
          (float) v / ATLAS,
          (float) (v + height) / ATLAS,
          (float) (v + height) / ATLAS
        },
        normal,
        RestPose.NONE);
  }

  private static List<CubeGeometry.Face> box(final float offsetX, final int u, final int v) {
    final float x = SX / 16.0f;
    final float y = SY / 16.0f;
    final float z = SZ / 16.0f;
    final float ox = offsetX / 16.0f;
    final List<CubeGeometry.Face> faces = new ArrayList<>();
    faces.add(
        face(
            new Vector3f(ox, y, 0),
            new Vector3f(x, 0, 0),
            new Vector3f(0, 0, z),
            new Vector3f(0, 1, 0),
            u + SZ,
            v,
            SX,
            SZ));
    faces.add(
        face(
            new Vector3f(ox, 0, 0),
            new Vector3f(x, 0, 0),
            new Vector3f(0, 0, z),
            new Vector3f(0, -1, 0),
            u + SZ + SX,
            v,
            SX,
            SZ));
    faces.add(
        face(
            new Vector3f(ox + x, y, 0),
            new Vector3f(0, 0, z),
            new Vector3f(0, -y, 0),
            new Vector3f(1, 0, 0),
            u,
            v + SZ,
            SZ,
            SY));
    faces.add(
        face(
            new Vector3f(ox, y, 0),
            new Vector3f(x, 0, 0),
            new Vector3f(0, -y, 0),
            new Vector3f(0, 0, -1),
            u + SZ,
            v + SZ,
            SX,
            SY));
    faces.add(
        face(
            new Vector3f(ox, y, z),
            new Vector3f(0, 0, -z),
            new Vector3f(0, -y, 0),
            new Vector3f(-1, 0, 0),
            u + SZ + SX,
            v + SZ,
            SZ,
            SY));
    faces.add(
        face(
            new Vector3f(ox + x, y, z),
            new Vector3f(-x, 0, 0),
            new Vector3f(0, -y, 0),
            new Vector3f(0, 0, 1),
            u + 2 * SZ + SX,
            v + SZ,
            SX,
            SY));
    return faces;
  }

  private static int at(final int[] pixels, final int x, final int y) {
    return pixels[y * ATLAS + x];
  }

  private static int[] iron() {
    return ArmourPainter.paint(
        List.of(box(0, 0, 0)), ATLAS, ATLAS, ArmourLook.IRON, ArmourPainter.Layer.BASE, false);
  }

  @Test
  @DisplayName("the bottom row of a flank is the dark hem")
  void flankEndsInAHem() {
    final int hemRow = SZ + SY - 1;

    assertEquals(ArmourLook.IRON.palette().edge(), at(iron(), SZ / 2, hemRow));
  }

  @Test
  @DisplayName("rivets sit in the row above the hem, spaced along the body")
  void rivetsLineTheHem() {
    final int rivetRow = SZ + SY - 2;

    assertEquals(ArmourLook.IRON.palette().highlight(), at(iron(), 4, rivetRow));
    assertEquals(ArmourLook.IRON.palette().shade(), at(iron(), 5, rivetRow));
  }

  @Test
  @DisplayName("a bright ridge runs down the middle of the back")
  void spineRunsAlongTheBack() {
    final int[] pixels = iron();
    final int spineColumn = SZ + SX / 2;

    IntStream.range(0, SZ)
        .forEach(
            row ->
                assertEquals(
                    ArmourLook.IRON.palette().highlight(),
                    at(pixels, spineColumn, row),
                    "row " + row));
  }

  @Test
  @DisplayName("only a strap and buckle cross the belly")
  void bellyIsOpenApartFromTheStrap() {
    final int[] pixels = iron();
    final int bellyLeft = SZ + SX;

    assertEquals(ArmourPainter.TRANSPARENT, at(pixels, bellyLeft, 0));
    assertEquals(ArmourLook.BUCKLE, at(pixels, bellyLeft + SX / 2, SZ / 2));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ArmourLook.class)
  @DisplayName("the plain and dyed layers never paint the same pixel")
  void layersNeverOverlap(final ArmourLook look) {
    final int[] base =
        ArmourPainter.paint(
            List.of(box(0, 0, 0)), ATLAS, ATLAS, look, ArmourPainter.Layer.BASE, true);
    final int[] dye =
        ArmourPainter.paint(
            List.of(box(0, 0, 0)), ATLAS, ATLAS, look, ArmourPainter.Layer.DYE, true);

    IntStream.range(0, base.length)
        .forEach(i -> assertTrue(base[i] == 0 || dye[i] == 0, "pixel " + i));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ArmourLook.class,
      names = {"IRON", "GOLD", "DIAMOND", "NETHERITE"})
  @DisplayName("metal tiers paint nothing that takes dye")
  void metalIsNeverDyed(final ArmourLook look) {
    final int[] dye =
        ArmourPainter.paint(
            List.of(box(0, 0, 0)), ATLAS, ATLAS, look, ArmourPainter.Layer.DYE, false);

    assertTrue(IntStream.of(dye).allMatch(pixel -> pixel == 0));
  }

  @Test
  @DisplayName("a face buried inside another armour cube stays clear")
  void buriedFacesAreSkipped() {
    final List<CubeGeometry.Face> first = box(0, 0, 0);
    final List<CubeGeometry.Face> second = box(SX - 2, 0, 32);

    final int[] pixels =
        ArmourPainter.paint(
            List.of(first, second), ATLAS, ATLAS, ArmourLook.IRON, ArmourPainter.Layer.BASE, false);

    assertEquals(ArmourPainter.TRANSPARENT, at(pixels, SZ / 2, SZ + SY / 2));
  }

  @Test
  @DisplayName("where two cubes share a flat side, only the first one paints it")
  void sharedSidesArePaintedOnce() {
    final List<CubeGeometry.Face> first = box(0, 0, 0);
    final List<CubeGeometry.Face> twin = box(0, 0, 32);

    final int[] pixels =
        ArmourPainter.paint(
            List.of(first, twin), ATLAS, ATLAS, ArmourLook.IRON, ArmourPainter.Layer.BASE, false);

    assertTrue(pixels[(SZ + SY / 2) * ATLAS + SZ / 2] != ArmourPainter.TRANSPARENT);
    assertEquals(ArmourPainter.TRANSPARENT, at(pixels, SZ / 2, 32 + SZ + SY / 2));
  }

  @ParameterizedTest(name = "level {0}")
  @ValueSource(ints = {1, 2})
  @DisplayName("cracks only ever spread as the armour wears down")
  void cracksAccumulate(final int level) {
    final int[] fewer = ArmourPainter.cracks(List.of(box(0, 0, 0)), ATLAS, ATLAS, level);
    final int[] more = ArmourPainter.cracks(List.of(box(0, 0, 0)), ATLAS, ATLAS, level + 1);

    IntStream.range(0, fewer.length)
        .filter(i -> fewer[i] == ArmourPainter.CRACK)
        .forEach(i -> assertEquals(ArmourPainter.CRACK, more[i], "pixel " + i));
    assertTrue(
        IntStream.of(more).filter(p -> p == ArmourPainter.CRACK).count()
            > IntStream.of(fewer).filter(p -> p == ArmourPainter.CRACK).count());
  }

  @Test
  @DisplayName("undamaged armour has no cracks")
  void noCracksWhenNew() {
    assertTrue(
        IntStream.of(ArmourPainter.cracks(List.of(box(0, 0, 0)), ATLAS, ATLAS, 0))
            .allMatch(pixel -> pixel == 0));
  }
}
