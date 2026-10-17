package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.render.armour.ArmourLook.Palette;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.QuadUv;
import java.util.List;
import org.joml.Vector3f;

public final class ArmourPainter {

  public enum Layer {
    BASE,
    DYE
  }

  static final int TRANSPARENT = 0;
  static final int CRACK = 0xBF000000;
  static final int CRACK_EDGE = 0x46A4A4A4;
  static final int DYED_TRIM = 0xFFD8D8D8;

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;
  private static final float FACING = 0.6f;
  private static final float OCCLUSION_PROBE = 0.5f / MODEL_UNITS_PER_BLOCK;
  private static final float EDGE_REACH = 1.0f;
  private static final float SHARED_SURFACE = 0.01f / MODEL_UNITS_PER_BLOCK;
  private static final int PLATE_LENGTH = 4;
  private static final int LAME_HEIGHT = 3;
  private static final int SCUTE_LENGTH = 5;
  private static final int SCUTE_HEIGHT = 3;
  private static final int RIVET_SPACING = 3;
  private static final int SPECK_PERCENT = 7;
  private static final int LEATHER_MOTTLE_PERCENT = 18;
  private static final int CRACK_AREA_PER_WALK = 90;

  enum Side {
    TOP,
    BELLY,
    FLANK,
    END
  }

  record Grid(int across, int acrossCount, int up, int upCount) {}

  record Texel(
      Side side, Grid grid, boolean hemFace, boolean openStart, boolean openEnd, int hash) {

    boolean hem() {
      return this.hemFace && this.grid.up() == 0;
    }

    boolean rivetRow() {
      return this.hemFace && this.grid.up() == 1;
    }

    boolean opening() {
      return (this.openStart && this.grid.across() == 0)
          || (this.openEnd && this.grid.across() == this.grid.acrossCount() - 1);
    }

    boolean spine() {
      final int centre = this.grid.upCount() / 2;
      return this.grid.up() == centre
          || (this.grid.upCount() % 2 == 0 && this.grid.up() == centre - 1);
    }
  }

  private record Paint(Layer layer, int colour) {
    static final Paint NONE = new Paint(Layer.BASE, TRANSPARENT);
  }

  @FunctionalInterface
  private interface TexelVisitor {
    void visit(int x, int y, Texel texel);
  }

  private record Rect(int x0, int y0, int x1, int y1) {
    int width() {
      return this.x1 - this.x0;
    }

    int height() {
      return this.y1 - this.y0;
    }
  }

  private ArmourPainter() {}

  public static int[] paint(
      final List<List<CubeGeometry.Face>> cubes,
      final int width,
      final int height,
      final ArmourLook look,
      final Layer layer,
      final boolean trimDyed) {
    final int[] pixels = new int[width * height];
    forEachVisibleTexel(
        cubes,
        width,
        height,
        (x, y, texel) -> {
          final Paint paint = paintOf(look, trimDyed, texel);
          if (paint.layer() == layer && paint.colour() != TRANSPARENT) {
            pixels[y * width + x] = paint.colour();
          }
        });
    return pixels;
  }

  public static int[] cracks(
      final List<List<CubeGeometry.Face>> cubes,
      final int width,
      final int height,
      final int level) {
    final int[] pixels = new int[width * height];
    if (level <= 0) {
      return pixels;
    }
    final boolean[] panel = new boolean[width * height];
    forEachVisibleTexel(
        cubes, width, height, (x, y, texel) -> panel[y * width + x] = texel.side() != Side.BELLY);
    for (int start = 0; start < panel.length; start++) {
      if (!panel[start]
          || Math.floorMod(hash(start % width, start / width, 0), CRACK_AREA_PER_WALK) >= level) {
        continue;
      }
      int x = start % width;
      int y = start / width;
      for (int step = 0; step < 2 + level * 2; step++) {
        if (x < 0 || y < 0 || x >= width || y >= height || !panel[y * width + x]) {
          break;
        }
        pixels[y * width + x] = CRACK;
        if (y + 1 < height && panel[(y + 1) * width + x] && pixels[(y + 1) * width + x] == 0) {
          pixels[(y + 1) * width + x] = CRACK_EDGE;
        }
        x += 1;
        y += Math.floorMod(hash(start, step, 0), 3) - 1;
      }
    }
    return pixels;
  }

  private static void forEachVisibleTexel(
      final List<List<CubeGeometry.Face>> cubes,
      final int width,
      final int height,
      final TexelVisitor visitor) {
    final List<CubeGeometry.Bounds> boxes = cubes.stream().map(ArmourPainter::posedBox).toList();
    final CubeGeometry.Bounds armour = new CubeGeometry.Bounds();
    boxes.forEach(
        box -> {
          armour.include(box.min());
          armour.include(box.max());
        });
    for (int cube = 0; cube < cubes.size(); cube++) {
      for (final CubeGeometry.Face face : cubes.get(cube)) {
        if (!occluded(face, boxes, cube)) {
          visitFace(face, armour, boxes, cube, width, height, visitor);
        }
      }
    }
  }

