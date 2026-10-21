package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public final class PortraitBaker {

  private static final float FACING_YAW = 215.0f;
  private static final float NEAR_PLANE = 1000.0f;
  private static final float FAR_PLANE = 21000.0f;
  private static final float GUI_DEPTH = -11000.0f;
  private static final int BUFFER_BYTES = 1 << 18;
  private static final float GUI_Z = 50.0f;
  private static final int SOURCE_SIZE = 256;
  private static final float MODEL_FILL = 0.45f;
  private static final float FEET_HEIGHT = 0.75f;

  private @Nullable PortraitDogRenderer renderer;

  public void forgetRenderer() {
    this.renderer = null;
  }

  public @Nullable NativeImage bake(final DogStatueLook look) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final UnleashedDogEntity dog = sitterOf(client, look);
    if (dog == null) {
      return null;
    }
    final SimpleFramebuffer target =
        new SimpleFramebuffer(SOURCE_SIZE, SOURCE_SIZE, true, MinecraftClient.IS_SYSTEM_MAC);
    final Matrix4f projection = RenderSystem.getProjectionMatrix();
    final VertexSorter sorting = RenderSystem.getVertexSorting();
    final Matrix4fStack modelView = RenderSystem.getModelViewStack();
    modelView.pushMatrix();
    try (BufferAllocator buffer = new BufferAllocator(BUFFER_BYTES)) {
      target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
      target.clear(MinecraftClient.IS_SYSTEM_MAC);
      target.beginWrite(true);
      RenderSystem.setProjectionMatrix(
          new Matrix4f().setOrtho(0.0f, SOURCE_SIZE, SOURCE_SIZE, 0.0f, NEAR_PLANE, FAR_PLANE),
          VertexSorter.BY_Z);
      modelView.translation(0.0f, 0.0f, GUI_DEPTH);
      RenderSystem.applyModelViewMatrix();
      BackgroundRenderer.clearFog();
      final DrawContext context = new DrawContext(client, VertexConsumerProvider.immediate(buffer));
      this.draw(client, context, dog);
      context.draw();
      return read(target);
    } finally {
      modelView.popMatrix();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.setProjectionMatrix(projection, sorting);
      target.delete();
      client.getFramebuffer().beginWrite(true);
      dog.discard();
    }
  }

  private static @Nullable UnleashedDogEntity sitterOf(
      final MinecraftClient client, final DogStatueLook look) {
    final EntityType<UnleashedDogEntity> type = ModEntities.getDogEntityType(look.breed());
    final UnleashedDogEntity dog =
        client.world == null || type == null ? null : type.create(client.world);
    if (dog == null) {
      return null;
    }
    look.applyTo(dog);
    dog.setSitting(true);
    dog.setInSittingPose(true);
    dog.setYaw(FACING_YAW);
    dog.setBodyYaw(FACING_YAW);
    dog.setHeadYaw(FACING_YAW);
    dog.prevYaw = FACING_YAW;
    dog.prevBodyYaw = FACING_YAW;
    dog.prevHeadYaw = FACING_YAW;
    return dog;
  }

  private void draw(
      final MinecraftClient client, final DrawContext context, final UnleashedDogEntity dog) {
    if (this.renderer == null) {
      this.renderer = PortraitDogRenderer.create(client);
    }
    final float size =
        SOURCE_SIZE
            * MODEL_FILL
            / dog.getRigSourceBreed().renderTransforms().portraitHeight()
            / dog.getScale();
    final MatrixStack matrices = context.getMatrices();
    matrices.push();
    matrices.translate(SOURCE_SIZE / 2.0f, SOURCE_SIZE * FEET_HEIGHT, GUI_Z);
    matrices.scale(size, size, -size);
    matrices.multiply(new Quaternionf().rotateZ((float) Math.PI));
    DiffuseLighting.method_34742();
    this.renderer.renderSitter(
        dog, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
    context.draw();
    DiffuseLighting.enableGuiDepthLighting();
    matrices.pop();
  }

  private static NativeImage read(final SimpleFramebuffer target) {
    final NativeImage image = new NativeImage(SOURCE_SIZE, SOURCE_SIZE, false);
    RenderSystem.bindTexture(target.getColorAttachment());
    image.loadFromTextureImage(0, false);
    image.mirrorVertically();
    return image;
  }
}
