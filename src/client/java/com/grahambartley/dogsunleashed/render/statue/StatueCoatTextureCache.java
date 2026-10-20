package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class StatueCoatTextureCache {

  @FunctionalInterface
  public interface Carver {
    @Nullable
    Identifier carve(Identifier coat, StatueMaterial material);
  }

  private record Key(Identifier coat, StatueMaterial material) {}

  private final Carver carver;
  private final Map<Key, Identifier> carved = new HashMap<>();

  public StatueCoatTextureCache(final Carver carver) {
    this.carver = carver;
  }

  public Identifier statueOf(final Identifier coat, final StatueMaterial material) {
    return this.carved.computeIfAbsent(
        new Key(coat, material),
        key -> {
          final Identifier statue = this.carver.carve(key.coat(), key.material());
          return statue != null ? statue : key.coat();
        });
  }

  public void clear(final TextureManager textureManager) {
    this.carved.forEach(
        (key, statue) -> {
          if (!statue.equals(key.coat())) {
            textureManager.destroyTexture(statue);
          }
        });
    this.carved.clear();
  }
}
