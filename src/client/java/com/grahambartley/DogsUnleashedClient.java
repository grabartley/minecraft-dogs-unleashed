package com.grahambartley;

import com.grahambartley.render.DalmatianRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    // Register GeckoLib entity renderer
    EntityRendererRegistry.register(ModEntities.DALMATIAN, DalmatianRenderer::new);
  }
}
