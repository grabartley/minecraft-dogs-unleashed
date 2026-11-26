package com.grahambartley.dogsunleashed.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DogsUnleashedDataGenerator implements DataGeneratorEntrypoint {

  @Override
  public void onInitializeDataGenerator(final FabricDataGenerator generator) {
    generator.createPack().addProvider(WoodenFurnitureRecipeProvider::new);
  }
}
