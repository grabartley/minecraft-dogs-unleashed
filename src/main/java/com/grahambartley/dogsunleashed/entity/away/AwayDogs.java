package com.grahambartley.dogsunleashed.entity.away;

import com.grahambartley.dogsunleashed.entity.delivery.Deliveries;
import com.grahambartley.dogsunleashed.entity.delivery.Delivery;
import com.grahambartley.dogsunleashed.entity.expedition.Expeditions;
import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

public final class AwayDogs {

  public static final String EXPLORING_KEY = "message.dogs-unleashed.expedition.away";
  public static final String DELIVERING_KEY = "message.dogs-unleashed.delivery.away";

  private AwayDogs() {}

  public static boolean isOffWorld(final MinecraftServer server, final UUID pet) {
    return Expeditions.get(server).isAway(pet) || Deliveries.get(server).isOffWorld(pet);
  }

  public static PetAwayStatus statusOf(final MinecraftServer server, final UUID pet) {
    if (Expeditions.get(server).isAway(pet)) {
      return PetAwayStatus.EXPLORING;
    }
    return Deliveries.get(server).of(pet).map(AwayDogs::statusOf).orElse(PetAwayStatus.HOME);
  }

  public static Optional<String> busyKey(final MinecraftServer server, final UUID pet) {
    return switch (statusOf(server, pet)) {
      case HOME -> Optional.empty();
      case EXPLORING -> Optional.of(EXPLORING_KEY);
      case DELIVERING, WAITING, RETURNING -> Optional.of(DELIVERING_KEY);
    };
  }

  private static PetAwayStatus statusOf(final Delivery delivery) {
    return switch (delivery.leg()) {
      case TO_RECIPIENT -> PetAwayStatus.DELIVERING;
      case WAITING -> PetAwayStatus.WAITING;
      case TO_OWNER -> PetAwayStatus.RETURNING;
    };
  }
}
