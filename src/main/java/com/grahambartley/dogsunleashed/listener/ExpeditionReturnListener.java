package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ExpeditionReturnListener {

  private static final int CHECK_INTERVAL_TICKS = 20;

  private ExpeditionReturnListener() {}

  public static void initialize() {
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
            DogExpeditions.returnDue(server);
          }
        });
  }
}