  private static void visitFace(
      final CubeGeometry.Face face,
      final CubeGeometry.Bounds armour,
      final List<CubeGeometry.Bounds> boxes,
      final int own,
      final int width,
      final int height,
      final TexelVisitor visitor) {
    final Rect rect = rectOf(face, width, height);
    if (rect.width() <= 0 || rect.height() <= 0) {
      return;
    }
    final Vector3f origin = posedAt(face, rect.x0(), rect.y0(), width, height);
    final Vector3f alongU = posedAt(face, rect.x1() - 1, rect.y0(), width, height).sub(origin);
    final Vector3f alongV = posedAt(face, rect.x0(), rect.y1() - 1, width, height).sub(origin);
    final Side side = sideOf(face.posedNormal());
    final CubeGeometry.Bounds faceBox = new CubeGeometry.Bounds();
    for (final Vector3f corner : face.corners()) {
      faceBox.include(face.pose().point(corner));
    }
    final float reach = EDGE_REACH / MODEL_UNITS_PER_BLOCK;
    final boolean hemFace =
        side != Side.TOP && side != Side.BELLY && faceBox.min().y - armour.min().y < reach;
    final boolean openStart = faceBox.min().z - armour.min().z < reach;
    final boolean openEnd = armour.max().z - faceBox.max().z < reach;
    for (int y = rect.y0(); y < rect.y1(); y++) {
      for (int x = rect.x0(); x < rect.x1(); x++) {
        if (sharedWithEarlierCube(posedAt(face, x, y, width, height), boxes, own)) {
          continue;
        }
        visitor.visit(
            x,
            y,
            new Texel(
                side,
                gridOf(side, x - rect.x0(), y - rect.y0(), rect, alongU, alongV),
                hemFace,
                openStart,
                openEnd,
                hash(x, y, 0)));
      }
    }
  }

  private static Grid gridOf(
      final Side side,
      final int fx,
      final int fy,
      final Rect rect,
      final Vector3f alongU,
      final Vector3f alongV) {
    final boolean vIsUp =
        side == Side.TOP || side == Side.BELLY
            ? Math.abs(alongV.x) >= Math.abs(alongU.x)
            : Math.abs(alongV.y) >= Math.abs(alongU.y);
    final Vector3f upAxis = vIsUp ? alongV : alongU;
    final Vector3f acrossAxis = vIsUp ? alongU : alongV;
    final int upCount = vIsUp ? rect.height() : rect.width();
    final int acrossCount = vIsUp ? rect.width() : rect.height();
    final int upIndex = vIsUp ? fy : fx;
    final int acrossIndex = vIsUp ? fx : fy;
    final float upSign = side == Side.TOP || side == Side.BELLY ? upAxis.x : upAxis.y;
    final float acrossSign = side == Side.END ? acrossAxis.x : acrossAxis.z;
    return new Grid(
        acrossSign >= 0 ? acrossIndex : acrossCount - 1 - acrossIndex,
        acrossCount,
        upSign >= 0 ? upIndex : upCount - 1 - upIndex,
        upCount);
  }

