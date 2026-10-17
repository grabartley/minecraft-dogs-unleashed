package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.armour.ArmourClearance;
import com.grahambartley.dogsunleashed.render.armour.ArmourFit;
import com.grahambartley.dogsunleashed.render.armour.ArmourLook;
import com.grahambartley.dogsunleashed.render.armour.ArmourPainter;
import com.grahambartley.dogsunleashed.render.armour.ArmourTextures;
import com.grahambartley.dogsunleashed.render.armour.InflatedCubes;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogArmourLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private record Frame(
      UnleashedDogBreed breed,
      Vector3f inflation,
      Identifier base,
      @Nullable Identifier dye,
      int dyeColour,
      @Nullable Identifier cracks) {}

  private @Nullable Frame frame;

  public DogArmourLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    final ItemStack worn = animatable.getArmour().worn();
    this.frame =
        worn.isEmpty() || animatable.isInvisible() ? null : frameFor(bakedModel, animatable, worn);
  }

  private static Frame frameFor(
      final BakedGeoModel model, final UnleashedDogEntity dog, final ItemStack worn) {
    final UnleashedDogBreed breed = dog.getRigSourceBreed();
    final ArmourLook look = ArmourLook.of(worn);
    final boolean trimDyed = ArmourLook.trimIsDyed(look, worn);
    final boolean hasDyeLayer = look.fillIsDyed() || trimDyed;
    final int level = ArmourTextures.crackLevel(worn);
    return new Frame(
        breed,
        ArmourClearance.inflationFor(model, breed),
        ArmourTextures.layer(model, breed, look, ArmourPainter.Layer.BASE, trimDyed),
        hasDyeLayer
            ? ArmourTextures.layer(model, breed, look, ArmourPainter.Layer.DYE, trimDyed)
            : null,
        DyedColorComponent.getColor(worn, ArmourLook.UNDYED_LEATHER),
        level == 0 ? null : ArmourTextures.cracks(model, breed, level));
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
    final Frame current = this.frame;
    if (current == null
        || bone.isHidden()
        || !ArmourFit.torsoBones(current.breed()).contains(bone.getName())) {
      return;
    }
    draw(
        poseStack,
        bone,
        current.breed(),
        current.inflation(),
        bufferSource,
        RenderLayer.getEntityCutoutNoCull(current.base()),
        packedLight,
        OPAQUE_WHITE);
    if (current.dye() != null) {
      draw(
          poseStack,
          bone,
          current.breed(),
          current.inflation(),
          bufferSource,
          RenderLayer.getEntityCutoutNoCull(current.dye()),
          packedLight,
          current.dyeColour());
    }
    if (current.cracks() != null) {
      draw(
          poseStack,
          bone,
          current.breed(),
          current.inflation(),
          bufferSource,
          RenderLayer.getEntityTranslucent(current.cracks()),
          packedLight,
          OPAQUE_WHITE);
    }
  }

  private static void draw(
      final MatrixStack poseStack,
      final GeoBone bone,
      final UnleashedDogBreed breed,
      final Vector3f inflation,
      final VertexConsumerProvider bufferSource,
      final RenderLayer layer,
      final int packedLight,
      final int colour) {
    InflatedCubes.render(
        poseStack,
        bone,
        breed,
        inflation,
        bufferSource.getBuffer(layer),
        packedLight,
        OverlayTexture.DEFAULT_UV,
        colour);
  }
}
