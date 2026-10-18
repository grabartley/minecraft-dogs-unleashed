package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.network.RegistryByteBuf;

public record RidingView(Kind kind, int staminaPercent) {

  public static final RidingView NONE = new RidingView(Kind.NONE, 0);
  public static final RidingView TOO_YOUNG = new RidingView(Kind.TOO_YOUNG, 0);

  public enum Kind {
    NONE,
    TOO_YOUNG,
    READY
  }

  public static RidingView of(
      final UnleashedDogBreed breed, final int growthStage, final int staminaPercent) {
    if (!breed.nature().growsThroughStages()) {
      return NONE;
    }
    return DireWolfGrowthStage.fromNumber(growthStage).rideable()
        ? new RidingView(Kind.READY, Math.max(0, Math.min(100, staminaPercent)))
        : TOO_YOUNG;
  }

  void write(final RegistryByteBuf buf) {
    buf.writeEnumConstant(this.kind);
    buf.writeVarInt(this.staminaPercent);
  }

  static RidingView read(final RegistryByteBuf buf) {
    return new RidingView(buf.readEnumConstant(Kind.class), buf.readVarInt());
  }
}
