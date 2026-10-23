package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DogBedItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final DogBedBlockEntityRenderer renderer;
  private final DogBedBlockEntity dummyEntity;

  public DogBedItemRenderer() {
    this.dummyEntity = new DogBedBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_BED.getDefaultState());
    this.renderer = new DogBedBlockEntityRenderer(null);
  }

  @Override
  public void render(
      final ItemStack stack,
      final ModelTransformationMode mode,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    this.dummyEntity.setColor(stack.getOrDefault(ModComponents.DOG_BED_COLOR, DyeColor.WHITE));

    matrices.push();
    PlacedBlockItemPose.apply(matrices, DogBedBlockEntityRenderer.BED_SCALE);

    final BakedGeoModel bakedModel =
        this.renderer
            .getGeoModel()
            .getBakedModel(this.renderer.getGeoModel().getModelResource(this.dummyEntity));
    final RenderLayer renderType =
        this.renderer.getRenderType(
            this.dummyEntity,
            this.renderer.getTextureLocation(this.dummyEntity),
            vertexConsumers,
            1.0f);

    this.renderer.actuallyRender(
        matrices,
        this.dummyEntity,
        bakedModel,
        renderType,
        vertexConsumers,
        vertexConsumers.getBuffer(renderType),
        false,
        1.0f,
        light,
        overlay,
        0xFFFFFFFF);

    matrices.pop();
  }
}
