package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogSaddlebags;
import com.grahambartley.dogsunleashed.entity.DogWheelIcons;
import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogDeliveries {

  public static final int WHEEL_ACTION_ID = 1;
  public static final Identifier WHEEL_ICON = DogWheelIcons.sprite("delivery");
  public static final String WHEEL_LABEL_KEY = "command.dogs-unleashed.deliver";
  public static final String ON_DELIVERY_KEY = "screen.dogs-unleashed.command_wheel.on_delivery";

  private DogDeliveries() {}

  public static void registerWheelAction() {
    DogWheelSubActions.REGISTERED.register(
        new DogWheelSubAction(
            WHEEL_ACTION_ID,
            WHEEL_LABEL_KEY,
            WHEEL_ICON,
            DogDeliveries::refusalKey,
            (dog, player) ->
                ModNetworking.sendOpenDeliveryPicker(player, dog, recipientsFor(player))));
  }

  public static @Nullable String refusalKey(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    return DeliveryEligibility.refusalKey(
            dog.getComponents().get(DogSaddlebags.class).isWorn(),
            !recipientsFor(player).isEmpty(),
            dog.hasPassengers() || dog.hasVehicle(),
            dog.getSleepController().isAsleep() || dog.getSleepController().isCommandedToSleep(),
            dog.isSitting() || dog.isInSittingPose(),
            dog.isLeashed())
        .orElse(null);
  }

  public static List<ServerPlayerEntity> recipientsFor(final ServerPlayerEntity owner) {
    return owner.getServer().getPlayerManager().getPlayerList().stream()
        .filter(player -> player != owner && isPresent(player))
        .toList();
  }

  public static boolean isPresent(final @Nullable ServerPlayerEntity player) {
    return player != null && !player.isDisconnected() && player.isAlive() && !player.isSpectator();
  }

  public static boolean isWaitingFor(final UnleashedDogEntity dog, final PlayerEntity player) {
    return dog.getWorld() instanceof ServerWorld world
        && Deliveries.get(world.getServer()).isWaitingFor(dog.getUuid(), player.getUuid());
  }

  public static boolean isWaiting(final UnleashedDogEntity dog) {
    return dog.getWorld() instanceof ServerWorld world
        && Deliveries.get(world.getServer())
            .of(dog.getUuid())
            .map(delivery -> delivery.leg() == DeliveryLeg.WAITING)
            .orElse(false);
  }

  public static void handedOver(final UnleashedDogEntity dog, final PlayerEntity player) {
    if (isWaitingFor(dog, player) && dog.getWorld() instanceof ServerWorld world) {
      final UUID pet = dog.getUuid();
      DogsUnleashed.runNextTick(() -> DeliveryRounds.sendHome(world.getServer(), pet));
    }
  }
}
