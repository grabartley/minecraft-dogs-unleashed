package com.grahambartley.dogsunleashed.render.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.portrait.DogPortrait;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PortraitTextureCacheTest {

  private static final DogStatueLook BEAGLE =
      new DogStatueLook(UnleashedDogBreed.BEAGLE, false, 1, -1, 0, 0, null, null);

  private final List<DogPortrait> bakes = new ArrayList<>();

  private PortraitTextureCache cache(final boolean succeeds) {
    return new PortraitTextureCache(
        portrait -> {
          this.bakes.add(portrait);
          return succeeds
              ? Identifier.of("dogs-unleashed", "portrait/baked_" + (this.bakes.size() - 1))
              : null;
        });
  }

  private static DogPortrait portrait(final String name, final PortraitSize size) {
    return new DogPortrait(BEAGLE, name, size);
  }

  @ParameterizedTest(name = "{0} frames")
  @ValueSource(ints = {1, 2, 60, 600})
  @DisplayName("a portrait is baked once, not every frame it is drawn")
  void bakesOnceAcrossFrames(final int frames) {
    final PortraitTextureCache cache = cache(true);
    for (int frame = 0; frame < frames; frame++) {
      cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
      cache.bakePending();
    }
    assertEquals(1, this.bakes.size());
    assertEquals(
        Identifier.of("dogs-unleashed", "portrait/baked_0"),
        cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
  }

  @Test
  @DisplayName("nothing is drawn until the queued bake runs")
  void unbakedPortraitHasNoTextureYet() {
    final PortraitTextureCache cache = cache(true);
    assertNull(cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
    assertNull(cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
    assertEquals(0, this.bakes.size());
  }

  @Test
  @DisplayName("portraits that look the same share a texture, whatever the dog's name")
  void sameLookSharesOneTexture() {
    final PortraitTextureCache cache = cache(true);
    cache.textureOf(portrait("Bentley", PortraitSize.WIDE));
    cache.textureOf(portrait("Rex", PortraitSize.WIDE));
    cache.bakePending();
    assertEquals(1, this.bakes.size());
    assertEquals(
        cache.textureOf(portrait("Bentley", PortraitSize.WIDE)),
        cache.textureOf(portrait("Rex", PortraitSize.WIDE)));
  }

  @Test
  @DisplayName("each size of the same dog gets its own texture")
  void eachSizeIsBakedSeparately() {
    final PortraitTextureCache cache = cache(true);
    for (final PortraitSize size : PortraitSize.values()) {
      cache.textureOf(portrait("Bentley", size));
    }
    cache.bakePending();
    assertEquals(PortraitSize.values().length, this.bakes.size());
  }

  @Test
  @DisplayName("a portrait that cannot be baked is not retried every frame")
  void failedBakeIsNotRetried() {
    final PortraitTextureCache cache = cache(false);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakePending();
    assertNull(cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
    cache.bakePending();
    assertEquals(1, this.bakes.size());
  }

  @Test
  @DisplayName("a resource reload destroys baked textures and bakes each portrait once more")
  void clearRebakesOnce() {
    final PortraitTextureCache cache = cache(true);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakePending();
    final TextureManager textures = mock(TextureManager.class);
    cache.clear(textures);
    verify(textures).destroyTexture(Identifier.of("dogs-unleashed", "portrait/baked_0"));
    for (int frame = 0; frame < 10; frame++) {
      cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
      cache.bakePending();
    }
    assertEquals(2, this.bakes.size());
  }

  @Test
  @DisplayName("clearing drops queued bakes and never destroys a texture that failed to bake")
  void clearDropsQueuedAndFailedBakes() {
    final PortraitTextureCache cache = cache(false);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakePending();
    cache.textureOf(portrait("Bentley", PortraitSize.LARGE));
    final TextureManager textures = mock(TextureManager.class);
    cache.clear(textures);
    verifyNoInteractions(textures);
    cache.bakePending();
    assertEquals(1, this.bakes.size());
  }
}
