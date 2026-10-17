package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.overlay.OverlaySheet.Cell;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OverlaySheetTest {

  static Stream<Arguments> slotsAndFaces() {
    return OverlaySheet.SLOTS.stream()
        .flatMap(slot -> Arrays.stream(Direction.values()).map(face -> Arguments.of(slot, face)));
  }

  @Test
  @DisplayName("the sheet holds every body slot in enum order and leaves fur out")
  void slotsAreTheBodySlots() {
    assertEquals(
        List.of(
            Slot.TORSO,
            Slot.MOUTH,
            Slot.HEAD,
            Slot.EYES,
            Slot.NECK,
            Slot.TAIL,
            Slot.EAR_LEFT,
            Slot.EAR_RIGHT,
            Slot.LEG_FRONT_LEFT,
            Slot.LEG_FRONT_RIGHT,
            Slot.LEG_BACK_LEFT,
            Slot.LEG_BACK_RIGHT),
        OverlaySheet.SLOTS);
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

  @Test
  @DisplayName("the six faces of a square fill it in direction order, three across and two down")
  void facesFillTheSquareInDirectionOrder() {
    assertEquals(
        Optional.of(new Cell(96, 0, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.DOWN));
    assertEquals(
        Optional.of(new Cell(112, 0, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.UP));
    assertEquals(
        Optional.of(new Cell(128, 0, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.NORTH));
    assertEquals(
        Optional.of(new Cell(96, 24, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.SOUTH));
    assertEquals(
        Optional.of(new Cell(112, 24, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.WEST));
    assertEquals(
        Optional.of(new Cell(128, 24, 16, 24)), OverlaySheet.cellOf(Slot.HEAD, Direction.EAST));
    assertEquals(
        Optional.of(new Cell(0, 96, 16, 24)),
        OverlaySheet.cellOf(Slot.LEG_FRONT_LEFT, Direction.DOWN));
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
