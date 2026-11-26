package com.grahambartley.dogsunleashed.render.bend;

import java.util.List;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class BentCuboids {

  private static final float PIXEL = 1.0f / 16.0f;

  private BentCuboids() {}

  public static void render(
      final MatrixStack.Entry entry,
      final List<ModelPart.Cuboid> cuboids,
      final VertexConsumer vertices,
      final int light,
      final int overlay,
      final int color,
      final float joint,
      final float angle) {
    final Matrix4f position = entry.getPositionMatrix();
    final Vector3f placed = new Vector3f();
    for (final ModelPart.Cuboid cuboid : cuboids) {
      for (final ModelPart.Quad quad : cuboid.sides) {
        for (final LimbBend.Corner[] piece : LimbBend.split(cornersOf(quad), joint)) {
          final Vector3f normal =
              entry.transformNormal(
                  LimbBend.bendNormal(quad.direction, LimbBend.isLower(piece, joint), angle),
                  new Vector3f());
          for (final LimbBend.Corner corner : piece) {
            final Vector3f bent = LimbBend.bend(corner.pos(), joint, angle);
            position.transformPosition(bent.x * PIXEL, bent.y * PIXEL, bent.z * PIXEL, placed);
            vertices.vertex(
                placed.x,
                placed.y,
                placed.z,
                color,
                corner.u(),
                corner.v(),
                overlay,
                light,
                normal.x,
                normal.y,
                normal.z);
          }
        }
      }
    }
  }

  private static LimbBend.Corner[] cornersOf(final ModelPart.Quad quad) {
    final LimbBend.Corner[] corners = new LimbBend.Corner[quad.vertices.length];
    for (int i = 0; i < corners.length; i++) {
      final ModelPart.Vertex vertex = quad.vertices[i];
      corners[i] = new LimbBend.Corner(vertex.pos, vertex.u, vertex.v);
    }
    return corners;
  }
}
