package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.overlay.OverlaySheet.Cell;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OverlaySheetTest {

  static Stream<Arguments> slotsAndFaces() {
    return OverlaySheet.SLOTS.stream()
        .flatMap(slot -> Arrays.stream(Direction.values()).map(face -> Arguments.of(slot, face)));
  }

  @Test
  @DisplayName("the sheet holds every slot except fur")
  void slotsAreTheBodySlots() {
    assertEquals(
        EnumSet.complementOf(EnumSet.of(Slot.FUR_SHAGGY, Slot.FUR_OVERGROWN)),
        EnumSet.copyOf(OverlaySheet.SLOTS));
    assertEquals(OverlaySheet.SLOTS.size(), Set.copyOf(OverlaySheet.SLOTS).size());
  }

  @Test
  @DisplayName("every face direction has exactly one cell in a square")
  void facesCoverEveryDirectionOnce() {
    assertEquals(EnumSet.allOf(Direction.class), EnumSet.copyOf(OverlaySheet.FACES));
    assertEquals(Direction.values().length, OverlaySheet.FACES.size());
  }

  @Test
  @DisplayName("the sheet is four squares wide and as tall as its slots need")
  void sheetSize() {
    assertEquals(192, OverlaySheet.WIDTH);
    assertEquals(144, OverlaySheet.HEIGHT);
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("slotsAndFaces")
  @DisplayName("every face cell sits inside its slot's square")
  void cellSitsInItsSquare(final Slot slot, final Direction face) {
    final Cell cell = OverlaySheet.cellOf(slot, face).orElseThrow();
    final int square = OverlaySheet.SLOTS.indexOf(slot);
    final int squareX = (square % OverlaySheet.SLOT_COLUMNS) * OverlaySheet.SLOT_SIZE;
    final int squareY = (square / OverlaySheet.SLOT_COLUMNS) * OverlaySheet.SLOT_SIZE;

    assertTrue(cell.x() >= squareX && cell.x() + cell.width() <= squareX + OverlaySheet.SLOT_SIZE);
    assertTrue(cell.y() >= squareY && cell.y() + cell.height() <= squareY + OverlaySheet.SLOT_SIZE);
  }

  @Test
  @DisplayName("no two face cells on the sheet overlap")
  void cellsDoNotOverlap() {
    final List<Cell> cells = new ArrayList<>();
    slotsAndFaces()
        .forEach(
            arguments ->
                cells.add(
                    OverlaySheet.cellOf((Slot) arguments.get()[0], (Direction) arguments.get()[1])
                        .orElseThrow()));

    for (int i = 0; i < cells.size(); i++) {
      for (int j = i + 1; j < cells.size(); j++) {
        assertFalse(
            overlap(cells.get(i), cells.get(j)), cells.get(i) + " overlaps " + cells.get(j));
      }
    }
  }

  @ParameterizedTest(name = "{0} {1} at ({2}, {3})")
  @CsvSource({
    "HEAD, DOWN, 96, 0",
    "HEAD, UP, 112, 0",
    "HEAD, NORTH, 128, 0",
    "HEAD, SOUTH, 96, 24",
    "HEAD, WEST, 112, 24",
    "HEAD, EAST, 128, 24",
    "TORSO, DOWN, 0, 0",
    "LEG_FRONT_LEFT, DOWN, 0, 96",
    "LEG_BACK_RIGHT, EAST, 176, 120"
  })
  @DisplayName("the six faces of a square fill it three across and two down in face order")
  void facesFillTheSquareInFaceOrder(
      final Slot slot, final Direction face, final int x, final int y) {
    assertEquals(Optional.of(new Cell(x, y, 16, 24)), OverlaySheet.cellOf(slot, face));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Slot.class,
      names = {"FUR_SHAGGY", "FUR_OVERGROWN"})
  @DisplayName("fur slots have no place on the sheet")
  void furHasNoCell(final Slot slot) {
    assertTrue(OverlaySheet.cellOf(slot, Direction.NORTH).isEmpty());
  }

  @Test
  @DisplayName("only a sheet of exactly the documented size fits")
  void onlyTheExactSizeFits() {
    assertTrue(OverlaySheet.fits(PixelGrid.blank(OverlaySheet.WIDTH, OverlaySheet.HEIGHT)));
    assertFalse(OverlaySheet.fits(PixelGrid.blank(OverlaySheet.WIDTH, OverlaySheet.HEIGHT + 1)));
  }

  private static boolean overlap(final Cell a, final Cell b) {
    return a.x() < b.x() + b.width()
        && b.x() < a.x() + a.width()
        && a.y() < b.y() + b.height()
        && b.y() < a.y() + a.height();
  }
}
