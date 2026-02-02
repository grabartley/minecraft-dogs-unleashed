package com.grahambartley;

import com.grahambartley.network.ModNetworkingClient;
import com.grahambartley.render.BeagleRenderer;
import com.grahambartley.render.DachshundRenderer;
import com.grahambartley.render.DogBedBlockEntityRenderer;
import com.grahambartley.render.DogBedItemRenderer;
import com.grahambartley.render.GoldenRetrieverRenderer;
import com.grahambartley.render.HuskyRenderer;
import com.grahambartley.render.ShibaInuRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    EntityRendererRegistry.register(ModEntities.HUSKY, HuskyRenderer::new);
    EntityRendererRegistry.register(ModEntities.DACHSHUND, DachshundRenderer::new);
    EntityRendererRegistry.register(ModEntities.BEAGLE, BeagleRenderer::new);
    EntityRendererRegistry.register(ModEntities.GOLDEN_RETRIEVER, GoldenRetrieverRenderer::new);
    EntityRendererRegistry.register(ModEntities.SHIBA_INU, ShibaInuRenderer::new);

    BlockEntityRendererFactories.register(ModBlockEntities.DOG_BED, DogBedBlockEntityRenderer::new);

    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_BED, new DogBedItemRenderer());

    ModKeyBindings.register();
    ModNetworkingClient.registerClientReceivers();
  }
}
