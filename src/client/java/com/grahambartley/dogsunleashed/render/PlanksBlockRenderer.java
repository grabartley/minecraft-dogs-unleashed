package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.render.layer.PlanksLayer;
import java.util.function.Function;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public abstract class PlanksBlockRenderer<T extends BlockEntity & GeoAnimatable>
    extends GeoBlockRenderer<T> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  protected PlanksBlockRenderer(final GeoModel<T> model, final Function<T, WoodType> woodOf) {
    super(model);
    this.addRenderLayer(new PlanksLayer<>(this, woodOf));
  }

  public void renderAsItem(
      final MatrixStack matrices,
      final T blockEntity,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    this.animatable = blockEntity;
    final BakedGeoModel model =
        this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(blockEntity));
    final RenderLayer renderType =
        this.getRenderType(
            blockEntity, this.getTextureLocation(blockEntity), vertexConsumers, 1.0f);
    this.actuallyRender(
        matrices,
        blockEntity,
        model,
        renderType,
        vertexConsumers,
        vertexConsumers.getBuffer(renderType),
        false,
        1.0f,
        light,
        overlay,
        OPAQUE_WHITE);
  }
}
