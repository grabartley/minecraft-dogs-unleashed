package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.DogRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public final class DogStatueRenderer extends DogRenderer {

  private final StatueDogModel statueModel;

  private DogStatueRenderer(
      final EntityRendererFactory.Context context, final StatueDogModel statueModel) {
    super(context, statueModel);
    this.statueModel = statueModel;
    this.shadowRadius = 0.0f;
  }

  public static DogStatueRenderer create(final EntityRendererFactory.Context context) {
    return new DogStatueRenderer(context, new StatueDogModel());
  }

  public void renderFigure(
      final StatueFigures.Figure figure,
      final StatueMaterial material,
      final MatrixStack matrices,
      final VertexConsumerProvider consumers,
      final int light) {
    this.statueModel.frame(
        material, StatuePoseFrame.tickOf(figure.statue().shownPose()), !figure.started());
    final UnleashedDogEntity dog = figure.dog();
    this.render(dog, dog.getYaw(), 0.0f, matrices, consumers, light);
    figure.markStarted();
  }
}
