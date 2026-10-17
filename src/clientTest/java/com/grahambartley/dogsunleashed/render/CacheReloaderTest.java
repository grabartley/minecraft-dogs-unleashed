package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.render.coat.UndeadCoatTextureCache;
import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CacheReloaderTest {

  private static final Identifier ID = Identifier.of("dogs-unleashed", "undead_coat_textures");
  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/husky_blackwhite_blueblue.png");
  private static final Identifier GENERATED = Identifier.of("dogs-unleashed", "undead/husky");

  @Test
  @DisplayName("the listener is registered under the id it was given")
  void idIsTheOneGiven() {
    assertEquals(ID, new CacheReloader(ID, () -> {}).getFabricId());
  }

  @Test
  @DisplayName("a resource reload frees a generated texture through the texture manager")
  void reloadFreesGeneratedTextures() {
    final UndeadCoatTextureCache cache = new UndeadCoatTextureCache((living, eyes) -> GENERATED);
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);
    final TextureManager textureManager = mock(TextureManager.class);

    new CacheReloader(ID, () -> cache.clear(textureManager)).reload(mock(ResourceManager.class));

    verify(textureManager).destroyTexture(GENERATED);
  }

  @Test
  @DisplayName("after a reload the cached value is built again")
  void reloadMakesTheNextLookupRebuild() {
    final int[] decays = {0};
    final UndeadCoatTextureCache cache =
        new UndeadCoatTextureCache(
            (living, eyes) -> {
              decays[0]++;
              return GENERATED;
            });
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);

    new CacheReloader(ID, () -> cache.clear(mock(TextureManager.class)))
        .reload(mock(ResourceManager.class));
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);

    assertEquals(2, decays[0]);
  }
}
