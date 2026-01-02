package com.grahambartley;

import com.grahambartley.render.BeagleRenderer;
import com.grahambartley.render.DachshundRenderer;
import com.grahambartley.render.DogBedBlockEntityRenderer;
import com.grahambartley.render.HuskyRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    EntityRendererRegistry.register(ModEntities.HUSKY, HuskyRenderer::new);
    EntityRendererRegistry.register(ModEntities.DACHSHUND, DachshundRenderer::new);
    EntityRendererRegistry.register(ModEntities.BEAGLE, BeagleRenderer::new);

    BlockEntityRendererFactories.register(ModBlockEntities.DOG_BED, DogBedBlockEntityRenderer::new);
  }
}
