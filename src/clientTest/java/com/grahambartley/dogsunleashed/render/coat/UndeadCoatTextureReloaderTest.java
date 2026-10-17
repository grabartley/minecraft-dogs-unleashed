package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndeadCoatTextureReloaderTest {

  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/husky_blackwhite_blueblue.png");
  private static final Identifier GENERATED = Identifier.of("dogs-unleashed", "undead/husky");

  @Test
  @DisplayName("the listener is registered under the mod's namespace")
  void idIsNamespacedToTheMod() {
    final UndeadCoatTextureReloader reloader =
        new UndeadCoatTextureReloader(
            new UndeadCoatTextureCache((living, eyes) -> null), () -> mock(TextureManager.class));

    assertEquals("dogs-unleashed", reloader.getFabricId().getNamespace());
  }

  @Test
  @DisplayName("a resource reload frees the generated texture through the live texture manager")
  void reloadFreesGeneratedTextures() {
    final UndeadCoatTextureCache cache = new UndeadCoatTextureCache((living, eyes) -> GENERATED);
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);
    final TextureManager textureManager = mock(TextureManager.class);
    final UndeadCoatTextureReloader reloader =
        new UndeadCoatTextureReloader(cache, () -> textureManager);

    reloader.reload(mock(ResourceManager.class));

    verify(textureManager).destroyTexture(GENERATED);
  }

  @Test
  @DisplayName("after a reload the coat is decayed again from the new textures")
  void reloadMakesTheNextLookupRebuild() {
    final int[] decays = {0};
    final UndeadCoatTextureCache cache =
        new UndeadCoatTextureCache(
            (living, eyes) -> {
              decays[0]++;
              return GENERATED;
            });
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);
    final UndeadCoatTextureReloader reloader =
        new UndeadCoatTextureReloader(cache, () -> mock(TextureManager.class));

    reloader.reload(mock(ResourceManager.class));
    cache.undeadOf(LIVING, UndeadDogEyes.HUSKY);

    assertEquals(2, decays[0]);
  }
}
