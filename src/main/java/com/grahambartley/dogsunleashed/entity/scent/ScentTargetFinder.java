package com.grahambartley.dogsunleashed.entity.scent;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface ScentTargetFinder {

  ScentTargetKind kind();

  ScentShellScan.Status advance(ServerWorld world);

  @Nullable
  Vec3d target();

  boolean isStillThere(ServerWorld world);
}
