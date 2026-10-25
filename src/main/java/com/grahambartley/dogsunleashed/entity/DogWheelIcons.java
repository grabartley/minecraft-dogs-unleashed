package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.minecraft.util.Identifier;

public final class DogWheelIcons {

  public static final Identifier ACTIONS = sprite("actions");

  private DogWheelIcons() {}

  public static Identifier sprite(final String name) {
    return Identifier.of(DogsUnleashed.MOD_ID, "command_wheel/" + name);
  }
}
