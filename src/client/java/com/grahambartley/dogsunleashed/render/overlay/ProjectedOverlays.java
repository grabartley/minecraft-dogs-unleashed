package com.grahambartley.dogsunleashed.render.overlay;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class ProjectedOverlays {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "projected_overlays");

  @FunctionalInterface
  public interface Images {
    Optional<PixelGrid> read(Identifier texture);
  }

  @FunctionalInterface
  public interface Registrar {
    Identifier register(UnleashedDogBreed rig, Identifier sheet, PixelGrid pixels);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectedOverlays.class);

  private static final ProjectedOverlays SHARED =
      new ProjectedOverlays(
          RigModels::loaded,
          RigProfiles::of,
          SlotRegions.shared(),
          ProjectedOverlays::readShipped,
          ProjectedOverlays::registerTexture);

  private final RigModels.Lookup models;
  private final Function<UnleashedDogBreed, RigProfile> profiles;
  private final SlotRegions regions;
  private final Images images;
  private final Registrar registrar;
  private final Map<UnleashedDogBreed, Map<Identifier, Optional<Identifier>>> byRig =
      new EnumMap<>(UnleashedDogBreed.class);

  public ProjectedOverlays(
      final RigModels.Lookup models,
      final Function<UnleashedDogBreed, RigProfile> profiles,
      final SlotRegions regions,
      final Images images,
      final Registrar registrar) {
    this.models = models;
    this.profiles = profiles;
    this.regions = regions;
    this.images = images;
    this.registrar = registrar;
  }

  public static ProjectedOverlays shared() {
    return SHARED;
  }

  public synchronized Optional<Identifier> textureOf(
      final UnleashedDogBreed rig, final Identifier sheet) {
    final Map<Identifier, Optional<Identifier>> sheets =
        this.byRig.computeIfAbsent(rig, key -> new HashMap<>());
    final Optional<Identifier> cached = sheets.get(sheet);
    if (cached != null) {
      return cached;
    }
    final Optional<BakedGeoModel> model = this.models.modelOf(rig);
    if (model.isEmpty()) {
      return Optional.empty();
    }
    final Optional<Identifier> projected = this.project(rig, sheet, model.get());
    sheets.put(sheet, projected);
    return projected;
  }

  public synchronized void clear(final TextureManager textureManager) {
    this.byRig.values().stream()
        .flatMap(sheets -> sheets.values().stream())
        .flatMap(Optional::stream)
        .forEach(textureManager::destroyTexture);
    this.byRig.clear();
  }

  private Optional<Identifier> project(
      final UnleashedDogBreed rig, final Identifier sheet, final BakedGeoModel model) {
    final Optional<PixelGrid> pixels = this.images.read(sheet);
    if (pixels.isEmpty()) {
      LOGGER.warn("Overlay sheet {} does not exist", sheet);
      return Optional.empty();
    }
    if (!OverlaySheet.fits(pixels.get())) {
      LOGGER.warn(
          "Overlay sheet {} is {}x{} but must be {}x{}",
          sheet,
          pixels.get().width(),
          pixels.get().height(),
          OverlaySheet.WIDTH,
          OverlaySheet.HEIGHT);
      return Optional.empty();
    }
    final int atlasWidth = (int) model.properties().textureWidth();
    final int atlasHeight = (int) model.properties().textureHeight();
    final RigProfile profile = this.profiles.apply(rig);
    final PixelGrid projected =
        OverlayProjector.project(
            pixels.get(),
            atlasWidth,
            atlasHeight,
            slot -> this.regions.regionsOf(rig, slot),
            slot ->
                profile
                    .sheetOverride(sheet, slot)
                    .flatMap(texture -> this.overrideOf(rig, texture, atlasWidth, atlasHeight)));
    return Optional.of(this.registrar.register(rig, sheet, projected));
  }

  private Optional<PixelGrid> overrideOf(
      final UnleashedDogBreed rig,
      final Identifier texture,
      final int atlasWidth,
      final int atlasHeight) {
    final Optional<PixelGrid> override = this.images.read(texture);
    if (override.isEmpty() || !override.get().hasSize(atlasWidth, atlasHeight)) {
      LOGGER.warn(
          "Rig {} override {} is missing or not {}x{}",
          rig.serializedId(),
          texture,
          atlasWidth,
          atlasHeight);
      return Optional.empty();
    }
    return override;
  }

  private static Optional<PixelGrid> readShipped(final Identifier texture) {
    final Optional<Resource> resource =
        MinecraftClient.getInstance().getResourceManager().getResource(texture);
    if (resource.isEmpty()) {
      return Optional.empty();
    }
    try (InputStream in = resource.get().getInputStream();
        NativeImage image = NativeImage.read(in)) {
      final PixelGrid pixels = PixelGrid.blank(image.getWidth(), image.getHeight());
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          pixels.set(x, y, image.getColor(x, y));
        }
      }
      return Optional.of(pixels);
    } catch (final IOException exception) {
      LOGGER.warn("Unreadable overlay texture {}: {}", texture, exception.getMessage());
      return Optional.empty();
    }
  }

  private static Identifier registerTexture(
      final UnleashedDogBreed rig, final Identifier sheet, final PixelGrid pixels) {
    final NativeImage image =
        new NativeImage(NativeImage.Format.RGBA, pixels.width(), pixels.height(), false);
    for (int y = 0; y < pixels.height(); y++) {
      for (int x = 0; x < pixels.width(); x++) {
        image.setColor(x, y, pixels.colourAt(x, y));
      }
    }
    final Identifier id =
        Identifier.of(
            MOD_ID,
            "sheet_overlay/"
                + rig.serializedId()
                + "/"
                + sheet.getNamespace()
                + "/"
                + sheet.getPath());
    MinecraftClient.getInstance()
        .getTextureManager()
        .registerTexture(id, new NativeImageBackedTexture(image));
    return id;
  }
}
