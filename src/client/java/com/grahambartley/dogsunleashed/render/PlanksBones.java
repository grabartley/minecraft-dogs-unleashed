package com.grahambartley.dogsunleashed.render;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class PlanksBones {

  private static final String NAME = "planks";
  private static final String SUFFIX = "_" + NAME;

  private PlanksBones() {}

  public static boolean isPlanks(final String boneName) {
    return boneName.equals(NAME) || boneName.endsWith(SUFFIX);
  }

  public static void hideIn(final BakedGeoModel model) {
    model.topLevelBones().forEach(PlanksBones::hideIn);
  }

  private static void hideIn(final GeoBone bone) {
    if (isPlanks(bone.getName())) {
      bone.setHidden(true);
    }
    bone.getChildBones().forEach(PlanksBones::hideIn);
  }
}
