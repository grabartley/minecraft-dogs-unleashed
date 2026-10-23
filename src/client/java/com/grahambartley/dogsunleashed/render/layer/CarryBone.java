package com.grahambartley.dogsunleashed.render.layer;

import java.util.Optional;

public final class CarryBone {

  private CarryBone() {}

  public static Optional<String> of(
      final boolean frisbee,
      final boolean fetchedItem,
      final Optional<String> gripBone,
      final Optional<String> mouthBone) {
    return frisbee && !fetchedItem ? gripBone : mouthBone;
  }
}
