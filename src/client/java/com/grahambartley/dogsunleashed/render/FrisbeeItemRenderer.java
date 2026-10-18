package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class FrisbeeItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final FrisbeeBlockEntityRenderer renderer = new FrisbeeBlockEntityRenderer();
  private final FrisbeeBlockEntity dummyEntity =
      new FrisbeeBlockEntity(BlockPos.ORIGIN, ModBlocks.FRISBEE.getDefaultState());

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {
    this.dummyEntity.setColor(stack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE));
    this.renderer.render(this.dummyEntity, 1.0f, matrices, vertexConsumers, light, overlay);
  }
}
