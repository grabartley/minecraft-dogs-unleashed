package com.grahambartley.dogsunleashed.render.layer;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.item.pendant.PendantFamily;
import net.minecraft.util.Identifier;

public enum PendantCharm {
  WARD("pendant_ward"),
  GUARDIAN("pendant_guardian"),
  VITALITY("pendant_vitality"),
  LOYALTY("pendant_loyalty");

  private final Identifier model;
  private final Identifier texture;

  PendantCharm(final String name) {
    this.model = Identifier.of(MOD_ID, "geo/" + name + ".geo.json");
    this.texture = Identifier.of(MOD_ID, "textures/entity/pendant/" + name + ".png");
  }

  public static PendantCharm of(final PendantFamily family) {
    return switch (family) {
      case WARDING -> WARD;
      case GUARDIAN -> GUARDIAN;
      case VITALITY -> VITALITY;
      case LOYALTY -> LOYALTY;
    };
  }

  public Identifier model() {
    return this.model;
  }

  public Identifier texture() {
    return this.texture;
  }
}
