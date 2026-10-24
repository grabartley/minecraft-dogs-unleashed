package com.grahambartley.dogsunleashed.render.armour;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.fur.FurLook;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class ArmourClearance {

  static final float BASE = 0.25f / 16.0f;
  static final float MARGIN = 0.15f / 16.0f;

  private static final Map<BakedGeoModel, Vector3f> BY_MODEL = new MapMaker().weakKeys().makeMap();

  private ArmourClearance() {}

  public static Vector3f inflationFor(final BakedGeoModel model, final UnleashedDogBreed breed) {
    return new Vector3f(
        BY_MODEL.computeIfAbsent(model, key -> new Vector3f(measure(key, breed), BASE, BASE)));
  }

  static float inflation(
      final List<CubeGeometry.Bounds> armour, final List<CubeGeometry.Bounds> others) {
    final CubeGeometry.Bounds body = new CubeGeometry.Bounds();
    armour.forEach(
        box -> {
          body.include(box.min());
          body.include(box.max());
        });
    float needed = 0.0f;
    for (final CubeGeometry.Bounds other : others) {
      if (other.min().y >= body.min().y
          || !overlaps(other.min().y, other.max().y, body.min().y, body.max().y)
          || !overlaps(other.min().z, other.max().z, body.min().z, body.max().z)) {
        continue;
      }
      needed =
          Math.max(
              needed,
              Math.max(other.max().x - body.max().x, body.min().x - other.min().x) + MARGIN);
    }
    return Math.max(BASE, needed);
  }

  private static boolean overlaps(
      final float minA, final float maxA, final float minB, final float maxB) {
    return minA < maxB && maxA > minB;
  }

  private static float measure(final BakedGeoModel model, final UnleashedDogBreed breed) {
    final List<CubeGeometry.Bounds> armour = new ArrayList<>();
    final List<CubeGeometry.Bounds> others = new ArrayList<>();
    collect(model.topLevelBones(), breed, armour, others);
    return inflation(armour, others);
  }

  private static void collect(
      final List<GeoBone> bones,
      final UnleashedDogBreed breed,
      final List<CubeGeometry.Bounds> armour,
      final List<CubeGeometry.Bounds> others) {
    for (final GeoBone bone : bones) {
      final List<List<CubeGeometry.Face>> cubes = CubeGeometry.facesByCube(bone);
      for (int i = 0; i < cubes.size(); i++) {
        final CubeGeometry.Bounds box = new CubeGeometry.Bounds();
        cubes
            .get(i)
            .forEach(
                face ->
                    Arrays.stream(face.corners()).map(face.pose()::point).forEach(box::include));
        if (box.isEmpty()) {
          continue;
        }
        if (ArmourFit.covers(breed, bone.getName(), bone.getCubes().get(i))) {
          armour.add(box);
        } else if (!ArmourFit.torsoBones(breed).contains(bone.getName())
            && !FurLook.isFur(RigProfiles.of(breed), bone.getName())) {
          others.add(box);
        }
      }
      collect(bone.getChildBones(), breed, armour, others);
    }
  }
}
