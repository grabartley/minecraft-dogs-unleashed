package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class RiddenDog {

  private RiddenDog() {}

  public static @Nullable UnleashedDogEntity of(final @Nullable PlayerEntity player) {
    return player != null
            && player.getVehicle() instanceof UnleashedDogEntity dog
            && dog.getRiding().rider() == player
        ? dog
        : null;
  }
}
