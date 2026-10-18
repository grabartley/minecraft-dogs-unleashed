package com.grahambartley.dogsunleashed.render.overlay;

import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.Direction;

public final class OverlaySheet {

  public static final List<Slot> SLOTS =
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
          Slot.LEG_BACK_RIGHT);

  public static final List<Direction> FACES =
      List.of(
          Direction.DOWN,
          Direction.UP,
          Direction.NORTH,
          Direction.SOUTH,
          Direction.WEST,
          Direction.EAST);

  public static final int SLOT_SIZE = 48;
  public static final int SLOT_COLUMNS = 4;
  public static final int WIDTH = SLOT_COLUMNS * SLOT_SIZE;
  public static final int HEIGHT = Math.ceilDiv(SLOTS.size(), SLOT_COLUMNS) * SLOT_SIZE;

  private static final int FACE_COLUMNS = 3;
  private static final int FACE_ROWS = 2;
  private static final int FACE_WIDTH = SLOT_SIZE / FACE_COLUMNS;
  private static final int FACE_HEIGHT = SLOT_SIZE / FACE_ROWS;

  private OverlaySheet() {}

  public record Cell(int x, int y, int width, int height) {}

  public static Optional<Cell> cellOf(final Slot slot, final Direction face) {
    final int square = SLOTS.indexOf(slot);
    if (square < 0) {
      return Optional.empty();
    }
    final int squareX = (square % SLOT_COLUMNS) * SLOT_SIZE;
    final int squareY = (square / SLOT_COLUMNS) * SLOT_SIZE;
    final int faceIndex = FACES.indexOf(face);
    return Optional.of(
        new Cell(
            squareX + (faceIndex % FACE_COLUMNS) * FACE_WIDTH,
            squareY + (faceIndex / FACE_COLUMNS) * FACE_HEIGHT,
            FACE_WIDTH,
            FACE_HEIGHT));
  }

  public static boolean fits(final PixelGrid sheet) {
    return sheet.hasSize(WIDTH, HEIGHT);
  }
}
