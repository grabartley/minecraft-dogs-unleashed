package com.grahambartley.render.layer;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryFetchItemLayer<T extends UnleashedDogEntity> extends BlockAndItemGeoLayer<T> {
  private static final String HEAD_BONE_NAME = "head";

  public DogCarryFetchItemLayer(GeoRenderer<T> renderer) {
    super(renderer);
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
    return new ItemStack(
        fetchItemType != null ? fetchItemType.item() : FetchTypes.TENNIS_BALL.item());
  }

  @Override
  protected void renderStackForBone(
      MatrixStack poseStack,
      GeoBone bone,
      ItemStack stack,
      T animatable,
      net.minecraft.client.render.VertexConsumerProvider bufferSource,
      float partialTick,
      int packedLight,
      int packedOverlay) {
    poseStack.translate(0.0, getVerticalOffset(animatable), getForwardOffset(animatable));
    poseStack.scale(getBallScale(animatable), getBallScale(animatable), getBallScale(animatable));
    super.renderStackForBone(
        poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
  }

  private static double getVerticalOffset(UnleashedDogEntity dog) {
    return dog.getBreed().carryBallVerticalOffset();
  }

  private static double getForwardOffset(UnleashedDogEntity dog) {
    return dog.getBreed().carryBallForwardOffset();
  }

  private static float getBallScale(UnleashedDogEntity dog) {
    return dog.getBreed().carryBallScale();
  }
}
