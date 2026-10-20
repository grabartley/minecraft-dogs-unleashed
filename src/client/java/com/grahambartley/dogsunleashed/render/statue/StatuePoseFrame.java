package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.DogStatuePose;

public final class StatuePoseFrame {

  private static final double HELD_HOWL_TICK = 40.0;

  private StatuePoseFrame() {}

  public static double tickOf(final DogStatuePose pose) {
    return pose == DogStatuePose.HOWL ? HELD_HOWL_TICK : 0.0;
  }
}
