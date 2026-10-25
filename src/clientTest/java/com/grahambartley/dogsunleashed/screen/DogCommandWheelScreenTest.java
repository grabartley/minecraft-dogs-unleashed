package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.DogWheelIcons;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogCommandWheelScreenTest {

  private static final int SMALLEST_GUI_HEIGHT = 240;
  private static final int FONT_HEIGHT = 9;

  static Stream<Identifier> wheelIcons() {
    return Stream.of(
            Arrays.stream(DogWheelAction.values()).map(DogWheelAction::icon),
            Arrays.stream(DogTrick.values()).map(DogTrick::icon),
            Stream.of(DogWheelIcons.ACTIONS, DogExpeditions.WHEEL_ICON, DogDeliveries.WHEEL_ICON))
        .flatMap(icons -> icons);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wheelIcons")
  @DisplayName("every icon the wheel can draw ships as a GUI sprite")
  void everyIconShipsAsASprite(final Identifier icon) {
    final Path sprite =
        Path.of(
            "src/client/resources/assets",
            icon.getNamespace(),
            "textures/gui/sprites",
            icon.getPath() + ".png");
    assertTrue(Files.isRegularFile(sprite), sprite + " is missing");
  }

  @ParameterizedTest(name = "{0} sectors")
  @ValueSource(ints = {10, 11, WheelSubActionState.MAX_PER_WHEEL})
  @DisplayName("neighbouring icons never overlap, even on the fullest ring")
  void iconsFitTheirSectors(final int sectors) {
    final double iconRadius =
        (DogCommandWheelScreen.INNER_RADIUS + DogCommandWheelScreen.OUTER_RADIUS) / 2.0;
    final double gapBetweenCentres = 2 * iconRadius * Math.sin(Math.PI / sectors);
    assertTrue(gapBetweenCentres >= DogCommandWheelScreen.ICON_SIZE, "gap " + gapBetweenCentres);
    assertTrue(
        DogCommandWheelScreen.OUTER_RADIUS - DogCommandWheelScreen.INNER_RADIUS
            >= DogCommandWheelScreen.ICON_SIZE);
  }

  @Test
  @DisplayName("the ring and its two caption lines fit the shortest GUI-scaled screen")
  void wheelFitsTheSmallestGuiScreen() {
    final int belowCentre =
        DogCommandWheelScreen.OUTER_RADIUS
            + DogCommandWheelScreen.CAPTION_GAP
            + 2 * FONT_HEIGHT
            + 2;
    assertTrue(belowCentre <= SMALLEST_GUI_HEIGHT / 2, "wheel reaches " + belowCentre);
  }

  private static final int SECTORS = 8;
  private static final double INNER = 40;
  private static final double OUTER = 100;

  static Stream<Arguments> deadzoneAndOutOfRangePoints() {
    return Stream.of(
        Arguments.of("wheel center", 0, 0),
        Arguments.of("inside deadzone", 20, 20),
        Arguments.of("just inside inner radius straight up", 0, -39),
        Arguments.of("just beyond outer radius straight up", 0, -101),
        Arguments.of("far outside the wheel", 500, 500));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("deadzoneAndOutOfRangePoints")
  @DisplayName("the hub deadzone and points beyond the outer radius select nothing")
  void deadzoneSelectsNothing(final String label, final double dx, final double dy) {
    assertEquals(-1, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  static Stream<Arguments> sectorCenters() {
    return Stream.of(
        Arguments.of("12 o'clock", 0, -70, 0),
        Arguments.of("1:30", 50, -50, 1),
        Arguments.of("3 o'clock", 70, 0, 2),
        Arguments.of("4:30", 50, 50, 3),
        Arguments.of("6 o'clock", 0, 70, 4),
        Arguments.of("7:30", -50, 50, 5),
        Arguments.of("9 o'clock", -70, 0, 6),
        Arguments.of("10:30", -50, -50, 7));
  }

  @ParameterizedTest(name = "{0} -> sector {2}")
  @MethodSource("sectorCenters")
  @DisplayName("each sector center maps to its index, clockwise from 12 o'clock")
  void sectorCentersMapClockwise(
      final String label, final double dx, final double dy, final int expected) {
    assertEquals(expected, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  static Stream<Arguments> sectorBoundaries() {
    final double radius = 70;
    final double justInside0 = Math.toRadians(-90 + 22.0);
    final double justInside1 = Math.toRadians(-90 + 23.0);
    return Stream.of(
        Arguments.of(
            "just before the 0/1 boundary",
            radius * Math.cos(justInside0),
            radius * Math.sin(justInside0),
            0),
        Arguments.of(
            "just after the 0/1 boundary",
            radius * Math.cos(justInside1),
            radius * Math.sin(justInside1),
            1));
  }

  @ParameterizedTest(name = "{0} -> sector {2}")
  @MethodSource("sectorBoundaries")
  @DisplayName("points on either side of a sector boundary resolve to the adjacent sectors")
  void sectorBoundariesResolveCleanly(
      final String label, final double dx, final double dy, final int expected) {
    assertEquals(expected, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  @ParameterizedTest(name = "ring position {0}")
  @MethodSource("ringPositions")
  @DisplayName("detection radii are inclusive across the whole ring band")
  void ringBandIsSelectable(final String label, final double dx, final double dy) {
    final int index = DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER);
    assertEquals(0, index);
  }

  static Stream<Arguments> ringPositions() {
    return Stream.of(
        Arguments.of("at inner detection radius", 0, -INNER),
        Arguments.of("mid ring", 0, -(INNER + OUTER) / 2),
        Arguments.of("at outer detection radius", 0, -OUTER));
  }

  static Stream<Arguments> hubIndexCases() {
    return Stream.of(
        Arguments.of("nothing hovered or rejected", -1, -1, -1),
        Arguments.of("hover wins over nothing", 3, -1, 3),
        Arguments.of("a rejected key press shows when nothing is hovered", -1, 7, 7),
        Arguments.of("hovering a sector replaces the rejected hint", 2, 7, 2));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("hubIndexCases")
  @DisplayName("the hub explains a rejected action until the player hovers another sector")
  void hubShowsHoveredThenRejectedAction(
      final String label, final int hovered, final int rejected, final int expected) {
    assertEquals(expected, DogCommandWheelScreen.hubIndex(hovered, rejected));
  }

  @ParameterizedTest(name = "sector {0} of 10")
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
  @DisplayName("with ten actions, the centre of every sector selects that sector")
  void tenSectorsEachSelectThemselves(final int sector) {
    final double angle = -Math.PI / 2 + sector * 2 * Math.PI / 10;
    final double radius = (INNER + OUTER) / 2.0;
    assertEquals(
        sector,
        DogCommandWheelScreen.sectorIndexAt(
            Math.cos(angle) * radius, Math.sin(angle) * radius, 10, INNER, OUTER));
  }

  @ParameterizedTest(name = "sector {0} of 11")
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
  @DisplayName("with the Actions sector added, the centre of every sector selects that sector")
  void elevenSectorsEachSelectThemselves(final int sector) {
    final double angle = -Math.PI / 2 + sector * 2 * Math.PI / 11;
    final double radius = (INNER + OUTER) / 2.0;
    assertEquals(
        sector,
        DogCommandWheelScreen.sectorIndexAt(
            Math.cos(angle) * radius, Math.sin(angle) * radius, 11, INNER, OUTER));
  }
}
