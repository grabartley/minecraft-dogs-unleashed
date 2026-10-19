package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.DogSleepController;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelActionPayload;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelSubActionPayload;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class WheelActionNetworkHandler {

  private WheelActionNetworkHandler() {}

  public static void handleSelectWheelAction(
      final SelectWheelActionPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();
    final DogWheelAction action = DogWheelAction.fromId(payload.actionId());
    if (action == null) {
      return;
    }

    final UnleashedDogEntity dog = ownedLivingDog(world, payload.dogId(), player);
    if (dog == null) {
      return;
    }
    final String dogName = PetNames.tamedNameOf(dog);
    if (action.isOneShot()) {
      runOneShotWheelAction(action, dog, player, dogName);
      return;
    }
    final DogCommand command = action.command();
    dog.getCommandController().apply(command);
    dog.getCommandController().acknowledge();
    player.sendMessage(Text.translatable(command.messageKey(), dogName), true);
  }

  public static void handleSelectWheelSubAction(
      final SelectWheelSubActionPayload payload, final ServerPlayNetworking.Context context) {
    selectSubAction(DogWheelSubActions.REGISTERED, payload, context.player());
  }

  public static void selectSubAction(
      final DogWheelSubActions registry,
      final SelectWheelSubActionPayload payload,
      final ServerPlayerEntity player) {
    final DogWheelSubAction action = registry.byId(payload.subActionId()).orElse(null);
    if (action == null) {
      return;
    }
    final UnleashedDogEntity dog = ownedLivingDog(player.getServerWorld(), payload.dogId(), player);
    if (dog == null) {
      return;
    }
    final String refusal = action.refusal().refusalKey(dog, player);
    if (refusal != null) {
      player.sendMessage(Text.translatable(refusal), true);
      return;
    }
    action.runner().run(dog, player);
  }

  private static @Nullable UnleashedDogEntity ownedLivingDog(
      final ServerWorld world, final UUID dogId, final ServerPlayerEntity player) {
    return world.getEntity(dogId) instanceof UnleashedDogEntity dog
            && dog.isOwner(player)
            && dog.isAlive()
        ? dog
        : null;
  }

  private static void runOneShotWheelAction(
      final DogWheelAction action,
      final UnleashedDogEntity dog,
      final ServerPlayerEntity player,
      final String dogName) {
    switch (action) {
      case GO_TO_BED -> {
        final BlockPos bedPos = dog.getSleepController().getAssignedBedPos().orElse(null);
        if (bedPos == null || dog.getSleepController().releaseSpotIfTaken(bedPos)) {
          return;
        }
        dog.getComponents().get(DogDeathWatch.class).standDown();
        final String replyKey =
            dog.getSleepController().commandToSleep(bedPos)
                ? "block.dogs-unleashed.dog_bed.sleep_command"
                : DogSleepController.NOT_SLEEPY_MESSAGE_KEY;
        player.sendMessage(Text.translatable(replyKey, dogName), true);
      }
      case EQUIPMENT -> player.openHandledScreen(dog);
      case RIDE -> {
        final String refusal = dog.getRiding().refusalKey(player);
        if (refusal != null || !dog.getRiding().mount(player)) {
          player.sendMessage(
              Text.translatable(
                  refusal != null ? refusal : "screen.dogs-unleashed.command_wheel.ride.busy"),
              true);
        }
      }
      default -> {}
    }
  }
}
