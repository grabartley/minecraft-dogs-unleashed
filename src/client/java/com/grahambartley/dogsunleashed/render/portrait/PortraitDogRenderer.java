package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.block.DogStatuePose;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.DogRenderer;
import com.grahambartley.dogsunleashed.render.PosedDogModel;
import com.grahambartley.dogsunleashed.render.statue.StatuePoseFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public final class PortraitDogRenderer extends DogRenderer {

  private final PosedDogModel posedModel;

  private PortraitDogRenderer(
      final EntityRendererFactory.Context context, final PosedDogModel posedModel) {
    super(context, posedModel);
    this.posedModel = posedModel;
    this.shadowRadius = 0.0f;
    this.addDogLayers(context);
  }

  public static PortraitDogRenderer create(final MinecraftClient client) {
    return new PortraitDogRenderer(
        new EntityRendererFactory.Context(
            client.getEntityRenderDispatcher(),
            client.getItemRenderer(),
            client.getBlockRenderManager(),
            client.getEntityRenderDispatcher().getHeldItemRenderer(),
            client.getResourceManager(),
            client.getEntityModelLoader(),
            client.textRenderer),
        new PosedDogModel());
  }

  public void renderPosed(
      final UnleashedDogEntity dog,
      final DogStatuePose pose,
      final MatrixStack matrices,
      final VertexConsumerProvider consumers,
      final int light) {
    this.posedModel.frame(StatuePoseFrame.tickOf(pose), true);
    this.render(dog, dog.getYaw(), 0.0f, matrices, consumers, light);
  }
}
