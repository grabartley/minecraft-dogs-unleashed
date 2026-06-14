package com.grahambartley.render.layer;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.block.entity.StickBlockEntity;
import com.grahambartley.block.entity.TennisBallBlockEntity;
import com.grahambartley.entity.CarryProfile;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import com.grahambartley.model.FrisbeeModel;
import com.grahambartley.model.StickModel;
import com.grahambartley.model.TennisBallModel;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryFetchItemLayer<T extends UnleashedDogEntity> extends BlockAndItemGeoLayer<T> {
  private static final String HEAD_BONE_NAME = "head";
  private static final int FULL_BRIGHT_COLOR = 0xFFFFFFFF;
  private static final float NO_PARTIAL_TICK = 1.0f;

  private final GeoBlockRenderer<TennisBallBlockEntity> tennisBallRenderer;
  private final TennisBallBlockEntity tennisBallDummy;
  private final GeoBlockRenderer<StickBlockEntity> stickRenderer;
  private final StickBlockEntity stickDummy;
  private final GeoBlockRenderer<FrisbeeBlockEntity> frisbeeRenderer;
  private final FrisbeeBlockEntity frisbeeDummy;

  public DogCarryFetchItemLayer(GeoRenderer<T> renderer) {
    super(renderer);
    this.tennisBallRenderer = new GeoBlockRenderer<>(new TennisBallModel());
    this.tennisBallDummy =
        new TennisBallBlockEntity(BlockPos.ORIGIN, ModBlocks.TENNIS_BALL.getDefaultState());
    this.stickRenderer = new GeoBlockRenderer<>(new StickModel());
    this.stickDummy = new StickBlockEntity(BlockPos.ORIGIN, ModBlocks.STICK.getDefaultState());
    this.frisbeeRenderer = new GeoBlockRenderer<>(new FrisbeeModel());
    this.frisbeeDummy =
        new FrisbeeBlockEntity(BlockPos.ORIGIN, ModBlocks.FRISBEE.getDefaultState());
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

    if (fetchType == FetchTypes.TENNIS_BALL) {
      renderGeo(
          tennisBallRenderer,
          tennisBallDummy,
          poseStack,
          bufferSource,
          packedLight,
          packedOverlay,
          FULL_BRIGHT_COLOR);
      return;
    }
    if (fetchType == FetchTypes.STICK) {
      renderGeo(
          stickRenderer,
          stickDummy,
          poseStack,
          bufferSource,
          packedLight,
          packedOverlay,
          FULL_BRIGHT_COLOR);
      return;
    }
    if (fetchType == FetchTypes.FRISBEE) {
      DyeColor color = stack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE);
      frisbeeDummy.setColor(color);
      int tint = color.getEntityColor() | 0xFF000000;
      renderGeo(
          frisbeeRenderer, frisbeeDummy, poseStack, bufferSource, packedLight, packedOverlay, tint);
      return;
    }

    super.renderStackForBone(
        poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
  }

  private static <A extends BlockEntity & GeoAnimatable> void renderGeo(
      GeoBlockRenderer<A> renderer,
      A animatable,
      MatrixStack poseStack,
      VertexConsumerProvider bufferSource,
      int packedLight,
      int packedOverlay,
      int color) {
    var geoModel = renderer.getGeoModel();
    BakedGeoModel bakedModel = geoModel.getBakedModel(geoModel.getModelResource(animatable));
    RenderLayer renderType =
        renderer.getRenderType(
            animatable, renderer.getTextureLocation(animatable), bufferSource, NO_PARTIAL_TICK);
    VertexConsumer buffer = bufferSource.getBuffer(renderType);
    renderer.actuallyRender(
        poseStack,
        animatable,
        bakedModel,
        renderType,
        bufferSource,
        buffer,
        false,
        NO_PARTIAL_TICK,
        packedLight,
        packedOverlay,
        color);
  }
}
