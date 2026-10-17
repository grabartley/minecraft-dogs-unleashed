package com.grahambartley.dogsunleashed.render.coat;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class UndeadCoatTextureReloader implements SimpleSynchronousResourceReloadListener {

  public static final Identifier ID = Identifier.of(MOD_ID, "undead_coat_textures");

  private final Supplier<TextureManager> textureManager;

  public UndeadCoatTextureReloader(final Supplier<TextureManager> textureManager) {
    this.textureManager = textureManager;
  }

  @Override
  public Identifier getFabricId() {
    return ID;
  }

  @Override
  public void reload(final ResourceManager manager) {
    UndeadCoatTextures.clear(this.textureManager.get());
  }
}
