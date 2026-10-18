package com.grahambartley.dogsunleashed.render.cosmetic;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import net.minecraft.util.Identifier;

public final class CosmeticAssets {

  private CosmeticAssets() {}

  public static Identifier model(final DogCosmetic cosmetic) {
    return Identifier.of(MOD_ID, "geo/" + cosmetic.itemId() + ".geo.json");
  }

  public static Identifier texture(final DogCosmetic cosmetic) {
    return Identifier.of(MOD_ID, "textures/entity/cosmetic/" + cosmetic.itemId() + ".png");
  }
}
