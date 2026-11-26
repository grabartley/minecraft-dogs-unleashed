package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.block.WoodPlanks;
import com.grahambartley.dogsunleashed.render.PlanksBones;
import java.util.function.Function;
import net.minecraft.block.WoodType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PlanksLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private final Function<T, WoodType> woodOf;

  public PlanksLayer(final GeoRenderer<T> renderer, final Function<T, WoodType> woodOf) {
    super(renderer);
    this.woodOf = woodOf;
  }

  @Override
  public void renderForBone(
      final MatrixStack poseStack,
      final T animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (!PlanksBones.isPlanks(bone.getName())) {
      return;
    }
    final VertexConsumer planks =
        bufferSource.getBuffer(
            RenderLayer.getEntityCutoutNoCull(WoodPlanks.textureOf(this.woodOf.apply(animatable))));
    for (final GeoCube cube : bone.getCubes()) {
      poseStack.push();
      this.getRenderer()
          .renderCube(poseStack, cube, planks, packedLight, packedOverlay, OPAQUE_WHITE);
      poseStack.pop();
    }
  }
}
