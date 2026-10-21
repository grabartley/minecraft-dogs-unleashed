package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextureCache.CrossCoat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CrossCoatTextureCacheTest {

  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_tri1.png");
  private static final Identifier OWN =
      Identifier.of("dogs-unleashed", "textures/entity/palette/beagle_tri1.png");
  private static final Identifier HUSKY =
      Identifier.of("dogs-unleashed", "textures/entity/palette/husky_agouti.png");
  private static final Identifier PAINTED = Identifier.of("dogs-unleashed", "cross/painted");
  private static final CrossCoat CROSS =
      new CrossCoat(LIVING, UnleashedDogBreed.BEAGLE, OWN, List.of(new PaletteWeight(HUSKY, 50)));

  private final List<CrossCoat> painted = new ArrayList<>();

  private CrossCoatTextureCache cachePainting(final Identifier result) {
    return new CrossCoatTextureCache(
        coat -> {
          this.painted.add(coat);
          return result;
        });
  }

  @Test
  @DisplayName("a coat with nothing pulling on it is the living coat and is never painted")
  void noPullKeepsTheLivingCoat() {
    final CrossCoatTextureCache cache = cachePainting(PAINTED);

    assertEquals(
        LIVING, cache.tintedOf(new CrossCoat(LIVING, UnleashedDogBreed.BEAGLE, OWN, List.of())));
    assertEquals(List.of(), this.painted);
  }

  @Test
  @DisplayName("each cross coat is painted once while the cache holds it")
  void paintsEachCoatOnce() {
    final CrossCoatTextureCache cache = cachePainting(PAINTED);

    assertEquals(PAINTED, cache.tintedOf(CROSS));
    assertEquals(PAINTED, cache.tintedOf(CROSS));
    assertEquals(List.of(CROSS), this.painted);
  }

  @ParameterizedTest(name = "painter gives {0}")
  @MethodSource("paintResults")
  @DisplayName("a painted coat is used, and one that cannot be painted stays living")
  void paintResultDecidesTheTexture(final Identifier painterResult, final Identifier expected) {
    assertEquals(expected, cachePainting(painterResult).tintedOf(CROSS));
  }

  static Stream<Arguments> paintResults() {
    return Stream.of(Arguments.of(PAINTED, PAINTED), Arguments.of(null, LIVING));
  }

  @Test
  @DisplayName("clearing destroys only painted textures and paints again on the next ask")
  void clearDestroysPaintedTextures() {
    final CrossCoatTextureCache cache = cachePainting(PAINTED);
    final TextureManager textures = mock(TextureManager.class);
    cache.tintedOf(CROSS);

    cache.clear(textures);
    cache.tintedOf(CROSS);

    verify(textures).destroyTexture(PAINTED);
    verify(textures, never()).destroyTexture(LIVING);
    assertEquals(List.of(CROSS, CROSS), this.painted);
  }
}
