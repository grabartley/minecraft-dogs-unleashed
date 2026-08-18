package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.pet.PetLifeState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/** How a pet's place in the death and resurrection cycle reads in the pet manager screens. */
public final class PetLifeStateDisplay {

  public static final int UNDEAD_COLOR = 0x8FBC8F;
  public static final int GONE_COLOR = 0xFF5555;

  private PetLifeStateDisplay() {}

  public static int nameColor(
      final PetLifeState lifeState, final int livingColor, final int deceasedColor) {
    return switch (lifeState) {
      case LIVING -> livingColor;
      case UNDEAD -> UNDEAD_COLOR;
      case DECEASED, LOST -> deceasedColor;
    };
  }

  /** {@code null} for a plain living pet, which needs no status of its own. */
  public static @Nullable Text statusLabel(final PetLifeState lifeState) {
    return switch (lifeState) {
      case LIVING -> null;
      case UNDEAD -> Text.translatable("screen.dogs-unleashed.pet_manager.undead");
      case DECEASED -> Text.translatable("screen.dogs-unleashed.pet_manager.deceased");
      case LOST -> Text.translatable("screen.dogs-unleashed.pet_manager.lost");
    };
  }

  public static int statusColor(final PetLifeState lifeState) {
    return lifeState == PetLifeState.UNDEAD ? UNDEAD_COLOR : GONE_COLOR;
  }
}
