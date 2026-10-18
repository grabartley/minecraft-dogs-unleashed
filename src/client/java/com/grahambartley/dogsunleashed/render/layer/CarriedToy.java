package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public record CarriedToy(float worldPixels, float modelPixels, float centreHeight) {
  private static final float PIXELS_PER_BLOCK = 16f;

  public static final CarriedToy TENNIS_BALL = new CarriedToy(2f, 12f, 6f);
  public static final CarriedToy FRISBEE = new CarriedToy(7f, 16f, 1f);
  public static final CarriedToy STICK = new CarriedToy(9f, 16f, 0f);

  public static CarriedToy of(final FetchItemType fetchType) {
    if (fetchType == FetchTypes.FRISBEE) {
      return FRISBEE;
    }
    if (fetchType == FetchTypes.STICK) {
      return STICK;
    }
    return TENNIS_BALL;
  }

  public float scaleIn(final Matrix4f entityOrigin, final Matrix4f frame) {
    return this.worldPixels
        / this.modelPixels
        * entityOrigin.getScale(new Vector3f()).x
        / frame.getScale(new Vector3f()).x;
  }

  public float centreDrop() {
    return -this.centreHeight / PIXELS_PER_BLOCK;
  }
}
