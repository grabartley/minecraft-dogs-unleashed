package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

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
    this.dummyEntity.readComponents(stack);

    matrices.push();
    PlacedBlockItemPose.apply(matrices, DogBedBlockEntityRenderer.BED_SCALE);

    this.renderer.renderAsItem(matrices, this.dummyEntity, vertexConsumers, light, overlay);

    matrices.pop();
  }
}
