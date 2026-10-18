package com.grahambartley.dogsunleashed.entity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record DogWheelSubAction(
    int id, String translationKey, Identifier iconItemId, Refusal refusal, Runner runner) {

  @FunctionalInterface
  public interface Refusal {
    @Nullable
    String refusalKey(UnleashedDogEntity dog, ServerPlayerEntity player);
  }

  @FunctionalInterface
  public interface Runner {
    void run(UnleashedDogEntity dog, ServerPlayerEntity player);
  }
}
