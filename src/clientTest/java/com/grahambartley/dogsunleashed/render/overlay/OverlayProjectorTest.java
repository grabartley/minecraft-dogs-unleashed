package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.overlay.OverlaySheet.Cell;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class OverlayProjectorTest {

  private static final int ATLAS = 32;
  private static final int OVERRIDE_COLOUR = 0xFF00FF00;

  @ParameterizedTest(name = "{0}")
  @EnumSource(Direction.class)
  @DisplayName("a face region takes its colour from the cell for that face direction")
  void faceTakesItsDirectionCell(final Direction face) {
    final SlotRegion region = new SlotRegion("head", face, 4, 6, 3, 2);

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(), ATLAS, ATLAS, regionsOn(Slot.HEAD, region), slot -> Optional.empty());

    for (int y = 6; y < 8; y++) {
      for (int x = 4; x < 7; x++) {
        assertEquals(labelOf(Slot.HEAD, face), projected.colourAt(x, y));
      }
    }
  }

  @Test
  @DisplayName("a region is resampled so its corners land on the cell's corners")
  void regionIsStretchedOverTheWholeCell() {
    final PixelGrid sheet = PixelGrid.blank(OverlaySheet.WIDTH, OverlaySheet.HEIGHT);
    final Cell cell = OverlaySheet.cellOf(Slot.TORSO, Direction.NORTH).orElseThrow();
    sheet.set(cell.x(), cell.y(), 1);
    sheet.set(cell.x() + cell.width() - 1, cell.y(), 2);
    sheet.set(cell.x(), cell.y() + cell.height() - 1, 3);
    sheet.set(cell.x() + cell.width() - 1, cell.y() + cell.height() - 1, 4);
    final SlotRegion region = new SlotRegion("body", Direction.NORTH, 0, 0, 32, 48);

    final PixelGrid projected =
        OverlayProjector.project(
            sheet, 32, 48, regionsOn(Slot.TORSO, region), slot -> Optional.empty());

    assertEquals(1, projected.colourAt(0, 0));
    assertEquals(2, projected.colourAt(31, 0));
    assertEquals(3, projected.colourAt(0, 47));
    assertEquals(4, projected.colourAt(31, 47));
  }

  @Test
  @DisplayName("texels outside every slot region stay transparent")
  void outsideRegionsIsTransparent() {
    final SlotRegion region = new SlotRegion("head", Direction.UP, 0, 0, 2, 2);

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(), ATLAS, ATLAS, regionsOn(Slot.HEAD, region), slot -> Optional.empty());

    assertEquals(0, projected.colourAt(2, 2));
    assertEquals(0, projected.colourAt(ATLAS - 1, ATLAS - 1));
  }

  @Test
  @DisplayName("an override replaces the projection for its own slot and leaves the others alone")
  void overrideWinsForItsSlotOnly() {
    final SlotRegion head = new SlotRegion("head", Direction.NORTH, 0, 0, 2, 2);
    final SlotRegion tail = new SlotRegion("tail", Direction.NORTH, 10, 10, 2, 2);
    final Map<Slot, List<SlotRegion>> regions =
        Map.of(Slot.HEAD, List.of(head), Slot.TAIL, List.of(tail));

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(),
            ATLAS,
            ATLAS,
            slot -> regions.getOrDefault(slot, List.of()),
            slot -> slot == Slot.HEAD ? Optional.of(filled(ATLAS, ATLAS)) : Optional.empty());

    assertEquals(OVERRIDE_COLOUR, projected.colourAt(1, 1));
    assertEquals(labelOf(Slot.TAIL, Direction.NORTH), projected.colourAt(11, 11));
    assertEquals(0, projected.colourAt(5, 5));
  }

  @Test
  @DisplayName("an override that does not match the atlas size is ignored")
  void wrongSizedOverrideIsIgnored() {
    final SlotRegion head = new SlotRegion("head", Direction.EAST, 0, 0, 2, 2);

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(),
            ATLAS,
            ATLAS,
            regionsOn(Slot.HEAD, head),
            slot -> Optional.of(filled(ATLAS * 2, ATLAS * 2)));

    assertEquals(labelOf(Slot.HEAD, Direction.EAST), projected.colourAt(0, 0));
  }

  @Test
  @DisplayName("a region running off the atlas edge is clipped rather than failing")
  void regionOffTheEdgeIsClipped() {
    final SlotRegion region = new SlotRegion("head", Direction.UP, ATLAS - 1, ATLAS - 1, 4, 4);

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(), ATLAS, ATLAS, regionsOn(Slot.HEAD, region), slot -> Optional.empty());

    assertEquals(labelOf(Slot.HEAD, Direction.UP), projected.colourAt(ATLAS - 1, ATLAS - 1));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = "CROSS_BREED",
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName(
      "on every shipped rig every slot texel is painted, and a face no other face shares shows its own cell")
  void everyShippedRigReceivesEverySlot(final UnleashedDogBreed rig) {
    final BakedGeoModel model = ShippedRigs.model(rig.serializedId());
    final SlotRegions regions =
        new SlotRegions(
            r -> Optional.of(model), new RigProfiles(RigProfiles::openBundled)::profileOf);
    final int width = (int) model.properties().textureWidth();
    final int height = (int) model.properties().textureHeight();

    final PixelGrid projected =
        OverlayProjector.project(
            labelledSheet(),
            width,
            height,
            slot -> regions.regionsOf(rig, slot),
            slot -> Optional.empty());

    final List<Map.Entry<Slot, SlotRegion>> all =
        OverlaySheet.SLOTS.stream()
            .flatMap(slot -> regions.regionsOf(rig, slot).stream().map(r -> Map.entry(slot, r)))
            .toList();
    for (final Map.Entry<Slot, SlotRegion> entry : all) {
      final SlotRegion region = entry.getValue();
      for (int y = region.y(); y < region.y() + region.height(); y++) {
        for (int x = region.x(); x < region.x() + region.width(); x++) {
          assertNotEquals(0, projected.colourAt(x, y), rig + " " + region + " left unpainted");
        }
      }
      final boolean shared =
          all.stream()
              .anyMatch(other -> other.getValue() != region && overlap(other.getValue(), region));
      if (!shared) {
        assertEquals(
            labelOf(entry.getKey(), region.face()),
            projected.colourAt(region.x(), region.y()),
            rig + " " + entry.getKey() + " " + region);
      }
    }
  }

  private static boolean overlap(final SlotRegion a, final SlotRegion b) {
    return a.x() < b.x() + b.width()
        && b.x() < a.x() + a.width()
        && a.y() < b.y() + b.height()
        && b.y() < a.y() + a.height();
  }

  private static PixelGrid labelledSheet() {
    final PixelGrid sheet = PixelGrid.blank(OverlaySheet.WIDTH, OverlaySheet.HEIGHT);
    for (final Slot slot : OverlaySheet.SLOTS) {
      for (final Direction face : Direction.values()) {
        final Cell cell = OverlaySheet.cellOf(slot, face).orElseThrow();
        for (int y = cell.y(); y < cell.y() + cell.height(); y++) {
          for (int x = cell.x(); x < cell.x() + cell.width(); x++) {
            sheet.set(x, y, labelOf(slot, face));
          }
        }
      }
    }
    return sheet;
  }

  private static int labelOf(final Slot slot, final Direction face) {
    return 0xFF000000 | (slot.ordinal() << 8) | face.ordinal();
  }

  private static PixelGrid filled(final int width, final int height) {
    final PixelGrid grid = PixelGrid.blank(width, height);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        grid.set(x, y, OVERRIDE_COLOUR);
      }
    }
    return grid;
  }

  private static Function<Slot, List<SlotRegion>> regionsOn(
      final Slot target, final SlotRegion region) {
    return slot -> slot == target ? List.of(region) : List.of();
  }
}
