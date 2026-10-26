package com.grahambartley.dogsunleashed.portrait;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;

public final class DogFacing {

  private DogFacing() {}

  public static void face(final UnleashedDogEntity dog, final float yaw) {
    dog.setYaw(yaw);
    dog.setBodyYaw(yaw);
    dog.setHeadYaw(yaw);
    dog.prevYaw = yaw;
    dog.prevBodyYaw = yaw;
    dog.prevHeadYaw = yaw;
    dog.setPitch(0.0f);
    dog.prevPitch = 0.0f;
  }
}
