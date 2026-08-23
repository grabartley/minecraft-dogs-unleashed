package com.grahambartley.dogsunleashed.block;

import net.minecraft.util.math.Vec3d;

/** Exactly where and which way a dog lies down in the bed it was assigned. */
public record DogSleepPose(Vec3d position, float yaw) {}
