package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.StickBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.TennisBallBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.model.FrisbeeModel;
import com.grahambartley.dogsunleashed.model.StickModel;
import com.grahambartley.dogsunleashed.model.TennisBallModel;
import com.grahambartley.dogsunleashed.render.DogRenderer;
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
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class DogCarryFetchItemLayer extends BlockAndItemGeoLayer<UnleashedDogEntity> {
  private static final int FULL_BRIGHT_COLOR = 0xFFFFFFFF;
  private static final float NO_PARTIAL_TICK = 1.0f;

  private final GeoBlockRenderer<TennisBallBlockEntity> tennisBallRenderer;
  private final TennisBallBlockEntity tennisBallDummy;
  private final GeoBlockRenderer<StickBlockEntity> stickRenderer;
  private final StickBlockEntity stickDummy;
  private final GeoBlockRenderer<FrisbeeBlockEntity> frisbeeRenderer;
  private final FrisbeeBlockEntity frisbeeDummy;
  private final DogRenderer dogRenderer;

  public DogCarryFetchItemLayer(DogRenderer renderer) {
    super(renderer);
    this.dogRenderer = renderer;
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
  protected ItemStack getStackForBone(GeoBone bone, UnleashedDogEntity animatable) {
    if (animatable.isInvisible() || !animatable.getFetchCarry().isCarrying()) {
      return null;
    }
    if (!RigProfiles.of(animatable.getRigSourceBreed())
        .mouthBone()
        .filter(bone.getName()::equals)
        .isPresent()) {
      return null;
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
      UnleashedDogEntity animatable,
      VertexConsumerProvider bufferSource,
      float partialTick,
      int packedLight,
      int packedOverlay) {
    FetchItemType fetchType = animatable.getFetchCarry().activeType();
    if (fetchType == null) {
      return;
    }
    final double forward = forwardOf(animatable);
    final Vec3d grip = MouthGrip.of(bone, forward);
    poseStack.translate(grip.x, grip.y, grip.z);
    final float toyScale =
        CarriedToySize.scaleInFrame(
            animatable.getRigSourceBreed().carryProfileFor(fetchType).scale(),
            animatable.getRigSourceBreed().renderTransforms().adultScale(),
            this.dogRenderer.scaleBelowEntityRoot(poseStack.peek().getPositionMatrix()));
    final Placement placement =
        new Placement(ToyHold.forToy(fetchType.id().getPath()), forward, toyScale);

    if (fetchType == FetchTypes.TENNIS_BALL) {
      renderGeo(
          tennisBallRenderer,
          tennisBallDummy,
          placement,
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
          placement,
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
          frisbeeRenderer,
          frisbeeDummy,
          placement,
          poseStack,
          bufferSource,
          packedLight,
          packedOverlay,
          tint);
      return;
    }

    super.renderStackForBone(
        poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
  }

  private static double forwardOf(final UnleashedDogEntity dog) {
    return -Math.cos(
        Math.toRadians(dog.getRigSourceBreed().renderTransforms().bodyYawOffsetDegrees()));
  }

  private static <A extends BlockEntity & GeoAnimatable> void renderGeo(
      GeoBlockRenderer<A> renderer,
      A animatable,
      Placement placement,
      MatrixStack poseStack,
      VertexConsumerProvider bufferSource,
      int packedLight,
      int packedOverlay,
      int color) {
    var geoModel = renderer.getGeoModel();
    BakedGeoModel bakedModel = geoModel.getBakedModel(geoModel.getModelResource(animatable));
    placement.apply(poseStack, bakedModel);
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

  private record Placement(ToyHold hold, double forward, float scale) {
    void apply(final MatrixStack poseStack, final BakedGeoModel toy) {
      final Vec3d point = this.hold.point(ToyBounds.of(toy), this.forward);
      poseStack.multiply(
          RotationAxis.POSITIVE_X.rotationDegrees(this.hold.droopDegrees(this.forward)));
      poseStack.scale(this.scale, this.scale, this.scale);
      poseStack.translate(-point.x, -point.y, -point.z);
    }
  }
}
