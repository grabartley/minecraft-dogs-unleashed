package com.grahambartley.dogsunleashed.render.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.block.DogStatuePose;
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
import org.junit.jupiter.params.provider.CsvSource;
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
    return new DogPortrait(BEAGLE, name, size, DogStatuePose.SIT);
  }

  private static void bakeTicks(final PortraitTextureCache cache, final int ticks) {
    for (int tick = 0; tick < ticks; tick++) {
      cache.bakeNextPending();
    }
  }

  @ParameterizedTest(name = "{0} portraits queued, {1} ticks")
  @CsvSource({"1, 1", "3, 1", "3, 2", "3, 3", "3, 5"})
  @DisplayName(
      "one portrait is baked per tick, oldest first, so a wall of portraits never stalls a frame")
  void bakesOnePortraitPerTick(final int queued, final int ticks) {
    final PortraitTextureCache cache = cache(true);
    final List<PortraitSize> sizes = List.of(PortraitSize.values()).subList(0, queued);
    sizes.forEach(size -> cache.textureOf(portrait("Bentley", size)));
    bakeTicks(cache, ticks);
    final int baked = Math.min(queued, ticks);
    assertEquals(sizes.subList(0, baked), this.bakes.stream().map(DogPortrait::size).toList());
  }

  @ParameterizedTest(name = "{0} frames")
  @ValueSource(ints = {1, 2, 60, 600})
  @DisplayName("a portrait is baked once, not every frame it is drawn")
  void bakesOnceAcrossFrames(final int frames) {
    final PortraitTextureCache cache = cache(true);
    for (int frame = 0; frame < frames; frame++) {
      cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
      cache.bakeNextPending();
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
    cache.bakeNextPending();
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
    bakeTicks(cache, PortraitSize.values().length);
    assertEquals(PortraitSize.values().length, this.bakes.size());
  }

  @ParameterizedTest(name = "a beagle painted {0} then {1} bakes {2} textures")
  @CsvSource({"SIT, STAND, 2", "STAND, HOWL, 2", "SIT, HOWL, 1", "STAND, STAND, 1"})
  @DisplayName(
      "each shown pose gets its own texture, and a howl on a breed that cannot howl shares the sitting one")
  void eachShownPoseIsBakedOnce(
      final DogStatuePose first, final DogStatuePose second, final int textures) {
    final PortraitTextureCache cache = cache(true);
    cache.textureOf(new DogPortrait(BEAGLE, "Bentley", PortraitSize.SMALL, first));
    cache.textureOf(new DogPortrait(BEAGLE, "Bentley", PortraitSize.SMALL, second));
    bakeTicks(cache, 2);
    assertEquals(textures, this.bakes.size());
  }

  @Test
  @DisplayName("a portrait that cannot be baked is not retried every frame")
  void failedBakeIsNotRetried() {
    final PortraitTextureCache cache = cache(false);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakeNextPending();
    assertNull(cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
    cache.bakeNextPending();
    assertEquals(1, this.bakes.size());
  }

  @Test
  @DisplayName("a resource reload destroys baked textures and bakes each portrait once more")
  void clearRebakesOnce() {
    final PortraitTextureCache cache = cache(true);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakeNextPending();
    final TextureManager textures = mock(TextureManager.class);
    cache.clear(textures);
    verify(textures).destroyTexture(Identifier.of("dogs-unleashed", "portrait/baked_0"));
    for (int frame = 0; frame < 10; frame++) {
      cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
      cache.bakeNextPending();
    }
    assertEquals(2, this.bakes.size());
  }

  @Test
  @DisplayName("clearing drops queued bakes and never destroys a texture that failed to bake")
  void clearDropsQueuedAndFailedBakes() {
    final PortraitTextureCache cache = cache(false);
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakeNextPending();
    cache.textureOf(portrait("Bentley", PortraitSize.LARGE));
    final TextureManager textures = mock(TextureManager.class);
    cache.clear(textures);
    verifyNoInteractions(textures);
    cache.bakeNextPending();
    assertEquals(1, this.bakes.size());
  }

  @Test
  @DisplayName(
      "a bake that throws leaves the portrait blank instead of crashing, and is not retried")
  void throwingBakeIsContained() {
    final PortraitTextureCache cache =
        new PortraitTextureCache(
            portrait -> {
              this.bakes.add(portrait);
              throw new IllegalStateException("no world");
            });
    cache.textureOf(portrait("Bentley", PortraitSize.SMALL));
    cache.bakeNextPending();
    assertNull(cache.textureOf(portrait("Bentley", PortraitSize.SMALL)));
    cache.bakeNextPending();
    assertEquals(1, this.bakes.size());
  }
}
