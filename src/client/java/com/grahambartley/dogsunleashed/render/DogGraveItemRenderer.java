package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DogGraveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final DogGraveBlockEntityRenderer renderer;
  private final DogGraveBlockEntity dummyEntity;

  public DogGraveItemRenderer() {
    this.dummyEntity =
        new DogGraveBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_GRAVE.getDefaultState());
    this.renderer = new DogGraveBlockEntityRenderer(null);
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    final DyeColor flowerColor =
        stack.getOrDefault(ModComponents.DOG_GRAVE_FLOWER_COLOR, DyeColor.RED);
    dummyEntity.setFlowerColor(flowerColor);

    matrices.push();
    PlacedBlockItemPose.apply(matrices, DogGraveBlockEntityRenderer.GRAVE_SCALE);

    final BakedGeoModel bakedModel =
        renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(dummyEntity));
    final RenderLayer renderType =
        renderer.getRenderType(
            dummyEntity, renderer.getTextureLocation(dummyEntity), vertexConsumers, 1.0f);
    final var buffer = vertexConsumers.getBuffer(renderType);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
        bakedModel,
        renderType,
        vertexConsumers,
        buffer,
        false,
        1.0f,
        light,
        overlay,
        0xFFFFFFFF);

    final int flowerTint = flowerColor.getEntityColor();
    final RenderLayer flowerRenderType =
        RenderLayer.getEntityCutoutNoCull(
            Identifier.of(DogsUnleashed.MOD_ID, "textures/block/dog_grave_flower.png"));
    final var flowerBuffer = vertexConsumers.getBuffer(flowerRenderType);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
        bakedModel,
        flowerRenderType,
        vertexConsumers,
        flowerBuffer,
        true,
        1.0f,
        light,
        overlay,
        flowerTint);

    matrices.pop();
  }
}
