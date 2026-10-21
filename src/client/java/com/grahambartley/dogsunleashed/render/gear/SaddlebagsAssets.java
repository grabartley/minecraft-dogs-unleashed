package com.grahambartley.dogsunleashed.render.gear;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import net.minecraft.util.Identifier;

public final class SaddlebagsAssets {

  public static final Identifier MODEL = Identifier.of(MOD_ID, "geo/saddlebags.geo.json");
  public static final Identifier TEXTURE =
      Identifier.of(MOD_ID, "textures/entity/gear/saddlebags.png");

  private SaddlebagsAssets() {}
}
