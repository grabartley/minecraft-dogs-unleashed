package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.block.DogBedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DogBedWheelAction {

  public static final int WHEEL_ACTION_ID = 5;
  public static final Identifier WHEEL_ICON = DogWheelIcons.sprite("assign_bed");
  public static final String WHEEL_LABEL_KEY = "command.dogs-unleashed.assign_bed";
  public static final String PENDING_MESSAGE_KEY = "message.dogs-unleashed.pending_bed_assignment";

  private DogBedWheelAction() {}

  public static void register(final DogWheelSubActions registry) {
    registry.register(action());
  }

  static DogWheelSubAction action() {
    return new DogWheelSubAction(
        WHEEL_ACTION_ID,
        WHEEL_LABEL_KEY,
        WHEEL_ICON,
        (dog, player) -> null,
        DogBedWheelAction::pickBedFor,
        dog -> isShownFor(dog.getBreed()));
  }

  static boolean isShownFor(final UnleashedDogBreed breed) {
    return breed.nature().sleepsInBeds();
  }

  private static void pickBedFor(final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    DogBedBlock.setPendingAssignment(player.getUuid(), dog.getUuid());
    DogInteractions.tell(dog, player, PENDING_MESSAGE_KEY);
  }
}
