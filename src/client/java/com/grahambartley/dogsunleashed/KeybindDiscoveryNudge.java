package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.config.ClientState;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/** One-shot toast pointing first-time players at the unbound Pet Manager keybind. */
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
    // The initial resource reload is still running behind the splash overlay at CLIENT_STARTED,
    // and the toast bakes its wrapped description at construction time, so building it any
    // earlier would freeze the untranslated key into the toast.
    if (!nudgePending || client.getOverlay() != null) {
      return;
    }
    nudgePending = false;
    final Path statePath = ClientState.defaultPath();
    ClientState.save(statePath, ClientState.load(statePath).withKeybindNudgeShown(true));
    showToast(client);
  }

  private static void showToast(final MinecraftClient client) {
    // SystemToast.show keeps the description on one line and lets it run off the screen edge;
    // create wraps it and sizes the toast to fit.
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
