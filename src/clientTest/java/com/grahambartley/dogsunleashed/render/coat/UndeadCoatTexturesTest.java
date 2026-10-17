package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndeadCoatTexturesTest {

  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_tri1.png");

  @AfterEach
  void emptyCache() {
    UndeadCoatTextures.clear(mock(TextureManager.class));
  }

  @Test
  @DisplayName("a coat that cannot be decayed falls back to the living texture")
  void undecayableCoatFallsBackToLiving() {
    assertEquals(LIVING, UndeadCoatTextures.undeadOf(LIVING, UndeadDogEyes.BEAGLE));
  }

  @Test
  @DisplayName("clearing never destroys a living texture the cache passed through")
  void clearKeepsPassThroughLivingTexture() {
    UndeadCoatTextures.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    final TextureManager textureManager = mock(TextureManager.class);

    UndeadCoatTextures.clear(textureManager);

    verifyNoInteractions(textureManager);
  }

  @Test
  @DisplayName("clearing forgets cached entries so the next lookup rebuilds them")
  void clearForgetsCachedEntries() {
    UndeadCoatTextures.undeadOf(LIVING, UndeadDogEyes.BEAGLE);
    UndeadCoatTextures.clear(mock(TextureManager.class));
    final TextureManager textureManager = mock(TextureManager.class);

    UndeadCoatTextures.clear(textureManager);

    verifyNoInteractions(textureManager);
    assertEquals(LIVING, UndeadCoatTextures.undeadOf(LIVING, UndeadDogEyes.BEAGLE));
  }
}