  private static Rect rectOf(final CubeGeometry.Face face, final int width, final int height) {
    float minU = Float.POSITIVE_INFINITY;
    float maxU = Float.NEGATIVE_INFINITY;
    float minV = Float.POSITIVE_INFINITY;
    float maxV = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < face.us().length; i++) {
      minU = Math.min(minU, face.us()[i]);
      maxU = Math.max(maxU, face.us()[i]);
      minV = Math.min(minV, face.vs()[i]);
      maxV = Math.max(maxV, face.vs()[i]);
    }
    return new Rect(
        Math.max(0, Math.round(minU * width)),
        Math.max(0, Math.round(minV * height)),
        Math.min(width, Math.round(maxU * width)),
        Math.min(height, Math.round(maxV * height)));
  }

  private static Vector3f posedAt(
      final CubeGeometry.Face face, final int x, final int y, final int width, final int height) {
    return QuadUv.pointAt(face, (x + 0.5f) / width, (y + 0.5f) / height)
        .map(face.pose()::point)
        .orElseGet(Vector3f::new);
  }

  static Side sideOf(final Vector3f normal) {
    if (normal.y > FACING) {
      return Side.TOP;
    }
    if (normal.y < -FACING) {
      return Side.BELLY;
    }
    return Math.abs(normal.z) > FACING ? Side.END : Side.FLANK;
  }

  private static Paint paintOf(final ArmourLook look, final boolean trimDyed, final Texel texel) {
    if (texel.side() == Side.BELLY) {
      return strap(texel);
    }
    return switch (look.pattern()) {
      case PLATES -> plates(look.palette(), texel);
      case STITCHED -> stitched(look.palette(), texel);
      case SCUTES -> scutes(look.palette(), trimDyed, texel);
    };
  }

  private static Paint strap(final Texel texel) {
    final int middle = texel.grid().acrossCount() / 2;
    final int across = texel.grid().across();
    if (across != middle && across != middle - 1) {
      return Paint.NONE;
    }
    final int upMiddle = texel.grid().upCount() / 2;
    if (texel.grid().up() == upMiddle || texel.grid().up() == upMiddle - 1) {
      return new Paint(Layer.BASE, ArmourLook.BUCKLE);
    }
    return new Paint(Layer.BASE, across == middle ? ArmourLook.STRAP_LIGHT : ArmourLook.STRAP);
  }

  private static Paint plates(final Palette palette, final Texel texel) {
    if (texel.side() == Side.TOP) {
      if (texel.spine()) {
        return new Paint(Layer.BASE, palette.highlight());
      }
      if (texel.grid().across() % PLATE_LENGTH == PLATE_LENGTH - 1) {
        return new Paint(Layer.BASE, palette.shade());
      }
      return new Paint(Layer.BASE, speck(texel) ? palette.highlight() : palette.light());
    }
    if (texel.hem() || texel.opening()) {
      return new Paint(Layer.BASE, palette.edge());
    }
    if (texel.rivetRow()) {
      return new Paint(
          Layer.BASE,
          texel.grid().across() % RIVET_SPACING == 1 ? palette.highlight() : palette.shade());
    }
    final int fromTop = texel.grid().upCount() - 1 - texel.grid().up();
    if (fromTop == 0) {
      return new Paint(Layer.BASE, palette.light());
    }
    final int row = fromTop % LAME_HEIGHT;
    if (row == 0) {
      return new Paint(Layer.BASE, palette.shade());
    }
    if (row == 1) {
      return new Paint(Layer.BASE, speck(texel) ? palette.highlight() : palette.light());
    }
    return new Paint(Layer.BASE, palette.base());
  }

  private static Paint stitched(final Palette palette, final Texel texel) {
    if (texel.hem() || texel.opening()) {
      return new Paint(Layer.DYE, palette.edge());
    }
    if (texel.rivetRow() && texel.grid().across() % 2 == 0) {
      return new Paint(Layer.BASE, palette.accent());
    }
    if (texel.side() == Side.TOP && texel.spine() && texel.grid().across() % 2 == 0) {
      return new Paint(Layer.BASE, palette.accent());
    }
    if (Math.floorMod(texel.hash(), 100) < LEATHER_MOTTLE_PERCENT) {
      return new Paint(Layer.DYE, palette.shade());
    }
    return new Paint(Layer.DYE, texel.side() == Side.TOP ? palette.light() : palette.base());
  }

  private static Paint scutes(final Palette palette, final boolean trimDyed, final Texel texel) {
    if (texel.hem() || texel.opening()) {
      return trimDyed ? new Paint(Layer.DYE, DYED_TRIM) : new Paint(Layer.BASE, palette.edge());
    }
    final int band = texel.grid().up() % SCUTE_HEIGHT;
    final int row = texel.grid().up() / SCUTE_HEIGHT;
    if ((texel.grid().across() + row * 2) % SCUTE_LENGTH == 0 && band != SCUTE_HEIGHT - 1) {
      return new Paint(Layer.BASE, palette.shade());
    }
    if (band == 0) {
      return new Paint(Layer.BASE, palette.shade());
    }
    if (band == SCUTE_HEIGHT - 1) {
      return new Paint(Layer.BASE, speck(texel) ? palette.highlight() : palette.light());
    }
    return new Paint(Layer.BASE, palette.base());
  }

  private static boolean speck(final Texel texel) {
    return Math.floorMod(texel.hash(), 100) < SPECK_PERCENT;
  }

  private static boolean sharedWithEarlierCube(
      final Vector3f point, final List<CubeGeometry.Bounds> boxes, final int own) {
    for (int i = 0; i < own; i++) {
      final Vector3f min = boxes.get(i).min().sub(SHARED_SURFACE, SHARED_SURFACE, SHARED_SURFACE);
      final Vector3f max = boxes.get(i).max().add(SHARED_SURFACE, SHARED_SURFACE, SHARED_SURFACE);
      if (point.x >= min.x
          && point.x <= max.x
          && point.y >= min.y
          && point.y <= max.y
          && point.z >= min.z
          && point.z <= max.z) {
        return true;
      }
    }
    return false;
  }

  private static boolean occluded(
      final CubeGeometry.Face face, final List<CubeGeometry.Bounds> boxes, final int own) {
    final Vector3f centre = new Vector3f();
    for (final Vector3f corner : face.corners()) {
      centre.add(face.pose().point(corner));
    }
    centre.div(face.corners().length).add(new Vector3f(face.posedNormal()).mul(OCCLUSION_PROBE));
    for (int i = 0; i < boxes.size(); i++) {
      if (i != own && boxes.get(i).contains(centre)) {
        return true;
      }
    }
    return false;
  }

  private static CubeGeometry.Bounds posedBox(final List<CubeGeometry.Face> faces) {
    final CubeGeometry.Bounds box = new CubeGeometry.Bounds();
    for (final CubeGeometry.Face face : faces) {
      for (final Vector3f corner : face.corners()) {
        box.include(face.pose().point(corner));
      }
    }
    return box;
  }

  static int hash(final int x, final int y, final int salt) {
    int h = x * 73_856_093 ^ y * 19_349_663 ^ salt * 83_492_791;
    h ^= h >>> 13;
    h *= 0x5BD1E995;
    return h ^ (h >>> 15);
  }
}
