package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.screen.PetManagerScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class ModKeyBindings {

  private static KeyBinding openPetManagerKey;

  public static void register() {
    openPetManagerKey =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.dogs-unleashed.open_pet_manager",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "category.dogs-unleashed.general"));

    ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::onClientTick);
  }

  private static void onClientTick(MinecraftClient client) {
    while (openPetManagerKey.wasPressed()) {
      if (client.currentScreen == null) {
        client.setScreen(new PetManagerScreen());
      }
    }
  }
}
