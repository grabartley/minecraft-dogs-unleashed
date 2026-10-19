package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.cosmetic.DrawnCosmetic;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadFit;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadFits;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadMotion;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogMobHeadLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final float SKULL_EDGE = 0.5f;
  private static final float BLOCK_EDGE = 1.0f;

  private final MobHeadFits fits;
  private final Map<SkullBlock.SkullType, SkullBlockEntityModel> skullModels;
  private final Map<MobHead, SkullBlock.SkullType> skullTypes = new EnumMap<>(MobHead.class);
  private final Map<MobHead, BlockState> blockStates = new EnumMap<>(MobHead.class);
  private final BlockRenderManager blocks;

  private Optional<MobHead> frameHead = Optional.empty();
  private Optional<MobHeadFit> frameFit = Optional.empty();

  public DogMobHeadLayer(
      final GeoRenderer<UnleashedDogEntity> renderer,
      final MobHeadFits fits,
      final EntityRendererFactory.Context context) {
    super(renderer);
    this.fits = fits;
    this.skullModels = SkullBlockEntityRenderer.getModels(context.getModelLoader());
    this.blocks = context.getBlockRenderManager();
    for (final MobHead head : MobHead.values()) {
      final Block block = ((BlockItem) head.item()).getBlock();
      if (head.isSkull()) {
        this.skullTypes.put(head, ((AbstractSkullBlock) block).getSkullType());
      } else {
        this.blockStates.put(head, block.getDefaultState());
      }
    }
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
    this.frameHead = DrawnCosmetic.mobHeadOn(animatable);
    this.frameFit =
        this.frameHead.flatMap(
            head -> this.fits.of(bakedModel, RigProfiles.of(animatable.getRigSourceBreed()), head));
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
    if (this.frameHead.isEmpty()
        || this.frameFit.isEmpty()
        || !bone.getName().equals(this.frameFit.get().boneName())) {
      return;
    }
    final MobHead head = this.frameHead.get();
    final MobHeadFit fit = this.frameFit.get();
    poseStack.push();
    final Vector3f base = fit.base();
    poseStack.translate(base.x, base.y, base.z);
    poseStack.multiply(fit.restRotation().conjugate());
    final float scale = fit.edge() / (head.isSkull() ? SKULL_EDGE : BLOCK_EDGE);
    poseStack.scale(scale, scale, scale * fit.depthStretch());
    poseStack.translate(-0.5f, 0.0f, -0.5f);
    if (head.isSkull()) {
      this.drawSkull(poseStack, animatable, head, bufferSource, partialTick, packedLight);
    } else {
      this.blocks.renderBlockAsEntity(
          this.blockStates.get(head),
          poseStack,
          bufferSource,
          packedLight,
          OverlayTexture.DEFAULT_UV);
    }
    poseStack.pop();
  }

  private void drawSkull(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final MobHead head,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight) {
    final SkullBlock.SkullType type = this.skullTypes.get(head);
    SkullBlockEntityRenderer.renderSkull(
        null,
        0.0f,
        MobHeadMotion.progress(
            head,
            animatable.limbAnimator.getPos(partialTick),
            animatable.getVocalization().ticksSinceBark(partialTick)),
        poseStack,
        bufferSource,
        packedLight,
        this.skullModels.get(type),
        SkullBlockEntityRenderer.getRenderLayer(type, null));
  }
}
