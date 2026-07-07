package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.network.ClientSummonDebugProbe;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.render.BeagleRenderer;
import com.grahambartley.dogsunleashed.render.DachshundRenderer;
import com.grahambartley.dogsunleashed.render.DogBedBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogBedItemRenderer;
import com.grahambartley.dogsunleashed.render.DogGraveBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogGraveItemRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeItemRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeProjectileRenderer;
import com.grahambartley.dogsunleashed.render.GoldenRetrieverRenderer;
import com.grahambartley.dogsunleashed.render.HuskyRenderer;
import com.grahambartley.dogsunleashed.render.ShibaInuRenderer;
import com.grahambartley.dogsunleashed.render.StickBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.StickProjectileRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallItemRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallProjectileRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    EntityRendererRegistry.register(
        ModEntities.TENNIS_BALL_PROJECTILE, TennisBallProjectileRenderer::new);
    EntityRendererRegistry.register(ModEntities.STICK_PROJECTILE, StickProjectileRenderer::new);
    EntityRendererRegistry.register(ModEntities.FRISBEE_PROJECTILE, FrisbeeProjectileRenderer::new);
    EntityRendererRegistry.register(ModEntities.HUSKY, HuskyRenderer::new);
    EntityRendererRegistry.register(ModEntities.DACHSHUND, DachshundRenderer::new);
    EntityRendererRegistry.register(ModEntities.BEAGLE, BeagleRenderer::new);
    EntityRendererRegistry.register(ModEntities.GOLDEN_RETRIEVER, GoldenRetrieverRenderer::new);
    EntityRendererRegistry.register(ModEntities.SHIBA_INU, ShibaInuRenderer::new);

    BlockEntityRendererFactories.register(ModBlockEntities.DOG_BED, DogBedBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.DOG_GRAVE, DogGraveBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.TENNIS_BALL, TennisBallBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(ModBlockEntities.STICK, StickBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.FRISBEE, FrisbeeBlockEntityRenderer::new);

    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_BED, new DogBedItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_GRAVE, new DogGraveItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(
        ModItems.TENNIS_BALL, new TennisBallItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.FRISBEE, new FrisbeeItemRenderer());

    ModKeyBindings.register();
    ModNetworkingClient.registerClientReceivers();
    ClientSummonDebugProbe.register();
  }
}
