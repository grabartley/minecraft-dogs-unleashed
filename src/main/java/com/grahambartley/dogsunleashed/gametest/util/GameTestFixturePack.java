package com.grahambartley.dogsunleashed.gametest.util;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GameTestFixturePack {

  static final String GAMETEST_PROPERTY = "fabric-api.gametest";
  private static final String PACK_NAME = "gametest_fixtures";

  private GameTestFixturePack() {}

  public static void registerWhenTesting() {
    if (!Boolean.getBoolean(GAMETEST_PROPERTY)) {
      return;
    }
    FabricLoader.getInstance()
        .getModContainer(DogsUnleashed.MOD_ID)
        .ifPresent(
            mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.of(DogsUnleashed.MOD_ID, PACK_NAME),
                    mod,
                    Text.literal("Dogs Unleashed game test fixtures"),
                    ResourcePackActivationType.ALWAYS_ENABLED));
  }
}
