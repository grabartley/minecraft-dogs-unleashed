package com.grahambartley.dogsunleashed.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class WhistlePointGeometry {

  static final double FACE_INSET = 0.01;

  private WhistlePointGeometry() {}

  public static Vec3d closestPoint(final Box box, final Vec3d from) {
    return new Vec3d(
        MathHelper.clamp(from.x, box.minX, box.maxX),
        MathHelper.clamp(from.y, box.minY, box.maxY),
        MathHelper.clamp(from.z, box.minZ, box.maxZ));
  }

  public static Vec3d faceCentre(final Box box, final Direction face) {
    final Vec3d centre = box.getCenter();
    return centre.add(
        face.getOffsetX() * (box.getLengthX() / 2 - FACE_INSET),
        face.getOffsetY() * (box.getLengthY() / 2 - FACE_INSET),
        face.getOffsetZ() * (box.getLengthZ() / 2 - FACE_INSET));
  }

  public static List<Direction> facesToTry(final Direction aimed) {
    final List<Direction> faces = new ArrayList<>(List.of(Direction.values()));
    faces.remove(aimed);
    faces.add(0, aimed);
    return faces;
  }
}
