package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.overlay.OverlaySheet.Cell;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotRegion;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class ProjectedOverlaysTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);
  private static final Identifier SHEET = Identifier.of("dogs-unleashed", "textures/sheet.png");
  private static final Identifier OTHER = Identifier.of("dogs-unleashed", "textures/other.png");
  private static final Identifier HEAD_OVERRIDE =
      Identifier.of("dogs-unleashed", "textures/beagle_head.png");
  private static final int SHEET_COLOUR = 0xFF0000FF;
  private static final int OVERRIDE_COLOUR = 0xFF00FF00;

  private final Map<Identifier, PixelGrid> images = new HashMap<>();
  private final List<Identifier> reads = new ArrayList<>();
  private final List<PixelGrid> registered = new ArrayList<>();

  @Test
  @DisplayName("a sheet is projected once per rig and then reused")
  void projectedOncePerRig() {
    this.images.put(
        SHEET, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    final ProjectedOverlays overlays = overlays(shipped(), BUNDLED::profileOf);

    final Optional<Identifier> first = overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);
    final Optional<Identifier> second = overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);
    overlays.textureOf(UnleashedDogBreed.HUSKY, SHEET);

    assertTrue(first.isPresent());
    assertEquals(first, second);
    assertEquals(2, this.registered.size());
    assertEquals(List.of(SHEET, SHEET), this.reads);
  }

  @Test
  @DisplayName("each rig's projection is the size of that rig's own texture atlas")
  void projectionMatchesTheAtlas() {
    this.images.put(
        SHEET, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    final ProjectedOverlays overlays = overlays(shipped(), BUNDLED::profileOf);

    overlays.textureOf(UnleashedDogBreed.HUSKY, SHEET);
    overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);

    assertTrue(this.registered.get(0).hasSize(64, 64));
    assertTrue(this.registered.get(1).hasSize(128, 128));
  }

  @Test
  @DisplayName("a missing sheet draws nothing and is not read again every frame")
  void missingSheetIsRememberedAsEmpty() {
    final ProjectedOverlays overlays = overlays(shipped(), BUNDLED::profileOf);

    assertTrue(overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET).isEmpty());
    assertTrue(overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET).isEmpty());

    assertEquals(List.of(SHEET), this.reads);
    assertTrue(this.registered.isEmpty());
  }

  @Test
  @DisplayName("a sheet of the wrong size draws nothing")
  void wrongSizedSheetDrawsNothing() {
    this.images.put(SHEET, PixelGrids.filled(64, 64, SHEET_COLOUR));
    final ProjectedOverlays overlays = overlays(shipped(), BUNDLED::profileOf);

    assertTrue(overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET).isEmpty());
    assertTrue(this.registered.isEmpty());
  }

  @Test
  @DisplayName("a rig whose model has not loaded yet is projected later rather than cached empty")
  void unloadedModelIsNotCached() {
    this.images.put(
        SHEET, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    final AtomicReference<Optional<BakedGeoModel>> model = new AtomicReference<>(Optional.empty());
    final ProjectedOverlays overlays = overlays(rig -> model.get(), BUNDLED::profileOf);

    assertTrue(overlays.textureOf(UnleashedDogBreed.HUSKY, SHEET).isEmpty());
    model.set(Optional.of(ShippedRigs.model("husky")));

    assertTrue(overlays.textureOf(UnleashedDogBreed.HUSKY, SHEET).isPresent());
  }

  @Test
  @DisplayName("a profile override for a slot replaces only that slot, and only for its own sheet")
  void profileOverrideReplacesItsSlot() {
    this.images.put(
        SHEET, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    this.images.put(
        OTHER, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    this.images.put(HEAD_OVERRIDE, PixelGrids.filled(128, 128, OVERRIDE_COLOUR));
    final RigProfile beagle = BUNDLED.profileOf(UnleashedDogBreed.BEAGLE);
    final RigProfile withOverride =
        new RigProfile(
            beagle.bones(),
            beagle.overlays(),
            beagle.pupils(),
            Map.of(SHEET, Map.of(Slot.HEAD, HEAD_OVERRIDE)));
    final ProjectedOverlays overlays = overlays(shipped(), rig -> withOverride);
    final SlotRegions regions = new SlotRegions(shipped(), rig -> withOverride);
    final SlotRegion head = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD).get(0);
    final SlotRegion tail = regions.regionsOf(UnleashedDogBreed.BEAGLE, Slot.TAIL).get(0);

    overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);
    overlays.textureOf(UnleashedDogBreed.BEAGLE, OTHER);

    assertEquals(OVERRIDE_COLOUR, this.registered.get(0).colourAt(head.x(), head.y()));
    assertEquals(SHEET_COLOUR, this.registered.get(0).colourAt(tail.x(), tail.y()));
    assertEquals(SHEET_COLOUR, this.registered.get(1).colourAt(head.x(), head.y()));
  }

  @Test
  @DisplayName("an override that is missing falls back to the projected sheet")
  void missingOverrideFallsBack() {
    this.images.put(SHEET, labelledHeadSheet());
    final RigProfile beagle = BUNDLED.profileOf(UnleashedDogBreed.BEAGLE);
    final RigProfile withOverride =
        new RigProfile(
            beagle.bones(),
            beagle.overlays(),
            beagle.pupils(),
            Map.of(SHEET, Map.of(Slot.HEAD, HEAD_OVERRIDE)));
    final ProjectedOverlays overlays = overlays(shipped(), rig -> withOverride);
    final SlotRegion head =
        new SlotRegions(shipped(), rig -> withOverride)
            .regionsOf(UnleashedDogBreed.BEAGLE, Slot.HEAD)
            .get(0);

    overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);

    assertEquals(SHEET_COLOUR, this.registered.get(0).colourAt(head.x(), head.y()));
  }

  @Test
  @DisplayName("clearing destroys every projected texture and projects again on the next ask")
  void clearDestroysAndReprojects() {
    this.images.put(
        SHEET, PixelGrids.filled(OverlaySheet.WIDTH, OverlaySheet.HEIGHT, SHEET_COLOUR));
    final ProjectedOverlays overlays = overlays(shipped(), BUNDLED::profileOf);
    final TextureManager textures = mock(TextureManager.class);
    final Identifier projected = overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET).orElseThrow();

    overlays.clear(textures);
    overlays.textureOf(UnleashedDogBreed.BEAGLE, SHEET);

    verify(textures).destroyTexture(projected);
    assertEquals(2, this.registered.size());
  }

  private ProjectedOverlays overlays(
      final RigModels.Lookup models, final Function<UnleashedDogBreed, RigProfile> profiles) {
    return new ProjectedOverlays(
        models,
        profiles,
        new SlotRegions(models, profiles),
        texture -> {
          this.reads.add(texture);
          return Optional.ofNullable(this.images.get(texture));
        },
        (rig, sheet, pixels) -> {
          this.registered.add(pixels);
          return Identifier.of(
              "dogs-unleashed", "sheet_overlay/" + rig.serializedId() + "/" + sheet.getPath());
        });
  }

  private static RigModels.Lookup shipped() {
    return rig -> Optional.of(ShippedRigs.model(rig.serializedId()));
  }

  private static PixelGrid labelledHeadSheet() {
    final PixelGrid sheet = PixelGrid.blank(OverlaySheet.WIDTH, OverlaySheet.HEIGHT);
    for (final Direction face : Direction.values()) {
      final Cell cell = OverlaySheet.cellOf(Slot.HEAD, face).orElseThrow();
      for (int y = cell.y(); y < cell.y() + cell.height(); y++) {
        for (int x = cell.x(); x < cell.x() + cell.width(); x++) {
          sheet.set(x, y, SHEET_COLOUR);
        }
      }
    }
    return sheet;
  }
}
