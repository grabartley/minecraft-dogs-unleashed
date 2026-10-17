package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class UndeadEyeGlowsTest {

  private static final RigProfiles BUNDLED = new RigProfiles(RigProfiles::openBundled);

  private final List<UnleashedDogBreed> registered = new ArrayList<>();
  private final List<UnleashedDogBreed> modelLoads = new ArrayList<>();

  @Test
  @DisplayName("a rig's eyes are worked out once and then reused")
  void eyesAreCachedPerRig() {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));

    final UndeadDogEyes first = glows.eyesOf(UnleashedDogBreed.BEAGLE);
    final UndeadDogEyes second = glows.eyesOf(UnleashedDogBreed.BEAGLE);

    assertSame(first, second);
    assertEquals(BUNDLED.profileOf(UnleashedDogBreed.BEAGLE).pupils(), first.pupils());
    assertEquals(List.of(UnleashedDogBreed.BEAGLE), this.modelLoads);
  }

  @Test
  @DisplayName("a rig whose model has not loaded yet has no eyes and is asked again later")
  void unloadedModelIsNotCached() {
    final AtomicReference<Optional<BakedGeoModel>> model = new AtomicReference<>(Optional.empty());
    final UndeadEyeGlows glows = glows(rig -> model.get());

    assertSame(UndeadDogEyes.NONE, glows.eyesOf(UnleashedDogBreed.HUSKY));
    model.set(Optional.of(ShippedRigs.model("husky")));

    assertEquals(
        BUNDLED.profileOf(UnleashedDogBreed.HUSKY).pupils(),
        glows.eyesOf(UnleashedDogBreed.HUSKY).pupils());
  }

  @ParameterizedTest(name = "{0} glows like {1}")
  @CsvSource({"CROSS_BREED, HUSKY", "BEAGLE, BEAGLE", "SHIBA_INU, SHIBA_INU"})
  @DisplayName("a cross-breed with no rig of its own glows on the fallback rig")
  void crossBreedUsesTheFallbackRig(
      final UnleashedDogBreed source, final UnleashedDogBreed expected) {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));

    glows.glowTextureOf(source);

    assertEquals(List.of(expected), this.registered);
  }

  @Test
  @DisplayName("a rig's glow texture is registered once and then reused")
  void glowTextureIsRegisteredOnce() {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));

    final Identifier first = glows.glowTextureOf(UnleashedDogBreed.DACHSHUND);
    final Identifier second = glows.glowTextureOf(UnleashedDogBreed.DACHSHUND);

    assertEquals(first, second);
    assertEquals(List.of(UnleashedDogBreed.DACHSHUND), this.registered);
  }

  @Test
  @DisplayName("clearing destroys every glow texture and works the eyes out again")
  void clearDestroysGlowsAndForgetsEyes() {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));
    final TextureManager textures = mock(TextureManager.class);
    final Identifier glow = glows.glowTextureOf(UnleashedDogBreed.BEAGLE);

    glows.clear(textures);
    glows.glowTextureOf(UnleashedDogBreed.BEAGLE);

    verify(textures).destroyTexture(glow);
    assertEquals(List.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.BEAGLE), this.registered);
    assertEquals(List.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.BEAGLE), this.modelLoads);
  }

  private UndeadEyeGlows glows(final Function<UnleashedDogBreed, Optional<BakedGeoModel>> models) {
    final Function<UnleashedDogBreed, Optional<BakedGeoModel>> counted =
        rig -> {
          this.modelLoads.add(rig);
          return models.apply(rig);
        };
    return new UndeadEyeGlows(
        counted,
        rig -> BUNDLED.profileOf(rig).pupils(),
        new SlotRegions(models::apply, BUNDLED::profileOf),
        (rig, eyes) -> {
          this.registered.add(rig);
          return Identifier.of("dogs-unleashed", "undead_glow/" + rig.serializedId());
        });
  }
}
