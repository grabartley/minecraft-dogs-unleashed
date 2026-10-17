package com.grahambartley.dogsunleashed.render.coat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class UndeadCoatTextureCache {

  @FunctionalInterface
  public interface Decayer {
    @Nullable
    Identifier decay(Identifier living, UndeadDogEyes eyes);
  }

  private final Decayer decayer;
  private final Map<Identifier, Identifier> undeadByLiving = new HashMap<>();

  public UndeadCoatTextureCache(final Decayer decayer) {
    this.decayer = decayer;
  }

  public Identifier undeadOf(final Identifier living, final UndeadDogEyes eyes) {
    return this.undeadByLiving.computeIfAbsent(
        living,
        key -> {
          final Identifier undead = this.decayer.decay(key, eyes);
          return undead != null ? undead : key;
        });
  }

  public void clear(final TextureManager textureManager) {
    this.undeadByLiving.forEach(
        (living, undead) -> {
          if (!undead.equals(living)) {
            textureManager.destroyTexture(undead);
          }
        });
    this.undeadByLiving.clear();
  }
}
