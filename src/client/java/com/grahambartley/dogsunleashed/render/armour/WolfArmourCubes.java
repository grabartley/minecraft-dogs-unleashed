package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RestPose;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class WolfArmourCubes {

  public static final int TEXTURE_WIDTH = 64;
  public static final int TEXTURE_HEIGHT = 32;

  static final Set<String> TORSO_PATHS = Set.of("/upper_body", "/body");

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private WolfArmourCubes() {}

  public static List<List<CubeGeometry.Face>> torso(final ModelPart root) {
    final List<List<CubeGeometry.Face>> cubes = new ArrayList<>();
    root.forEachCuboid(
        new MatrixStack(),
        (entry, path, index, cuboid) -> {
          if (TORSO_PATHS.contains(path)) {
            cubes.add(facesOf(path, entry.getPositionMatrix(), entry.getNormalMatrix(), cuboid));
          }
        });
    return cubes;
  }

  private static List<CubeGeometry.Face> facesOf(
      final String path,
      final Matrix4f position,
      final Matrix3f normal,
      final ModelPart.Cuboid cuboid) {
    final List<CubeGeometry.Face> faces = new ArrayList<>();
    for (final ModelPart.Quad quad : cuboid.sides) {
      final Vector3f[] corners = new Vector3f[quad.vertices.length];
      final float[] us = new float[quad.vertices.length];
      final float[] vs = new float[quad.vertices.length];
      for (int i = 0; i < quad.vertices.length; i++) {
        final ModelPart.Vertex vertex = quad.vertices[i];
        corners[i] =
            upright(
                position.transformPosition(
                    new Vector3f(vertex.pos).div(MODEL_UNITS_PER_BLOCK), new Vector3f()));
        us[i] = vertex.u;
        vs[i] = vertex.v;
      }
      faces.add(
          new CubeGeometry.Face(
              path,
              corners,
              us,
              vs,
              upright(normal.transform(new Vector3f(quad.direction))).normalize(),
              RestPose.NONE));
    }
    return faces;
  }

  private static Vector3f upright(final Vector3f modelSpace) {
    return modelSpace.set(-modelSpace.x, -modelSpace.y, modelSpace.z);
  }
}
