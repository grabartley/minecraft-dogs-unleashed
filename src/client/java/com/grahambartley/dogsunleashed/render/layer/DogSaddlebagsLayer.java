package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.armour.ArmourClearance;
import com.grahambartley.dogsunleashed.render.gear.SaddlebagsAssets;
import com.grahambartley.dogsunleashed.render.gear.SaddlebagsFit;
import com.grahambartley.dogsunleashed.render.rig.BackTopLocator;
import com.grahambartley.dogsunleashed.render.rig.BackTopLocator.BackTop;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogSaddlebagsLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private final Map<BakedGeoModel, Optional<BackTop>> backs = new MapMaker().weakKeys().makeMap();

  private Optional<BackTop> frameBack = Optional.empty();
  private BakedGeoModel frameModel;

  public DogSaddlebagsLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  @Override
  public void preRender(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel bakedModel,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    this.frameModel = bakedModel;
    this.frameBack =
        animatable.getComponents().get(DogSaddlebags.class).isWorn()
            ? this.backs.computeIfAbsent(
                bakedModel,
                model ->
                    BackTopLocator.locate(model, RigProfiles.of(animatable.getRigSourceBreed())))
            : Optional.empty();
  }

  @Override
  public void renderForBone(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (this.frameBack.isEmpty() || !bone.getName().equals(this.frameBack.get().boneName())) {
      return;
    }
    final BakedGeoModel bags = GeckoLibCache.getBakedModels().get(SaddlebagsAssets.MODEL);
    if (bags == null) {
      return;
    }
    final BackTop back = this.frameBack.get();
    final boolean armoured = !animatable.getArmour().worn().isEmpty();
    final Vector3f inflation =
        armoured
            ? ArmourClearance.inflationFor(this.frameModel, animatable.getRigSourceBreed())
            : new Vector3f();
    final float scale = SaddlebagsFit.scale(back.width(), inflation.x);
    final Vector3f point = back.point();
    poseStack.push();
    poseStack.translate(point.x, point.y, point.z);
    poseStack.multiply(back.restRotation().conjugate());
    poseStack.translate(0.0f, SaddlebagsFit.lift(armoured, inflation.y), 0.0f);
    poseStack.scale(scale, scale, scale);
    final RenderLayer layer = RenderLayer.getEntityCutoutNoCull(SaddlebagsAssets.TEXTURE);
    final VertexConsumer consumer = bufferSource.getBuffer(layer);
    for (final GeoBone bagBone : bags.topLevelBones()) {
      this.getRenderer()
          .renderRecursively(
              poseStack,
              animatable,
              bagBone,
              layer,
              bufferSource,
              consumer,
              true,
              partialTick,
              packedLight,
              packedOverlay,
              OPAQUE_WHITE);
    }
    poseStack.pop();
  }
}
