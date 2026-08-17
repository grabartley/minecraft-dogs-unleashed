package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelActionPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class WheelActionNetworkHandler {

  private WheelActionNetworkHandler() {}

  // Same convention as handleSetPetName: no ACK packet, the COMMAND DataTracker broadcast is the
  // source of truth for the client.
  public static void handleSelectWheelAction(
      final SelectWheelActionPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();
    final DogWheelAction action = DogWheelAction.fromId(payload.actionId());
    if (action == null) {
      return;
    }

    world
        .getServer()
        .execute(
            () -> {
              if (!(world.getEntity(payload.dogId()) instanceof UnleashedDogEntity dog)
                  || !dog.isOwner(player)
                  || !dog.isAlive()) {
                return;
              }
              final String dogName = dog.getTamedName();
              if (action.isOneShot()) {
                runOneShotWheelAction(action, dog, player, dogName);
                return;
              }
              final DogCommand command = action.command();
              dog.applyCommand(command);
              dog.acknowledgeCommand();
              player.sendMessage(Text.translatable(command.messageKey(), dogName), true);
            });
  }

  private static void runOneShotWheelAction(
      final DogWheelAction action,
      final UnleashedDogEntity dog,
      final ServerPlayerEntity player,
      final String dogName) {
    switch (action) {
      case GO_TO_BED -> {
        final BlockPos bedPos = dog.getAssignedBedPos().orElse(null);
        if (bedPos == null) {
          return;
        }
        dog.commandToSleep(bedPos);
        player.sendMessage(
            Text.translatable("block.dogs-unleashed.dog_bed.sleep_command", dogName), true);
      }
      case EQUIPMENT -> player.openHandledScreen(dog);
      default -> {}
    }
  }
}
