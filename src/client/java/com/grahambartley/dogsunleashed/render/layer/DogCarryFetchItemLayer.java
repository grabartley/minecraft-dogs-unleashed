package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.StickBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.TennisBallBlockEntity;
import com.grahambartley.dogsunleashed.entity.CarryProfile;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.model.FrisbeeModel;
import com.grahambartley.dogsunleashed.model.StickModel;
import com.grahambartley.dogsunleashed.model.TennisBallModel;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryFetchItemLayer<T extends UnleashedDogEntity> extends BlockAndItemGeoLayer<T> {
  private static final int FULL_BRIGHT_COLOR = 0xFFFFFFFF;
  private static final float NO_PARTIAL_TICK = 1.0f;
  private static final float ITEM_SIDEWAYS_DEGREES = 90.0f;

  private final GeoBlockRenderer<TennisBallBlockEntity> tennisBallRenderer;
  private final TennisBallBlockEntity tennisBallDummy;
  private final GeoBlockRenderer<StickBlockEntity> stickRenderer;
  private final StickBlockEntity stickDummy;
  private final GeoBlockRenderer<FrisbeeBlockEntity> frisbeeRenderer;
  private final FrisbeeBlockEntity frisbeeDummy;
  private final Matrix4f entityOrigin;

  public DogCarryFetchItemLayer(GeoRenderer<T> renderer, Matrix4f entityOrigin) {
    super(renderer);
    this.entityOrigin = entityOrigin;
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
    if (animatable.isInvisible() || !animatable.getFetchCarry().isCarrying()) {
      return null;
    }
    if (!RigProfiles.of(animatable.getRigSourceBreed())
        .mouthBone()
        .filter(bone.getName()::equals)
        .isPresent()) {
      return null;
    }

    if (animatable.getFetchCarry().isCarryingFetchedItem()) {
      return animatable.getFetchCarry().carriedStack().copy();
    }
    var fetchItemType = animatable.getFetchCarry().activeType();
    if (fetchItemType == null) {
      return null;
    }
    ItemStack carried = animatable.getFetchCarry().carriedStack();
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
    if (animatable.getFetchCarry().isCarryingFetchedItem()) {
      this.renderFetchedItem(
          poseStack,
          bone,
          stack,
          animatable,
          bufferSource,
          partialTick,
          packedLight,
          packedOverlay);
      return;
    }
    FetchItemType fetchType = animatable.getFetchCarry().activeType();
    if (fetchType == null) {
      return;
    }
    CarryProfile profile = animatable.getRigSourceBreed().carryProfileFor(fetchType);
    MouthFrame.level(poseStack, bone);
    poseStack.translate(0.0, profile.verticalOffset(), profile.forwardOffset());
    CarriedToy toy = CarriedToy.of(fetchType);
    float scale = toy.scaleIn(this.entityOrigin, poseStack.peek().getPositionMatrix());
    poseStack.scale(scale, scale, scale);
    poseStack.translate(0.0, toy.centreDrop(), 0.0);

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

  private void renderFetchedItem(
      MatrixStack poseStack,
      GeoBone bone,
      ItemStack stack,
      T animatable,
      VertexConsumerProvider bufferSource,
      float partialTick,
      int packedLight,
      int packedOverlay) {
    CarryProfile profile = animatable.getRigSourceBreed().carryProfileFor(null);
    MouthFrame.level(poseStack, bone);
    poseStack.translate(0.0, profile.verticalOffset(), profile.forwardOffset());
    float scale = CarriedToy.ITEM.scaleIn(this.entityOrigin, poseStack.peek().getPositionMatrix());
    poseStack.scale(scale, scale, scale);
    poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ITEM_SIDEWAYS_DEGREES));
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
