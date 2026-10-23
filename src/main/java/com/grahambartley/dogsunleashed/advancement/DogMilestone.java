package com.grahambartley.dogsunleashed.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum DogMilestone implements StringIdentifiable {
  WHISTLE_POINT("whistle_point"),
  GEAR_EQUIPPED("gear_equipped"),
  COLLAR_DYED("collar_dyed"),
  SCENT_FOUND("scent_found");

  public static final Codec<DogMilestone> CODEC =
      StringIdentifiable.createCodec(DogMilestone::values);

  private final String id;

  DogMilestone(final String id) {
    this.id = id;
  }

  @Override
  public String asString() {
    return this.id;
  }
}
