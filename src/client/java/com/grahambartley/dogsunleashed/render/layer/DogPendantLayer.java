package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.render.rig.CharmMount;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator.CollarSpot;
import com.grahambartley.dogsunleashed.render.rig.CollarTextureMask;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.LivePose;
import com.grahambartley.dogsunleashed.render.rig.PoseClearance;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogPendantLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE = 0xFF000000;

  private record Mount(CollarSpot spot, CharmMount.Sides sides, Matrix4f charmToBone) {}

  private final Map<BakedGeoModel, Optional<Mount>> mounts = new MapMaker().weakKeys().makeMap();
  private final Map<BakedGeoModel, CubeGeometry.Bounds> charmBounds =
      new MapMaker().weakKeys().makeMap();

  private Optional<Mount> frameMount = Optional.empty();
  private BakedGeoModel frameModel;

  public DogPendantLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    this.frameMount =
        animatable.getPendants().worn() == null || animatable.isInvisible()
            ? Optional.empty()
            : this.mounts.computeIfAbsent(bakedModel, model -> mount(model, animatable));
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
    final Optional<Mount> mount = this.frameMount;
    final PendantType pendant = animatable.getPendants().worn();
    if (mount.isEmpty()
        || pendant == null
        || !bone.getName().equals(mount.get().spot().boneName())) {
      return;
    }
    final PendantCharm charm = PendantCharm.of(pendant.family());
    final BakedGeoModel charmModel = GeckoLibCache.getBakedModels().get(charm.model());
    if (charmModel == null) {
      return;
    }
    final Vector3f clearance =
        CharmMount.clearanceInBone(
            PoseClearance.liveBoxesOf(this.frameModel, mount.get().sides().body()),
            PoseClearance.liveBoxesOf(this.frameModel, mount.get().sides().head()),
            LivePose.of(bone),
            mount.get().charmToBone(),
            this.charmBounds.computeIfAbsent(charmModel, DogPendantLayer::boundsOf),
            mount.get().spot().facesNegativeZ());
    poseStack.push();
    poseStack.translate(clearance.x, clearance.y, clearance.z);
    CharmMount.place(poseStack, mount.get().spot());
    final RenderLayer charmLayer = RenderLayer.getEntityCutoutNoCull(charm.texture());
    final VertexConsumer charmBuffer = bufferSource.getBuffer(charmLayer);
    for (final GeoBone charmBone : charmModel.topLevelBones()) {
      this.getRenderer()
          .renderRecursively(
              poseStack,
              animatable,
              charmBone,
              charmLayer,
              bufferSource,
              charmBuffer,
              true,
              partialTick,
              packedLight,
              packedOverlay,
              OPAQUE | pendant.tint());
    }
    poseStack.pop();
  }

  private static CubeGeometry.Bounds boundsOf(final BakedGeoModel charmModel) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    for (final GeoBone charmBone : charmModel.topLevelBones()) {
      final CubeGeometry.Bounds part = CubeGeometry.posedBoundsOf(charmBone);
      if (!part.isEmpty()) {
        bounds.include(part.min());
        bounds.include(part.max());
      }
    }
    return bounds;
  }

  private static Optional<Mount> mount(
      final BakedGeoModel model, final UnleashedDogEntity animatable) {
    final String mouthBone = animatable.getRigSourceBreed().mouthAnchorBoneName();
    return locate(model, animatable)
        .map(
            spot ->
                new Mount(
                    spot,
                    CharmMount.sidesOf(model, spot, mouthBone),
                    CharmMount.charmToBone(spot)));
  }

  private static Optional<CollarSpot> locate(
      final BakedGeoModel model, final UnleashedDogEntity animatable) {
    final Identifier collarTexture =
        Identifier.of(
            DogsUnleashed.MOD_ID,
            "textures/entity/" + animatable.getRigSourceBreed().serializedId() + "_collar.png");
    return CollarTextureMask.load(MinecraftClient.getInstance().getResourceManager(), collarTexture)
        .flatMap(
            mask ->
                CollarBandLocator.locate(
                    model, animatable.getRigSourceBreed().mouthAnchorBoneName(), mask));
  }
}
