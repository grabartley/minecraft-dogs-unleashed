package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogExpeditions {

  public static final int WHEEL_ACTION_ID = 0;
  public static final String WHEEL_LABEL_KEY = "command.dogs-unleashed.explore";

  private DogExpeditions() {}

  public static void registerWheelAction() {
    DogWheelSubActions.REGISTERED.register(
        new DogWheelSubAction(
            WHEEL_ACTION_ID,
            WHEEL_LABEL_KEY,
            Identifier.ofVanilla("filled_map"),
            DogExpeditions::refusalKey,
            (dog, player) -> ExpeditionDeparture.send(dog, player)));
  }

  public static @Nullable String refusalKey(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    return ExpeditionEligibility.refusalKey(
            dog.isUndead(),
            dog.hasPassengers() || dog.hasVehicle(),
            dog.getSleepController().isAsleep() || dog.getSleepController().isCommandedToSleep(),
            dog.isSitting() || dog.isInSittingPose(),
            dog.isLeashed())
        .orElse(null);
  }
}
