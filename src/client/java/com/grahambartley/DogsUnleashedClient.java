package com.grahambartley;

import com.grahambartley.render.BeagleRenderer;
import com.grahambartley.render.DachshundRenderer;
import com.grahambartley.render.HuskyRenderer;
import com.grahambartley.render.ShibaInuRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    EntityRendererRegistry.register(ModEntities.HUSKY, HuskyRenderer::new);
    EntityRendererRegistry.register(ModEntities.DACHSHUND, DachshundRenderer::new);
    EntityRendererRegistry.register(ModEntities.BEAGLE, BeagleRenderer::new);
    EntityRendererRegistry.register(ModEntities.SHIBA_INU, ShibaInuRenderer::new);
  }
}
