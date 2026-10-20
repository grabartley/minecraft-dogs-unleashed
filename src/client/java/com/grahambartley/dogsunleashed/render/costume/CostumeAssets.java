package com.grahambartley.dogsunleashed.render.costume;

import net.minecraft.util.Identifier;

public record CostumeAssets(Identifier sheet, Identifier partsModel, Identifier partsTexture) {

  public static CostumeAssets of(final Identifier costumeItem) {
    final String name = costumeItem.getPath();
    return new CostumeAssets(
        costumeItem.withPath("textures/entity/costume/" + name + ".png"),
        costumeItem.withPath("geo/costume/" + name + ".geo.json"),
        costumeItem.withPath("textures/entity/costume/" + name + "_parts.png"));
  }
}
