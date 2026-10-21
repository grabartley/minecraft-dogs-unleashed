package com.grahambartley.dogsunleashed.render.cross;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.render.coat.CoatImages;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextureCache.CrossCoat;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import com.grahambartley.dogsunleashed.render.rig.Texel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CrossCoatTextures {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "cross_coat_textures");

  private static final float PERCENT = 100.0f;
  private static final CrossCoatTextureCache CACHE =
      new CrossCoatTextureCache(CrossCoatTextures::paint);

  private CrossCoatTextures() {}

  public static CrossCoatTextureCache cache() {
    return CACHE;
  }

  public static Identifier tintedOf(final CrossCoat coat) {
    return CACHE.tintedOf(coat);
  }

  private static @Nullable Identifier paint(final CrossCoat coat) {
    final Optional<CoatPalette> own = paletteOf(coat.ownPalette());
    if (own.isEmpty()) {
      return null;
    }
    final List<PaletteTint.Pull> pulls = new ArrayList<>();
    for (final PaletteWeight weight : coat.pulls()) {
      paletteOf(weight.palette())
          .ifPresent(
              palette -> pulls.add(new PaletteTint.Pull(palette, weight.percent() / PERCENT)));
    }
    if (pulls.isEmpty()) {
      return null;
    }
    try (CoatImages.Source source = CoatImages.read(coat.living())) {
      if (source == null) {
        return null;
      }
      final int width = source.image().getWidth();
      final int height = source.image().getHeight();
      final boolean[] mask = coatMask(coat, width, height);
      final int[] tinted =
          PaletteTint.tintedAbgr(
              CoatImages.pixelsOf(source.image()),
              width,
              texel -> mask[texel.y() * width + texel.x()],
              own.get(),
              pulls);
      return CoatImages.register(idOf(coat), CoatImages.imageOf(tinted, width, height));
    }
  }

  private static boolean[] coatMask(final CrossCoat coat, final int width, final int height) {
    final boolean[] mask = new boolean[width * height];
    for (final Slot slot : Slot.values()) {
      if (slot == Slot.EYES) {
        continue;
      }
      for (final SlotRegion region : SlotRegions.shared().regionsOf(coat.rig(), slot)) {
        for (int y = Math.max(0, region.y());
            y < Math.min(height, region.y() + region.height());
            y++) {
          for (int x = Math.max(0, region.x());
              x < Math.min(width, region.x() + region.width());
              x++) {
            mask[y * width + x] = true;
          }
        }
      }
    }
    for (final Texel pupil : RigProfiles.of(coat.rig()).pupils()) {
      if (pupil.x() >= 0 && pupil.y() >= 0 && pupil.x() < width && pupil.y() < height) {
        mask[pupil.y() * width + pupil.x()] = false;
      }
    }
    return mask;
  }

  private static Optional<CoatPalette> paletteOf(final Identifier palette) {
    try (CoatImages.Source source = CoatImages.read(palette)) {
      if (source == null || source.image().getWidth() < CoatPalette.SIZE) {
        return Optional.empty();
      }
      final int[] rgb = new int[CoatPalette.SIZE];
      for (int x = 0; x < CoatPalette.SIZE; x++) {
        final int abgr = source.image().getColor(x, 0);
        rgb[x] =
            (CoatImages.red(abgr) << 16) | (CoatImages.green(abgr) << 8) | CoatImages.blue(abgr);
      }
      return CoatPalette.ofRow(rgb);
    }
  }

  static Identifier idOf(final CrossCoat coat) {
    final String living = coat.living().getPath().replace(".png", "");
    final String pulls =
        coat.pulls().stream()
            .map(
                weight ->
                    weight
                            .palette()
                            .getPath()
                            .replace("textures/entity/palette/", "")
                            .replace(".png", "")
                        + "_"
                        + weight.percent())
            .collect(Collectors.joining("/"));
    return Identifier.of(MOD_ID, "cross/" + living + "/" + pulls);
  }
}
