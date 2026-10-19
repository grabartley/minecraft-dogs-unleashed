package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import java.util.stream.Stream;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.render.entity.model.PiglinHeadEntityModel;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MobHeadShapeTest {

  private static final float SKULL_EDGE = 0.5f;
  private static final float EPSILON = 1.0e-3f;

  private static final class Recorder implements VertexConsumer {
    private final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();

    @Override
    public VertexConsumer vertex(final float x, final float y, final float z) {
      this.bounds.include(new Vector3f(x, y, z));
      return this;
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
      return this;
    }

    @Override
    public VertexConsumer texture(final float u, final float v) {
      return this;
    }

    @Override
    public VertexConsumer overlay(final int u, final int v) {
      return this;
    }

    @Override
    public VertexConsumer light(final int u, final int v) {
      return this;
    }

    @Override
    public VertexConsumer normal(final float x, final float y, final float z) {
      return this;
    }
  }

  private static CubeGeometry.Bounds drawnBy(
      final SkullBlockEntityModel model, final float progress) {
    final Recorder recorder = new Recorder();
    final MatrixStack matrices = new MatrixStack();
    matrices.scale(1.0f / SKULL_EDGE, 1.0f / SKULL_EDGE, 1.0f / SKULL_EDGE);
    matrices.translate(-0.5f, 0.0f, -0.5f);
    SkullBlockEntityRenderer.renderSkull(
        null, 0.0f, progress, matrices, layer -> recorder, 0, model, null);
    return recorder.bounds;
  }

  private static SkullBlockEntityModel skull() {
    return new SkullEntityModel(SkullEntityModel.getSkullTexturedModelData().createModel());
  }

  private static SkullBlockEntityModel headWithHat() {
    return new SkullEntityModel(SkullEntityModel.getHeadTexturedModelData().createModel());
  }

  static Stream<Arguments> vanillaModels() {
    return Stream.of(
        Arguments.of(MobHead.SKELETON, skull(), 0.0f),
        Arguments.of(MobHead.WITHER_SKELETON, skull(), 0.0f),
        Arguments.of(MobHead.CREEPER, skull(), 0.0f),
        Arguments.of(MobHead.ZOMBIE, headWithHat(), 0.0f),
        Arguments.of(
            MobHead.PIGLIN,
            new PiglinHeadEntityModel(
                PiglinHeadEntityModel.getModelData().getRoot().createPart(64, 64)),
            0.0f),
        Arguments.of(
            MobHead.PIGLIN,
            new PiglinHeadEntityModel(
                PiglinHeadEntityModel.getModelData().getRoot().createPart(64, 64)),
            2.5f),
        Arguments.of(
            MobHead.PIGLIN,
            new PiglinHeadEntityModel(
                PiglinHeadEntityModel.getModelData().getRoot().createPart(64, 64)),
            5.0f),
        Arguments.of(
            MobHead.DRAGON,
            new DragonHeadEntityModel(DragonHeadEntityModel.getTexturedModelData().createModel()),
            7.5f),
        Arguments.of(
            MobHead.DRAGON,
            new DragonHeadEntityModel(DragonHeadEntityModel.getTexturedModelData().createModel()),
            12.5f));
  }

  @ParameterizedTest(name = "{0} at {2}")
  @MethodSource("vanillaModels")
  @DisplayName("each footprint holds what vanilla draws for that head")
  void footprintHoldsTheVanillaModel(
      final MobHead head, final SkullBlockEntityModel model, final float progress) {
    final CubeGeometry.Bounds drawn = drawnBy(model, progress);
    final CubeGeometry.Bounds footprint = MobHeadShape.footprint(head);

    assertTrue(
        within(footprint, drawn.min()) && within(footprint, drawn.max()),
        head + " draws " + drawn.min() + " to " + drawn.max());
  }

  static Stream<Arguments> vanillaHeads() {
    return Stream.of(
        Arguments.of(MobHead.SKELETON, skull()),
        Arguments.of(MobHead.WITHER_SKELETON, skull()),
        Arguments.of(MobHead.CREEPER, skull()),
        Arguments.of(MobHead.ZOMBIE, headWithHat()),
        Arguments.of(
            MobHead.PIGLIN,
            new PiglinHeadEntityModel(
                PiglinHeadEntityModel.getModelData().getRoot().createPart(64, 64))),
        Arguments.of(
            MobHead.DRAGON,
            new DragonHeadEntityModel(DragonHeadEntityModel.getTexturedModelData().createModel())));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("vanillaHeads")
  @DisplayName("each footprint is the box vanilla's head sweeps through a whole motion cycle")
  void footprintIsTight(final MobHead head, final SkullBlockEntityModel model) {
    final CubeGeometry.Bounds swept = new CubeGeometry.Bounds();
    for (float progress = 0.0f; progress <= 20.0f; progress += 0.25f) {
      final CubeGeometry.Bounds drawn = drawnBy(model, progress);
      swept.include(drawn.min());
      swept.include(drawn.max());
    }
    final CubeGeometry.Bounds footprint = MobHeadShape.footprint(head);

    assertTrue(footprint.min().distance(swept.min()) < 0.04f, head + " min " + swept.min());
    assertTrue(footprint.max().distance(swept.max()) < 0.04f, head + " max " + swept.max());
  }

  private static boolean within(final CubeGeometry.Bounds box, final Vector3f point) {
    return point.x >= box.min().x - EPSILON
        && point.x <= box.max().x + EPSILON
        && point.y >= box.min().y - EPSILON
        && point.y <= box.max().y + EPSILON
        && point.z >= box.min().z - EPSILON
        && point.z <= box.max().z + EPSILON;
  }

  @Test
  @DisplayName("a plain skull fills exactly one unit cube standing on its base")
  void skullIsTheUnitCube() {
    final CubeGeometry.Bounds drawn = drawnBy(skull(), 0.0f);

    assertEquals(new Vector3f(-0.5f, 0.0f, -0.5f), drawn.min());
    assertEquals(new Vector3f(0.5f, 1.0f, 0.5f), drawn.max());
    assertEquals(MobHeadShape.footprint(MobHead.CREEPER).min(), drawn.min());
    assertEquals(MobHeadShape.footprint(MobHead.CREEPER).max(), drawn.max());
  }

  @Test
  @DisplayName("placing scales the footprint by the edge and stretches only its depth")
  void placedScalesAndStretches() {
    final CubeGeometry.Bounds placed =
        MobHeadShape.placed(MobHead.CREEPER, new Vector3f(1, 2, 3), 2.0f, 1.5f);

    assertEquals(new Vector3f(0, 2, 1.5f), placed.min());
    assertEquals(new Vector3f(2, 4, 4.5f), placed.max());
  }
}
