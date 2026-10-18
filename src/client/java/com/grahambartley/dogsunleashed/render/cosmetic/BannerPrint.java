package com.grahambartley.dogsunleashed.render.cosmetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public final class BannerPrint {

  static final float BANNER_UNITS = 64.0f;
  static final float FRONT_X = 1.0f;
  static final float FRONT_Y = 1.0f;
  static final float FRONT_WIDTH = 20.0f;
  static final float FRONT_HEIGHT = 40.0f;
  static final float FACING_Z = 0.9f;

  public interface Image {
    int width();

    int height();

    int get(int x, int y);
  }

  public record Face(int x, int y, int width, int height, boolean flipU, boolean flipV) {}

  private BannerPrint() {}

  public static int[] print(final Image cloth, final Image pattern, final List<Face> faces) {
    final int[] out = new int[cloth.width() * cloth.height()];
    for (final Face face : faces) {
      for (int dy = 0; dy < face.height(); dy++) {
        for (int dx = 0; dx < face.width(); dx++) {
          final int x = face.x() + dx;
          final int y = face.y() + dy;
          if (x < 0 || y < 0 || x >= cloth.width() || y >= cloth.height()) {
            continue;
          }
          if (cloth.get(x, y) >>> 24 == 0) {
            continue;
          }
          final int u = face.flipU() ? face.width() - 1 - dx : dx;
          final int v = face.flipV() ? face.height() - 1 - dy : dy;
          out[y * cloth.width() + x] =
              pattern.get(
                  sourceX(u, face.width(), pattern.width()),
                  sourceY(v, face.height(), pattern.height()));
        }
      }
    }
    return out;
  }

  static int sourceX(final int u, final int faceWidth, final int patternWidth) {
    final float banner = FRONT_X + (u + 0.5f) / faceWidth * FRONT_WIDTH;
    return Math.min(patternWidth - 1, (int) (banner * patternWidth / BANNER_UNITS));
  }

  static int sourceY(final int v, final int faceHeight, final int patternHeight) {
    final float banner = FRONT_Y + (v + 0.5f) / faceHeight * FRONT_HEIGHT;
    return Math.min(patternHeight - 1, (int) (banner * patternHeight / BANNER_UNITS));
  }

  public static List<Face> facesOf(final BakedGeoModel model, final int width, final int height) {
    final List<Face> candidates = new ArrayList<>();
    for (final GeoBone bone : model.topLevelBones()) {
      collect(bone, width, height, candidates);
    }
    final int tallest = candidates.stream().mapToInt(Face::height).max().orElse(0);
    return candidates.stream().filter(face -> face.height() * 2 > tallest).toList();
  }

  private static void collect(
      final GeoBone bone, final int width, final int height, final List<Face> out) {
    for (final GeoCube cube : bone.getCubes()) {
      for (final GeoQuad quad : cube.quads()) {
        if (quad != null && Math.abs(quad.normal().z()) > FACING_Z) {
          faceOf(quad, width, height).ifPresent(out::add);
        }
      }
    }
    for (final GeoBone child : bone.getChildBones()) {
      collect(child, width, height, out);
    }
  }

  private static Optional<Face> faceOf(final GeoQuad quad, final int width, final int height) {
    final GeoVertex[] vertices = quad.vertices();
    float minU = Float.MAX_VALUE;
    float minV = Float.MAX_VALUE;
    float maxU = -Float.MAX_VALUE;
    float maxV = -Float.MAX_VALUE;
    for (final GeoVertex vertex : vertices) {
      minU = Math.min(minU, vertex.texU());
      maxU = Math.max(maxU, vertex.texU());
      minV = Math.min(minV, vertex.texV());
      maxV = Math.max(maxV, vertex.texV());
    }
    final int x = Math.round(minU * width);
    final int y = Math.round(minV * height);
    final int w = Math.round(maxU * width) - x;
    final int h = Math.round(maxV * height) - y;
    if (w <= 0 || h <= 0) {
      return Optional.empty();
    }
    final float facing = Math.signum(quad.normal().z());
    return Optional.of(
        new Face(x, y, w, h, slope(vertices, true) * facing < 0.0f, slope(vertices, false) > 0.0f));
  }

  private static float slope(final GeoVertex[] vertices, final boolean horizontal) {
    for (int i = 1; i < vertices.length; i++) {
      final float along =
          horizontal
              ? vertices[i].position().x() - vertices[0].position().x()
              : vertices[i].position().y() - vertices[0].position().y();
      final float across =
          horizontal
              ? vertices[i].texU() - vertices[0].texU()
              : vertices[i].texV() - vertices[0].texV();
      if (Math.abs(along) > 1.0e-6f && Math.abs(across) > 1.0e-6f) {
        return across / along;
      }
    }
    return 0.0f;
  }
}
