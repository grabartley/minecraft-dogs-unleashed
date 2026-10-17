package com.grahambartley.dogsunleashed.render.overlay;

import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class OverlayProjector {

  private OverlayProjector() {}

  public static PixelGrid project(
      final PixelGrid sheet,
      final int atlasWidth,
      final int atlasHeight,
      final Function<Slot, List<SlotRegion>> regions,
      final Function<Slot, Optional<PixelGrid>> overrides) {
    final PixelGrid projected = PixelGrid.blank(atlasWidth, atlasHeight);
    for (final Slot slot : OverlaySheet.SLOTS) {
      final Optional<PixelGrid> override =
          overrides.apply(slot).filter(image -> image.hasSize(atlasWidth, atlasHeight));
      for (final SlotRegion region : regions.apply(slot)) {
        if (override.isPresent()) {
          copy(override.get(), region, projected);
        } else {
          resample(sheet, slot, region, projected);
        }
      }
    }
    return projected;
  }

  private static void copy(final PixelGrid from, final SlotRegion region, final PixelGrid into) {
    forEachTexel(region, into, (x, y, column, row) -> into.set(x, y, from.colourAt(x, y)));
  }

  private static void resample(
      final PixelGrid sheet, final Slot slot, final SlotRegion region, final PixelGrid into) {
    OverlaySheet.cellOf(slot, region.face())
        .ifPresent(
            cell ->
                forEachTexel(
                    region,
                    into,
                    (x, y, column, row) ->
                        into.set(
                            x,
                            y,
                            sheet.colourAt(
                                cell.x() + column * cell.width() / region.width(),
                                cell.y() + row * cell.height() / region.height()))));
  }

  @FunctionalInterface
  private interface TexelVisitor {
    void visit(int x, int y, int column, int row);
  }

  private static void forEachTexel(
      final SlotRegion region, final PixelGrid atlas, final TexelVisitor visitor) {
    for (int row = 0; row < region.height(); row++) {
      for (int column = 0; column < region.width(); column++) {
        final int x = region.x() + column;
        final int y = region.y() + row;
        if (x < atlas.width() && y < atlas.height()) {
          visitor.visit(x, y, column, row);
        }
      }
    }
  }
}
