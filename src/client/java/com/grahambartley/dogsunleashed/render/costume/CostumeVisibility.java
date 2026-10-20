package com.grahambartley.dogsunleashed.render.costume;

public final class CostumeVisibility {

  private CostumeVisibility() {}

  public static boolean shows(
      final boolean wearsCostume, final boolean armoured, final boolean invisible) {
    return wearsCostume && !armoured && !invisible;
  }
}
