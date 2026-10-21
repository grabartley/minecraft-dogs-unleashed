package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class FittedGeoItemRenderer<T extends Item & GeoItem>
    implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final Supplier<GeoModel<T>> model;
  private FittedRenderer<T> renderer;

  public FittedGeoItemRenderer(final Supplier<GeoModel<T>> model) {
    this.model = model;
  }

  @Override
  public void render(
      final ItemStack stack,
      final ModelTransformationMode mode,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    if (this.renderer == null) {
      this.renderer = new FittedRenderer<>(this.model.get());
    }
    this.renderer.render(stack, mode, matrices, vertexConsumers, light, overlay);
  }

  static ModelFit fitOf(final BakedGeoModel model) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final GeoBone bone : model.topLevelBones()) {
      include(bounds, bone);
    }
    return bounds.isEmpty()
        ? new ModelFit(1.0f, new Vector3f(0.5f, 0.0f, 0.5f))
        : ModelFit.intoBlock(bounds.min(), bounds.max());
  }

  private static void include(final CubeGeometry.Bounds bounds, final GeoBone bone) {
    final CubeGeometry.Bounds own = CubeGeometry.posedBoundsOf(bone);
    if (!own.isEmpty()) {
      bounds.include(own.min());
      bounds.include(own.max());
    }
    for (final GeoBone child : bone.getChildBones()) {
      include(bounds, child);
    }
  }

  private static final class FittedRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {

    private BakedGeoModel fittedModel;
    private ModelFit fit;

    FittedRenderer(final GeoModel<T> model) {
      super(model);
    }

    @Override
    public void preRender(
        final MatrixStack poseStack,
        final T animatable,
        final BakedGeoModel model,
        final VertexConsumerProvider bufferSource,
        final VertexConsumer buffer,
        final boolean isReRender,
        final float partialTick,
        final int packedLight,
        final int packedOverlay,
        final int colour) {
      super.preRender(
          poseStack,
          animatable,
          model,
          bufferSource,
          buffer,
          isReRender,
          partialTick,
          packedLight,
          packedOverlay,
          colour);
      if (isReRender) {
        return;
      }
      if (this.fittedModel != model) {
        this.fittedModel = model;
        this.fit = fitOf(model);
      }
      poseStack.translate(-0.5f, -0.51f, -0.5f);
      final Vector3f offset = this.fit.offset();
      poseStack.translate(offset.x, offset.y, offset.z);
      poseStack.scale(this.fit.scale(), this.fit.scale(), this.fit.scale());
    }
  }
}
