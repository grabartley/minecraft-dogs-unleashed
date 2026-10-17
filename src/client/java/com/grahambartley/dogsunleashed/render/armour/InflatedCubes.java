package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.util.RenderUtil;

public final class InflatedCubes {

  private InflatedCubes() {}

  public static void render(
      final MatrixStack poseStack,
      final GeoBone bone,
      final UnleashedDogBreed breed,
      final Vector3f inflation,
      final VertexConsumer buffer,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    for (int index = 0; index < bone.getCubes().size(); index++) {
      if (!ArmourFit.covers(breed, bone.getName(), bone.getCubes().get(index))) {
        continue;
      }
      final GeoCube cube = bone.getCubes().get(index);
      poseStack.push();
      RenderUtil.translateToPivotPoint(poseStack, cube);
      RenderUtil.rotateMatrixAroundCube(poseStack, cube);
      RenderUtil.translateAwayFromPivotPoint(poseStack, cube);
      inflate(poseStack, cube, inflation);
      final Matrix3f normals = poseStack.peek().getNormalMatrix();
      final Matrix4f positions = new Matrix4f(poseStack.peek().getPositionMatrix());
      for (final GeoQuad quad : cube.quads()) {
        if (quad == null) {
          continue;
        }
        final Vector3f normal = normals.transform(new Vector3f(quad.normal())).normalize();
        RenderUtil.fixInvertedFlatCube(cube, normal);
        for (final GeoVertex vertex : quad.vertices()) {
          final Vector3f position = vertex.position();
          final Vector4f transformed =
              positions.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));
          buffer.vertex(
              transformed.x(),
              transformed.y(),
              transformed.z(),
              colour,
              vertex.texU(),
              vertex.texV(),
              packedOverlay,
              packedLight,
              normal.x(),
              normal.y(),
              normal.z());
        }
      }
      poseStack.pop();
    }
  }

  private static void inflate(final MatrixStack poseStack, final GeoCube cube, final Vector3f by) {
    final Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
    final Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
    for (final GeoQuad quad : cube.quads()) {
      if (quad == null) {
        continue;
      }
      for (final GeoVertex vertex : quad.vertices()) {
        min.min(vertex.position());
        max.max(vertex.position());
      }
    }
    final Vector3f size = new Vector3f(max).sub(min);
    final Vector3f centre = new Vector3f(min).add(max).mul(0.5f);
    poseStack.translate(centre.x, centre.y, centre.z);
    poseStack.scale(grow(size.x, by.x), grow(size.y, by.y), grow(size.z, by.z));
    poseStack.translate(-centre.x, -centre.y, -centre.z);
  }

  static float grow(final float size, final float by) {
    return size <= 0.0f ? 1.0f : (size + 2.0f * by) / size;
  }
}
