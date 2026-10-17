package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndeadCoatTextureCacheTest {

  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_tri1.png");
  private static final Identifier GENERATED = Identifier.of("dogs-unleashed", "undead/beagle_tri1");

  private final List<Identifier> decayCalls = new ArrayList<>();

  private UndeadCoatTextureCache cacheDecayingTo(final Identifier result) {
    return new UndeadCoatTextureCache(
        (living, eyes) -> {
          this.decayCalls.add(living);
          return result;
        });
  }

  @Test
  @DisplayName("returns the generated undead texture for a living coat")
  void returnsTheGeneratedTexture() {
    assertEquals(GENERATED, cacheDecayingTo(GENERATED).undeadOf(LIVING, UndeadDogEyes.BEAGLE));
  }

  @Test
  @DisplayName("decays each living coat once while the cache holds it")
  void decaysEachCoatOnce() {
    final UndeadCoatTextureCache cache = cacheDecayingTo(GENERATED);

    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);

    assertEquals(List.of(LIVING), this.decayCalls);
  }

  @Test
  @DisplayName("falls back to the living texture when the coat cannot be decayed")
  void fallsBackToLivingWhenDecayFails() {
    assertEquals(LIVING, cacheDecayingTo(null).undeadOf(LIVING, UndeadDogEyes.BEAGLE));
  }

  @Test
  @DisplayName("clearing frees each generated texture exactly once")
  void clearFreesGeneratedTextures() {
    final UndeadCoatTextureCache cache = cacheDecayingTo(GENERATED);
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    final TextureManager textureManager = mock(TextureManager.class);

    cache.clear(textureManager);

    verify(textureManager, times(1)).destroyTexture(GENERATED);
    verify(textureManager, never()).destroyTexture(LIVING);
  }

  @Test
  @DisplayName("clearing never frees a living texture the cache passed through")
  void clearKeepsPassThroughLivingTexture() {
    final UndeadCoatTextureCache cache = cacheDecayingTo(null);
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    final TextureManager textureManager = mock(TextureManager.class);

    cache.clear(textureManager);

    verifyNoInteractions(textureManager);
  }

  @Test
  @DisplayName("after clearing, the next lookup decays the coat again")
  void lookupAfterClearDecaysAgain() {
    final UndeadCoatTextureCache cache = cacheDecayingTo(GENERATED);
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);

    cache.clear(mock(TextureManager.class));
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);

    assertEquals(List.of(LIVING, LIVING), this.decayCalls);
  }

  @Test
  @DisplayName("a second clear with nothing rebuilt frees nothing")
  void repeatedClearFreesNothingTwice() {
    final UndeadCoatTextureCache cache = cacheDecayingTo(GENERATED);
    cache.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    cache.clear(mock(TextureManager.class));
    final TextureManager textureManager = mock(TextureManager.class);

    cache.clear(textureManager);

    verifyNoInteractions(textureManager);
  }
}
