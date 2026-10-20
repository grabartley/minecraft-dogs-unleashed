package com.grahambartley.dogsunleashed.render.statue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StatueCoatTextureCacheTest {

  private static final Identifier COAT =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_tri1.png");

  private final List<String> carves = new ArrayList<>();

  private StatueCoatTextureCache cache(final boolean succeeds) {
    return new StatueCoatTextureCache(
        (coat, material) -> {
          this.carves.add(coat.getPath() + "@" + material.id());
          return succeeds ? Identifier.of("dogs-unleashed", "statue/" + material.id()) : null;
        });
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(StatueMaterial.class)
  @DisplayName("each material gets its own carved texture for the same coat")
  void eachMaterialIsCarvedSeparately(final StatueMaterial material) {
    assertEquals(
        Identifier.of("dogs-unleashed", "statue/" + material.id()),
        cache(true).statueOf(COAT, material));
  }

  @Test
  @DisplayName("a coat is carved once per material and then reused")
  void carvesOncePerMaterial() {
    final StatueCoatTextureCache cache = cache(true);
    cache.statueOf(COAT, StatueMaterial.GOLD);
    cache.statueOf(COAT, StatueMaterial.GOLD);
    cache.statueOf(COAT, StatueMaterial.STONE);
    assertEquals(
        List.of("textures/entity/beagle_tri1.png@gold", "textures/entity/beagle_tri1.png@stone"),
        this.carves);
  }

  @Test
  @DisplayName("a coat that cannot be carved falls back to the coat itself, and is not retried")
  void failedCarveFallsBackToTheCoat() {
    final StatueCoatTextureCache cache = cache(false);
    assertEquals(COAT, cache.statueOf(COAT, StatueMaterial.STONE));
    assertEquals(COAT, cache.statueOf(COAT, StatueMaterial.STONE));
    assertEquals(1, this.carves.size());
  }

  @Test
  @DisplayName("clearing destroys carved textures but never the original coat")
  void clearDestroysOnlyCarvedTextures() {
    final StatueCoatTextureCache carved = cache(true);
    carved.statueOf(COAT, StatueMaterial.COPPER);
    final TextureManager textures = mock(TextureManager.class);
    carved.clear(textures);
    verify(textures).destroyTexture(Identifier.of("dogs-unleashed", "statue/copper"));

    final StatueCoatTextureCache failed = cache(false);
    failed.statueOf(COAT, StatueMaterial.COPPER);
    final TextureManager untouched = mock(TextureManager.class);
    failed.clear(untouched);
    verify(untouched, never()).destroyTexture(COAT);
  }

  @Test
  @DisplayName("after clearing, the next request carves again")
  void clearForgetsCarvedTextures() {
    final StatueCoatTextureCache cache = cache(true);
    cache.statueOf(COAT, StatueMaterial.GOLD);
    cache.clear(mock(TextureManager.class));
    cache.statueOf(COAT, StatueMaterial.GOLD);
    assertEquals(2, this.carves.size());
  }
}
