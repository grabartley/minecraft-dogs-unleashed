package com.grahambartley.dogsunleashed.portrait;

public final class PortraitPoseRules {

  private PortraitPoseRules() {}

  public static boolean poses(
      final boolean hasPortrait, final boolean mainHand, final boolean emptyHand) {
    return hasPortrait && mainHand && emptyHand;
  }
}
