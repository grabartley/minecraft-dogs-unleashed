package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class PetLifeStateDisplay {

  public static final int UNDEAD_COLOR = 0x8FBC8F;
  public static final int GONE_COLOR = 0xFF5555;
  public static final int AWAY_COLOR = 0xFFAA00;
  public static final int SENIOR_COLOR = 0xBBBBBB;

  private PetLifeStateDisplay() {}

  public static int nameColor(
      final PetLifeState lifeState, final int livingColor, final int deceasedColor) {
    return switch (lifeState) {
      case LIVING -> livingColor;
      case UNDEAD -> UNDEAD_COLOR;
      case DECEASED, LOST -> deceasedColor;
    };
  }

  public static @Nullable Text statusLabel(final PetLifeState lifeState) {
    return switch (lifeState) {
      case LIVING -> null;
      case UNDEAD -> Text.translatable("screen.dogs-unleashed.pet_manager.undead");
      case DECEASED -> Text.translatable("screen.dogs-unleashed.pet_manager.deceased");
      case LOST -> Text.translatable("screen.dogs-unleashed.pet_manager.lost");
    };
  }

  public static @Nullable Text statusLabel(final PetSyncData pet) {
    final String awayKey = pet.away().translationKey();
    return awayKey != null ? Text.translatable(awayKey) : statusLabel(pet.lifeState());
  }

  public static @Nullable Text rowTag(final PetSyncData pet) {
    final Text status = statusLabel(pet);
    if (status != null) {
      return status;
    }
    return pet.age() == PetAge.SENIOR ? Text.translatable(PetAge.SENIOR.translationKey()) : null;
  }

  public static int rowTagColor(final PetSyncData pet) {
    return statusLabel(pet) != null ? statusColor(pet) : SENIOR_COLOR;
  }

  public static int statusColor(final PetSyncData pet) {
    return pet.away().isAway() ? AWAY_COLOR : statusColor(pet.lifeState());
  }

  public static int statusColor(final PetLifeState lifeState) {
    return lifeState == PetLifeState.UNDEAD ? UNDEAD_COLOR : GONE_COLOR;
  }
}
