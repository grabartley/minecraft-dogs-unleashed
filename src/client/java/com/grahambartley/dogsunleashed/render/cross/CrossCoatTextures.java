package com.grahambartley.dogsunleashed.render.cross;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.coat.CoatImages;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextureCache.CrossCoat;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class CrossCoatTextures {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "cross_coat_textures");

  private static final Logger LOGGER = LoggerFactory.getLogger(CrossCoatTextures.class);
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
    final Optional<BakedGeoModel> model = RigModels.loaded(coat.rig());
    if (model.isEmpty()) {
      return null;
    }
    try (CoatImages.Source source = CoatImages.read(coat.living())) {
      if (source == null) {
        return null;
      }
      final int width = source.image().getWidth();
      final int height = source.image().getHeight();
      if (width != (int) model.get().properties().textureWidth()
          || height != (int) model.get().properties().textureHeight()) {
        LOGGER.warn("Leaving {} untinted, its size does not match its rig", coat.living());
        return null;
      }
      final CrossCoatMask mask =
          CrossCoatMask.of(
              width,
              height,
              coatRegions(coat.rig(), model.get()),
              RigProfiles.of(coat.rig()).pupils());
      final int[] tinted =
          PaletteTint.tintedAbgr(
              CoatImages.pixelsOf(source.image()), width, mask::tints, own.get(), pulls);
      return CoatImages.register(idOf(coat), CoatImages.imageOf(tinted, width, height));
    }
  }

  private static List<SlotRegion> coatRegions(
      final UnleashedDogBreed rig, final BakedGeoModel model) {
    final List<SlotRegion> regions = new ArrayList<>();
    for (final Slot slot : Slot.values()) {
      if (slot != Slot.EYES) {
        regions.addAll(SlotRegions.shared().regionsOf(rig, slot));
      }
    }
    regions.addAll(SlotRegions.ofBones(model, RigProfiles.of(rig).ears().variantBones()));
    return regions;
  }

  private static Optional<CoatPalette> paletteOf(final Identifier palette) {
    try (CoatImages.Source source = CoatImages.read(palette)) {
      if (source == null || source.image().getWidth() < CoatPalette.SIZE) {
        return Optional.empty();
      }
      final int[] abgr = new int[CoatPalette.SIZE];
      for (int x = 0; x < CoatPalette.SIZE; x++) {
        abgr[x] = source.image().getColor(x, 0);
      }
      return CoatPalette.ofAbgrRow(abgr);
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
