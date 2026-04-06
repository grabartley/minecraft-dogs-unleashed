package com.grahambartley.render.layer;

import com.grahambartley.ModItems;
import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryBallLayer<T extends UnleashedDogEntity> extends BlockAndItemGeoLayer<T> {

  public DogCarryBallLayer(GeoRenderer<T> renderer) {
    super(renderer);
  }

  @Override
  protected ItemStack getStackForBone(GeoBone bone, T animatable) {
    if (animatable.isInvisible() || !animatable.isCarryingBall()) {
      return null;
    }

    return "head".equals(bone.getName()) ? new ItemStack(ModItems.TENNIS_BALL) : null;
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
    return switch (dog.getBreedId()) {
      case "goldenretriever", "husky" -> 0.08;
      case "dachshund" -> -0.02;
      default -> 0.03;
    };
  }

  private static double getForwardOffset(UnleashedDogEntity dog) {
    return switch (dog.getBreedId()) {
      case "goldenretriever", "husky" -> -0.52;
      case "dachshund" -> -0.34;
      default -> -0.42;
    };
  }

  private static float getBallScale(UnleashedDogEntity dog) {
    return switch (dog.getBreedId()) {
      case "goldenretriever", "husky" -> 0.55f;
      case "dachshund" -> 0.42f;
      default -> 0.48f;
    };
  }
}
