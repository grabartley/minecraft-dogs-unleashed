package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.screen.DogPreviewFraming;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
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
import org.joml.Vector3f;

public final class PortraitBaker {

  private static final float FACING_YAW = 215.0f;
  private static final float NEAR_PLANE = 1000.0f;
  private static final float FAR_PLANE = 21000.0f;
  private static final float GUI_DEPTH = -11000.0f;
  private static final int BUFFER_BYTES = 1 << 18;
  private static final float GUI_Z = 50.0f;
  private static final int SOURCE_SIZE = 256;

  private PortraitBaker() {}

  public static @Nullable NativeImage bake(final DogStatueLook look) {
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
    try (BufferAllocator buffer = new BufferAllocator(BUFFER_BYTES)) {
      target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
      target.clear(MinecraftClient.IS_SYSTEM_MAC);
      target.beginWrite(true);
      RenderSystem.setProjectionMatrix(
          new Matrix4f().setOrtho(0.0f, SOURCE_SIZE, SOURCE_SIZE, 0.0f, NEAR_PLANE, FAR_PLANE),
          VertexSorter.BY_Z);
      modelView.pushMatrix();
      modelView.translation(0.0f, 0.0f, GUI_DEPTH);
      RenderSystem.applyModelViewMatrix();
      final DrawContext context = new DrawContext(client, VertexConsumerProvider.immediate(buffer));
      draw(context, dog);
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

  private static void draw(final DrawContext context, final UnleashedDogEntity dog) {
    final float portraitHeight = dog.getRigSourceBreed().renderTransforms().portraitHeight();
    final int box = SOURCE_SIZE / 2;
    final float scale = dog.getScale();
    final Vector3f pivot =
        new Vector3f(
            0.0f,
            dog.getHeight() / 2.0f
                + DogPreviewFraming.pivotOffset(portraitHeight, dog.getHeight(), 1.0f) * scale,
            0.0f);
    final MatrixStack matrices = context.getMatrices();
    matrices.push();
    matrices.translate(SOURCE_SIZE / 2.0f, SOURCE_SIZE / 2.0f, GUI_Z);
    final float size = DogPreviewFraming.renderSize(box, portraitHeight) / scale;
    matrices.scale(size, size, -size);
    matrices.translate(pivot.x, pivot.y, pivot.z);
    matrices.multiply(new Quaternionf().rotateZ((float) Math.PI));
    DiffuseLighting.method_34742();
    PortraitDogRenderer.create(MinecraftClient.getInstance())
        .renderSitter(
            dog,
            matrices,
            context.getVertexConsumers(),
            LightmapTextureManager.MAX_LIGHT_COORDINATE);
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
