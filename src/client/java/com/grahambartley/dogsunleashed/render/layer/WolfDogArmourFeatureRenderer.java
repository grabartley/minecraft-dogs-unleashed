package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.WolfDogArmour;
import com.grahambartley.dogsunleashed.render.armour.ArmourLook;
import com.grahambartley.dogsunleashed.render.armour.ArmourPainter;
import com.grahambartley.dogsunleashed.render.armour.WolfArmourCubes;
import com.grahambartley.dogsunleashed.render.armour.WolfArmourTextures;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class WolfDogArmourFeatureRenderer
    extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private final WolfEntityModel<WolfEntity> model;
  private final WolfArmourTextures textures;

  public WolfDogArmourFeatureRenderer(
      final FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context,
      final EntityModelLoader loader) {
    super(context);
    final ModelPart root = loader.getModelPart(EntityModelLayers.WOLF_ARMOR);
    this.textures =
        new WolfArmourTextures(
            WolfArmourCubes.torso(root),
            WolfArmourCubes.TEXTURE_WIDTH,
            WolfArmourCubes.TEXTURE_HEIGHT);
    this.model = new WolfEntityModel<>(root);
  }

  @Override
  public void render(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final WolfEntity wolf,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch) {
    if (!WolfDogArmour.wears(wolf) || wolf.isInvisible()) {
      return;
    }
    final ItemStack worn = wolf.getBodyArmor();
    final ArmourLook look = ArmourLook.of(worn);
    this.getContextModel().copyStateTo(this.model);
    this.model.animateModel(wolf, limbAngle, limbDistance, tickDelta);
    this.model.setAngles(wolf, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
    this.draw(
        matrices,
        vertexConsumers,
        light,
        this.textures.layer(look, ArmourPainter.Layer.BASE),
        OPAQUE_WHITE,
        false);
    if (look.fillIsDyed()) {
      this.draw(
          matrices,
          vertexConsumers,
          light,
          this.textures.layer(look, ArmourPainter.Layer.DYE),
          DyedColorComponent.getColor(worn, ArmourLook.UNDYED_LEATHER),
          false);
    }
    final int level = ArmourLook.crackLevel(worn);
    if (level > 0) {
      this.draw(matrices, vertexConsumers, light, this.textures.cracks(level), OPAQUE_WHITE, true);
    }
  }

  private void draw(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final Identifier texture,
      final int colour,
      final boolean translucent) {
    this.model.render(
        matrices,
        vertexConsumers.getBuffer(
            translucent
                ? RenderLayer.getEntityTranslucent(texture)
                : RenderLayer.getEntityCutoutNoCull(texture)),
        light,
        OverlayTexture.DEFAULT_UV,
        colour);
  }
}
