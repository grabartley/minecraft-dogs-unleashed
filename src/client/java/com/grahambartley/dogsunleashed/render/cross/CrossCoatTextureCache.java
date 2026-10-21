package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CrossCoatTextureCache {

  public record CrossCoat(
      Identifier living, UnleashedDogBreed rig, Identifier ownPalette, List<PaletteWeight> pulls) {

    public CrossCoat {
      pulls = List.copyOf(pulls);
    }
  }

  @FunctionalInterface
  public interface Painter {
    @Nullable
    Identifier paint(CrossCoat coat);
  }

  private final Painter painter;
  private final Map<CrossCoat, Identifier> tinted = new HashMap<>();

  public CrossCoatTextureCache(final Painter painter) {
    this.painter = painter;
  }

  public synchronized Identifier tintedOf(final CrossCoat coat) {
    if (coat.pulls().isEmpty()) {
      return coat.living();
    }
    return this.tinted.computeIfAbsent(
        coat,
        key -> {
          final Identifier painted = this.painter.paint(key);
          return painted != null ? painted : key.living();
        });
  }

  public synchronized void clear(final TextureManager textureManager) {
    this.tinted.forEach(
        (coat, texture) -> {
          if (!texture.equals(coat.living())) {
            textureManager.destroyTexture(texture);
          }
        });
    this.tinted.clear();
  }
}
