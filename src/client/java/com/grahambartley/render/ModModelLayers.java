package com.grahambartley.render;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModModelLayers {

  public static final EntityModelLayer DALMATIAN =
      new EntityModelLayer(Identifier.of(MOD_ID, "dalmatian"), "main");
}
