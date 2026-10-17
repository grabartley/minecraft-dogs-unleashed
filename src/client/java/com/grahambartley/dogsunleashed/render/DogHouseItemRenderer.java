package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DogHouseItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private static final double ITEM_CENTRE = 0.5;

  private static final float HOUSE_TO_BLOCK_SCALE = 0.5f;

  private static final double HOUSE_HALF_HEIGHT = 1.0;

  private final CushionTintedDogHouseRenderer renderer;
  private final DogHouseBlockEntity dummyEntity;

  public DogHouseItemRenderer() {
    this.dummyEntity =
        new DogHouseBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState());
    this.renderer = new CushionTintedDogHouseRenderer();
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    dummyEntity.setColor(stack.getOrDefault(ModComponents.DOG_HOUSE_COLOR, DyeColor.WHITE));

    matrices.push();
    matrices.translate(ITEM_CENTRE, ITEM_CENTRE, ITEM_CENTRE);
    matrices.scale(HOUSE_TO_BLOCK_SCALE, HOUSE_TO_BLOCK_SCALE, HOUSE_TO_BLOCK_SCALE);
    matrices.translate(0.0, -HOUSE_HALF_HEIGHT, 0.0);

    final BakedGeoModel bakedModel =
        renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(dummyEntity));
    final RenderLayer renderType =
        renderer.getRenderType(
            dummyEntity, renderer.getTextureLocation(dummyEntity), vertexConsumers, 1.0f);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
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
