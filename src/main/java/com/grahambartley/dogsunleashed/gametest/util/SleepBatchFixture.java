package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.entity.DogPlaySession;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

public final class SleepBatchFixture {

  public static final long NIGHT_TIME = 15000;

  private SleepBatchFixture() {}

  public static void pinNight(final ServerWorld world) {
    world.setTimeOfDay(NIGHT_TIME);
  }

  public static void prepare(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    DogPlaySession.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }

  public static void teardown(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
    DogPlaySession.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
  }
}
