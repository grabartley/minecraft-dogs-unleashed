package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndeadCoatTextureReloaderTest {

  @Test
  @DisplayName("the listener is registered under the mod's namespace")
  void idIsNamespacedToTheMod() {
    final UndeadCoatTextureReloader reloader =
        new UndeadCoatTextureReloader(() -> mock(TextureManager.class));

    assertEquals("dogs-unleashed", reloader.getFabricId().getNamespace());
  }

  @Test
  @DisplayName("every reload clears the cache against the live texture manager")
  @SuppressWarnings("unchecked")
  void everyReloadReadsTheLiveTextureManager() {
    final Supplier<TextureManager> textureManager = mock(Supplier.class);
    when(textureManager.get()).thenReturn(mock(TextureManager.class));
    final UndeadCoatTextureReloader reloader = new UndeadCoatTextureReloader(textureManager);

    reloader.reload(mock(ResourceManager.class));
    reloader.reload(mock(ResourceManager.class));

    verify(textureManager, times(2)).get();
  }
}
