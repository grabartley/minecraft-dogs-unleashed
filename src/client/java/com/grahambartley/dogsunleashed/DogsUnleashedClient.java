package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.config.SyncedServerConfig;
import com.grahambartley.dogsunleashed.item.RopeToyItem;
import com.grahambartley.dogsunleashed.item.SledItem;
import com.grahambartley.dogsunleashed.item.armor.DogArmorTier;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import com.grahambartley.dogsunleashed.model.RopeToyModel;
import com.grahambartley.dogsunleashed.model.SledModel;
import com.grahambartley.dogsunleashed.network.ModNetworkingClient;
import com.grahambartley.dogsunleashed.render.CacheReloader;
import com.grahambartley.dogsunleashed.render.DogBedBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogBedItemRenderer;
import com.grahambartley.dogsunleashed.render.DogGraveBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogGraveItemRenderer;
import com.grahambartley.dogsunleashed.render.DogHouseBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogHouseItemRenderer;
import com.grahambartley.dogsunleashed.render.DogRenderer;
import com.grahambartley.dogsunleashed.render.DogStatueBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.DogWhistleItemRenderer;
import com.grahambartley.dogsunleashed.render.FittedGeoItemRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeItemRenderer;
import com.grahambartley.dogsunleashed.render.FrisbeeProjectileRenderer;
import com.grahambartley.dogsunleashed.render.ModelFit;
import com.grahambartley.dogsunleashed.render.StickBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.StickProjectileRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallBlockEntityRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallItemRenderer;
import com.grahambartley.dogsunleashed.render.TennisBallProjectileRenderer;
import com.grahambartley.dogsunleashed.render.WaterBowlTint;
import com.grahambartley.dogsunleashed.render.armour.ArmourLook;
import com.grahambartley.dogsunleashed.render.coat.UndeadCoatTextures;
import com.grahambartley.dogsunleashed.render.coat.UndeadEyeGlows;
import com.grahambartley.dogsunleashed.render.cosmetic.BandanaPrints;
import com.grahambartley.dogsunleashed.render.costume.CostumePartsAnimator;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextures;
import com.grahambartley.dogsunleashed.render.cross.RigSlotSizes;
import com.grahambartley.dogsunleashed.render.layer.WolfDogArmourFeatureRenderer;
import com.grahambartley.dogsunleashed.render.overlay.MuddyCoat;
import com.grahambartley.dogsunleashed.render.overlay.ProjectedOverlays;
import com.grahambartley.dogsunleashed.render.overlay.SeniorMuzzle;
import com.grahambartley.dogsunleashed.render.overlay.SledHarnessCoat;
import com.grahambartley.dogsunleashed.render.pawprint.PawPrints;
import com.grahambartley.dogsunleashed.render.portrait.DogPortraitEntityRenderer;
import com.grahambartley.dogsunleashed.render.portrait.PortraitTextures;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotRegions;
import com.grahambartley.dogsunleashed.render.sled.HarnessLines;
import com.grahambartley.dogsunleashed.render.sled.SledAnchors;
import com.grahambartley.dogsunleashed.render.sled.SledRenderer;
import com.grahambartley.dogsunleashed.render.statue.StatueCoatTextures;
import com.grahambartley.dogsunleashed.riding.RideInputSync;
import com.grahambartley.dogsunleashed.riding.RideStaminaHud;
import com.grahambartley.dogsunleashed.riding.SledInputSync;
import com.grahambartley.dogsunleashed.screen.DogEquipmentScreen;
import com.grahambartley.dogsunleashed.tug.TugMeterHud;
import com.grahambartley.dogsunleashed.whistle.WhistlePointer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.math.ColorHelper;

