package com.grahambartley.dogsunleashed.entity;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record DogWheelSubAction(
    int id,
    String translationKey,
    Identifier icon,
    Refusal refusal,
    Runner runner,
    Visibility visibility) {

  public static final Visibility ALWAYS_SHOWN = dog -> true;

  public DogWheelSubAction(
      final int id,
      final String translationKey,
      final Identifier icon,
      final Refusal refusal,
      final Runner runner) {
    this(id, translationKey, icon, refusal, runner, ALWAYS_SHOWN);
  }

  @FunctionalInterface
  public interface Refusal {
    @Nullable
    String refusalKey(UnleashedDogEntity dog, ServerPlayerEntity player);
  }

  @FunctionalInterface
  public interface Runner {
    void run(UnleashedDogEntity dog, ServerPlayerEntity player);
  }

  @FunctionalInterface
  public interface Visibility {
    boolean shownFor(UnleashedDogEntity dog);
  }
}
