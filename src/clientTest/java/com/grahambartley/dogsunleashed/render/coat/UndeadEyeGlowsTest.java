package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = "CROSS_BREED",
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("each rig registers its own glow texture with its own eyes")
  void eachRigRegistersItsOwnGlow(final UnleashedDogBreed rig) {
    final UndeadEyeGlows glows = glows(r -> Optional.of(ShippedRigs.model(r.serializedId())));

    assertTrue(glows.glowTextureOf(rig).isPresent());
    assertEquals(List.of(rig), this.registered);
  }

  @Test
  @DisplayName("no glow is registered while the rig's model has not loaded")
  void noGlowWhileTheModelIsUnloaded() {
    final AtomicReference<Optional<BakedGeoModel>> model = new AtomicReference<>(Optional.empty());
    final UndeadEyeGlows glows = glows(rig -> model.get());

    assertTrue(glows.glowTextureOf(UnleashedDogBreed.HUSKY).isEmpty());
    model.set(Optional.of(ShippedRigs.model("husky")));

    assertTrue(glows.glowTextureOf(UnleashedDogBreed.HUSKY).isPresent());
    assertEquals(List.of(UnleashedDogBreed.HUSKY), this.registered);
  }

  @Test
  @DisplayName("a rig's glow texture is registered once and then reused")
  void glowTextureIsRegisteredOnce() {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));

    final Optional<Identifier> first = glows.glowTextureOf(UnleashedDogBreed.DACHSHUND);
    final Optional<Identifier> second = glows.glowTextureOf(UnleashedDogBreed.DACHSHUND);

    assertEquals(first, second);
    assertEquals(List.of(UnleashedDogBreed.DACHSHUND), this.registered);
  }

  @Test
  @DisplayName("clearing destroys every glow texture and works the eyes out again")
  void clearDestroysGlowsAndForgetsEyes() {
    final UndeadEyeGlows glows = glows(rig -> Optional.of(ShippedRigs.model(rig.serializedId())));
    final TextureManager textures = mock(TextureManager.class);
    final Identifier glow = glows.glowTextureOf(UnleashedDogBreed.BEAGLE).orElseThrow();

    glows.clear(textures);
    glows.glowTextureOf(UnleashedDogBreed.BEAGLE);

    verify(textures).destroyTexture(glow);
    assertEquals(List.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.BEAGLE), this.registered);
    assertEquals(List.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.BEAGLE), this.modelLoads);
  }

  private UndeadEyeGlows glows(final RigModels.Lookup models) {
    final RigModels.Lookup counted =
        rig -> {
          this.modelLoads.add(rig);
          return models.modelOf(rig);
        };
    return new UndeadEyeGlows(
        counted,
        rig -> BUNDLED.profileOf(rig).pupils(),
        new SlotRegions(models, BUNDLED::profileOf),
        (rig, eyes) -> {
          this.registered.add(rig);
          return Identifier.of("dogs-unleashed", "undead_glow/" + rig.serializedId());
        });
  }
}
