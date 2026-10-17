package com.grahambartley.dogsunleashed.render;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class CacheReloader implements SimpleSynchronousResourceReloadListener {

  private final Identifier id;
  private final Runnable clear;

  public CacheReloader(final Identifier id, final Runnable clear) {
    this.id = id;
    this.clear = clear;
  }

  @Override
  public Identifier getFabricId() {
    return this.id;
  }

  @Override
  public void reload(final ResourceManager manager) {
    this.clear.run();
  }
}
