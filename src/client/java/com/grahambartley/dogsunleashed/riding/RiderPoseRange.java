package com.grahambartley.dogsunleashed.riding;

import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;

public record RiderPoseRange(RiderPose rest, RiderPose gallop) {

  public static final float FULL_GALLOP_LIMB_SPEED = 0.6f;
  public static final RiderPoseRange UPRIGHT =
      new RiderPoseRange(RiderPose.UPRIGHT, RiderPose.UPRIGHT);

  public static RiderPoseRange parse(final JsonObject json) {
    final RiderPose rest =
        json.get("rest") instanceof JsonObject restJson
            ? RiderPose.parse(restJson, RiderPose.UPRIGHT)
            : RiderPose.UPRIGHT;
    final RiderPose gallop =
        json.get("gallop") instanceof JsonObject gallopJson
            ? RiderPose.parse(gallopJson, rest)
            : rest;
    return new RiderPoseRange(rest, gallop);
  }

  public RiderPose at(final float mountLimbSpeed) {
    return this.rest.blend(
        this.gallop, MathHelper.clamp(mountLimbSpeed / FULL_GALLOP_LIMB_SPEED, 0.0f, 1.0f));
  }
}
