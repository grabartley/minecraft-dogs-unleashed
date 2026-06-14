package com.grahambartley.render.layer;

import com.grahambartley.ModBlocks;
import com.grahambartley.block.entity.StickBlockEntity;
import com.grahambartley.entity.CarryProfile;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import com.grahambartley.model.StickModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryFetchItemLayer<T extends UnleashedDogEntity> extends BlockAndItemGeoLayer<T> {
  private static final String HEAD_BONE_NAME = "head";
  private static final int FULL_BRIGHT_COLOR = 0xFFFFFFFF;

  private final GeoBlockRenderer<StickBlockEntity> stickRenderer;
  private final StickBlockEntity stickDummy;

  public DogCarryFetchItemLayer(GeoRenderer<T> renderer) {
    super(renderer);
    this.stickRenderer = new GeoBlockRenderer<>(new StickModel());
    this.stickDummy = new StickBlockEntity(BlockPos.ORIGIN, ModBlocks.STICK.getDefaultState());
  }

  @Override
  protected ItemStack getStackForBone(GeoBone bone, T animatable) {
    if (animatable.isInvisible() || !animatable.isCarryingFetchItem()) {
      return null;
    }
    if (!HEAD_BONE_NAME.equals(bone.getName())) {
      return null;
    }

    var fetchItemType = animatable.getActiveFetchType();
    if (fetchItemType == null) {
      return null;
    }
    ItemStack carried = animatable.getCarriedFetchItemStack();
    return carried.isEmpty() ? new ItemStack(fetchItemType.item()) : carried.copy();
  }

  @Override
  protected void renderStackForBone(
      MatrixStack poseStack,
      GeoBone bone,
      ItemStack stack,
      T animatable,
      VertexConsumerProvider bufferSource,
      float partialTick,
      int packedLight,
      int packedOverlay) {
    FetchItemType fetchType = animatable.getActiveFetchType();
    if (fetchType == null) {
      return;
    }
    CarryProfile profile = animatable.getBreed().carryProfileFor(fetchType);
    poseStack.translate(0.0, profile.verticalOffset(), profile.forwardOffset());
    poseStack.scale(profile.scale(), profile.scale(), profile.scale());

    if (fetchType == FetchTypes.STICK) {
      renderStickInMouth(poseStack, bufferSource, packedLight, packedOverlay);
      return;
    }

    super.renderStackForBone(
        poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
  }

  private void renderStickInMouth(
      MatrixStack poseStack,
      VertexConsumerProvider bufferSource,
      int packedLight,
      int packedOverlay) {
    var geoModel = stickRenderer.getGeoModel();
    BakedGeoModel bakedModel = geoModel.getBakedModel(geoModel.getModelResource(stickDummy));
    RenderLayer renderType =
        stickRenderer.getRenderType(
            stickDummy, stickRenderer.getTextureLocation(stickDummy), bufferSource, 1.0f);
    VertexConsumer buffer = bufferSource.getBuffer(renderType);
    stickRenderer.actuallyRender(
        poseStack,
        stickDummy,
        bakedModel,
        renderType,
        bufferSource,
        buffer,
        false,
        1.0f,
        packedLight,
        packedOverlay,
        FULL_BRIGHT_COLOR);
  }
}