public class DogsUnleashedClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    EntityRendererRegistry.register(
        ModEntities.TENNIS_BALL_PROJECTILE, TennisBallProjectileRenderer::new);
    EntityRendererRegistry.register(ModEntities.STICK_PROJECTILE, StickProjectileRenderer::new);
    EntityRendererRegistry.register(
        ModPortraits.DOG_PORTRAIT_ENTITY, DogPortraitEntityRenderer::new);
    EntityRendererRegistry.register(ModEntities.FRISBEE_PROJECTILE, FrisbeeProjectileRenderer::new);
    EntityRendererRegistry.register(ModEntities.SLED, SledRenderer::new);
    ColorProviderRegistry.ITEM.register(
        (stack, tintIndex) ->
            tintIndex > 0 ? -1 : DyedColorComponent.getColor(stack, ArmourLook.UNDYED_LEATHER),
        ModArmorItems.of(DogArmorTier.LEATHER));
    for (final DogCosmetic cosmetic : DogCosmetic.values()) {
      if (cosmetic.isDyeable()) {
        registerDyeTint(ModCosmeticItems.of(cosmetic), cosmetic.undyedColor());
      }
    }
    for (final DogCostume costume : DogCostume.values()) {
      if (costume.isDyeable()) {
        registerDyeTint(ModCostumeItems.of(costume), costume.undyedColor());
      }
    }
    EntityRendererRegistry.register(ModEntities.HUSKY, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.DACHSHUND, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.BEAGLE, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.GOLDEN_RETRIEVER, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.SHIBA_INU, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.PINSCHER, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.DIRE_WOLF, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.CROSS_BREED, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_HUSKY, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_DACHSHUND, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_BEAGLE, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_GOLDEN_RETRIEVER, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_SHIBA_INU, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_PINSCHER, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_DIRE_WOLF, DogRenderer::new);
    EntityRendererRegistry.register(ModEntities.ZOMBIE_CROSS_BREED, DogRenderer::new);

    BlockEntityRendererFactories.register(ModBlockEntities.DOG_BED, DogBedBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.DOG_GRAVE, DogGraveBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.DOG_HOUSE, DogHouseBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.TENNIS_BALL, TennisBallBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(ModBlockEntities.STICK, StickBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.FRISBEE, FrisbeeBlockEntityRenderer::new);
    BlockEntityRendererFactories.register(
        ModBlockEntities.DOG_STATUE, DogStatueBlockEntityRenderer::new);

    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_BED, new DogBedItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_GRAVE, new DogGraveItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(ModBlocks.DOG_HOUSE, new DogHouseItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(
        ModItems.TENNIS_BALL, new TennisBallItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(ModItems.FRISBEE, new FrisbeeItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(
        ModItems.DOG_WHISTLE, new DogWhistleItemRenderer());
    BuiltinItemRendererRegistry.INSTANCE.register(
        ModItems.SLED, new FittedGeoItemRenderer<SledItem>(SledModel::new, ModelFit::intoBlock));
    BuiltinItemRendererRegistry.INSTANCE.register(
        ModItems.ROPE_TOY,
        new FittedGeoItemRenderer<RopeToyItem>(RopeToyModel::new, ModelFit::acrossBlock));

    LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
        (entityType, renderer, helper, context) -> {
          if (renderer instanceof WolfEntityRenderer wolfRenderer) {
            helper.register(
                new WolfDogArmourFeatureRenderer(wolfRenderer, context.getModelLoader()));
          }
        });

    HandledScreens.register(ModScreenHandlers.DOG_EQUIPMENT, DogEquipmentScreen::new);
    WhistlePointer.register();
    SeniorMuzzle.register();
    MuddyCoat.register();
    SledHarnessCoat.register();
    HarnessLines.register();
    PawPrints.register();
    CostumePartsAnimator.clearOnDisconnect();

    final ResourceManagerHelper resources =
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
    resources.registerReloadListener(
        new CacheReloader(
            UndeadCoatTextures.RELOADER_ID,
            () ->
                UndeadCoatTextures.cache()
                    .clear(MinecraftClient.getInstance().getTextureManager())));
    resources.registerReloadListener(
        new CacheReloader(
            CrossCoatTextures.RELOADER_ID,
            () ->
                CrossCoatTextures.cache()
                    .clear(MinecraftClient.getInstance().getTextureManager())));
    resources.registerReloadListener(
        new CacheReloader(RigSlotSizes.RELOADER_ID, RigSlotSizes::clear));
    resources.registerReloadListener(
        new CacheReloader(
            StatueCoatTextures.RELOADER_ID,
            () ->
                StatueCoatTextures.cache()
                    .clear(MinecraftClient.getInstance().getTextureManager())));
    resources.registerReloadListener(
        new CacheReloader(
            PortraitTextures.RELOADER_ID,
            () -> PortraitTextures.reset(MinecraftClient.getInstance().getTextureManager())));
    ClientTickEvents.END_CLIENT_TICK.register(client -> PortraitTextures.cache().bakePending());
    resources.registerReloadListener(
        new CacheReloader(RigProfiles.RELOADER_ID, RigProfiles.shared()::clear));
    resources.registerReloadListener(
        new CacheReloader(SledAnchors.RELOADER_ID, SledAnchors.shared()::clear));
    resources.registerReloadListener(
        new CacheReloader(SlotRegions.RELOADER_ID, SlotRegions.shared()::clear));
    resources.registerReloadListener(
        new CacheReloader(
            UndeadEyeGlows.RELOADER_ID,
            () ->
                UndeadEyeGlows.shared().clear(MinecraftClient.getInstance().getTextureManager())));
    resources.registerReloadListener(
        new CacheReloader(
            ProjectedOverlays.RELOADER_ID,
            () ->
                ProjectedOverlays.shared()
                    .clear(MinecraftClient.getInstance().getTextureManager())));

    resources.registerReloadListener(
        new CacheReloader(
            BandanaPrints.RELOADER_ID,
            () -> BandanaPrints.shared().clear(MinecraftClient.getInstance().getTextureManager())));

    ModKeyBindings.register();
    ModNetworkingClient.registerClientReceivers();
    SyncedServerConfig.resetOnDisconnect();
    KeybindDiscoveryNudge.register();
    RideInputSync.register();
    RideStaminaHud.register();
    TugMeterHud.register();
    SledInputSync.register();
    WaterBowlTint.register();
  }

  private static void registerDyeTint(final Item item, final int undyedColor) {
    ColorProviderRegistry.ITEM.register(
        (stack, tintIndex) ->
            tintIndex > 0
                ? -1
                : ColorHelper.Argb.fullAlpha(DyedColorComponent.getColor(stack, undyedColor)),
        item);
  }
}
