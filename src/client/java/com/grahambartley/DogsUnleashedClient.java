package com.grahambartley;

import com.grahambartley.model.DalmatianModel;
import com.grahambartley.render.DalmatianRenderer;
import com.grahambartley.render.ModModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    // Register entity renderers
    EntityRendererRegistry.register(ModEntities.DALMATIAN, DalmatianRenderer::new);

    // Register model layers
    EntityModelLayerRegistry.registerModelLayer(
        ModModelLayers.DALMATIAN, DalmatianModel::getTexturedModelData);
  }
}
