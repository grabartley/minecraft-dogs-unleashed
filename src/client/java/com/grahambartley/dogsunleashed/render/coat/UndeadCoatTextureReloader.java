package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class UndeadCoatTextureReloader implements SimpleSynchronousResourceReloadListener {

  public static final Identifier ID = Identifier.of(MOD_ID, "undead_coat_textures");

  private final UndeadCoatTextureCache cache;
  private final Supplier<TextureManager> textureManager;

  public UndeadCoatTextureReloader(
      final UndeadCoatTextureCache cache, final Supplier<TextureManager> textureManager) {
    this.cache = cache;
    this.textureManager = textureManager;
  }

  @Override
  public Identifier getFabricId() {
    return ID;
  }

  @Override
  public void reload(final ResourceManager manager) {
    this.cache.clear(this.textureManager.get());
  }
}
