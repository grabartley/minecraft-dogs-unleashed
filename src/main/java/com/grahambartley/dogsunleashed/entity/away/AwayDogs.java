package com.grahambartley.dogsunleashed.entity.away;

import com.grahambartley.dogsunleashed.entity.delivery.Deliveries;
import com.grahambartley.dogsunleashed.entity.delivery.Delivery;
import com.grahambartley.dogsunleashed.entity.expedition.Expeditions;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

public final class AwayDogs {

  private AwayDogs() {}

  public static boolean isOffWorld(final MinecraftServer server, final UUID pet) {
    return Expeditions.get(server).isAway(pet) || Deliveries.get(server).isOffWorld(pet);
  }

  public static PetAwayStatus statusOf(final MinecraftServer server, final UUID pet) {
    if (Expeditions.get(server).isAway(pet)) {
      return PetAwayStatus.EXPLORING;
    }
    return Deliveries.get(server)
        .of(pet)
        .map(Delivery::leg)
        .map(leg -> leg.awayStatus())
        .orElse(PetAwayStatus.HOME);
  }

  public static Optional<String> busyKey(final MinecraftServer server, final UUID pet) {
    return statusOf(server, pet).busyKey();
  }
}
