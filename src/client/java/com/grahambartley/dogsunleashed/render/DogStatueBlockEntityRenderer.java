package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.DogStatue;
import com.grahambartley.dogsunleashed.block.DogStatueBlock;
import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import com.grahambartley.dogsunleashed.render.statue.DogStatueRenderer;
import com.grahambartley.dogsunleashed.render.statue.StatueEngraving;
import com.grahambartley.dogsunleashed.render.statue.StatueFigures;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public final class DogStatueBlockEntityRenderer
    implements BlockEntityRenderer<DogStatueBlockEntity> {

  private static final double TOP_OF_BASE = 0.5;

  private static final float PANEL_CENTRE_Y = 4.0f / 16.0f;
  private static final float PANEL_FRONT = 0.5f - 2.0f / 16.0f + 0.005f;
  private static final float GLYPH_CENTRE = 3.5f;

  private final DogStatueRenderer renderer;
  private final TextRenderer textRenderer;
  private final StatueFigures figures = new StatueFigures();

  public DogStatueBlockEntityRenderer(final BlockEntityRendererFactory.Context context) {
    final MinecraftClient client = MinecraftClient.getInstance();
    this.textRenderer = context.getTextRenderer();
    this.renderer =
        DogStatueRenderer.create(
            new EntityRendererFactory.Context(
                context.getEntityRenderDispatcher(),
                context.getItemRenderer(),
                context.getRenderManager(),
                client.getEntityRenderDispatcher().getHeldItemRenderer(),
                client.getResourceManager(),
                context.getLayerRenderDispatcher(),
                context.getTextRenderer()));
  }

  @Override
  public void render(
      final DogStatueBlockEntity base,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider consumers,
      final int light,
      final int overlay) {
    final DogStatue statue = base.statue();
    final BlockState state = base.getCachedState();
    if (statue == null) {
      this.figures.forget(base);
      return;
    }
    if (base.getWorld() == null || !(state.getBlock() instanceof DogStatueBlock block)) {
      return;
    }
    final Direction facing = state.get(DogStatueBlock.FACING);
    if (!statue.name().isEmpty()) {
      this.engrave(
          statue.name(),
          block.material(),
          facing,
          matrices,
          consumers,
          WorldRenderer.getLightmapCoordinates(base.getWorld(), base.getPos().offset(facing)));
    }
    final StatueFigures.Figure figure = this.figures.of(base, statue, facing);
    if (figure == null) {
      return;
    }
    matrices.push();
    matrices.translate(0.5, TOP_OF_BASE, 0.5);
    this.renderer.renderFigure(
        figure,
        block.material(),
        matrices,
        consumers,
        WorldRenderer.getLightmapCoordinates(base.getWorld(), base.getPos().up()));
    matrices.pop();
  }

  private void engrave(
      final String name,
      final StatueMaterial material,
      final Direction facing,
      final MatrixStack matrices,
      final VertexConsumerProvider consumers,
      final int light) {
    final StatueEngraving.Layout layout = StatueEngraving.layout(name, this.textRenderer::getWidth);
    final float scale = layout.scale();
    matrices.push();
    matrices.translate(0.5, 0.0, 0.5);
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
    matrices.translate(0.0, PANEL_CENTRE_Y, PANEL_FRONT);
    matrices.scale(scale, -scale, scale);
    final float firstLine =
        -GLYPH_CENTRE - (layout.lines().size() - 1) * StatueEngraving.LINE_SPACING / 2.0f;
    for (int i = 0; i < layout.lines().size(); i++) {
      final String line = layout.lines().get(i);
      this.textRenderer.draw(
          line,
          -this.textRenderer.getWidth(line) / 2.0f,
          firstLine + i * StatueEngraving.LINE_SPACING,
          StatueEngraving.colourOf(material),
          false,
          matrices.peek().getPositionMatrix(),
          consumers,
          TextRenderer.TextLayerType.POLYGON_OFFSET,
          0,
          light);
    }
    matrices.pop();
  }

  @Override
  public boolean rendersOutsideBoundingBox(final DogStatueBlockEntity base) {
    return true;
  }
}
