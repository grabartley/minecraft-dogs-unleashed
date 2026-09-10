package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.config.ClientState;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class KeybindDiscoveryNudge {

  private static final long TOAST_DURATION_MS = 10_000L;

  private static boolean nudgePending = false;

  private KeybindDiscoveryNudge() {}

  public static void register() {
    ClientLifecycleEvents.CLIENT_STARTED.register(KeybindDiscoveryNudge::onClientStarted);
    ClientTickEvents.END_CLIENT_TICK.register(KeybindDiscoveryNudge::onClientTick);
  }

  static boolean shouldShowNudge(final @Nullable ClientState state) {
    return state != null && !state.keybindNudgeShown();
  }

  private static void onClientStarted(final MinecraftClient client) {
    nudgePending = shouldShowNudge(ClientState.load(ClientState.defaultPath()));
  }

  private static void onClientTick(final MinecraftClient client) {
    if (!nudgePending || client.getOverlay() != null) {
      return;
    }
    nudgePending = false;
    final Path statePath = ClientState.defaultPath();
    ClientState.save(statePath, ClientState.load(statePath).withKeybindNudgeShown(true));
    showToast(client);
  }

  private static void showToast(final MinecraftClient client) {
    client
        .getToastManager()
        .add(
            SystemToast.create(
                client,
                new SystemToast.Type(TOAST_DURATION_MS),
                Text.translatable("toast.dogs-unleashed.keybind_nudge.title"),
                Text.translatable("toast.dogs-unleashed.keybind_nudge.description")));
  }
}
