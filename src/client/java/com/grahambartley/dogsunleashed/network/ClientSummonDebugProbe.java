package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Diagnostic for summons that succeed server-side but stay invisible in game: a short while after
 * each summon click, logs which dog entities the client's world actually contains around the
 * player, so a missing entity here pinpoints the server-to-client tracking gap.
 */
public final class ClientSummonDebugProbe {

  private static final int PROBE_DELAY_TICKS = 40;
  private static final double PROBE_RADIUS = 16.0;

  private static final List<PendingProbe> pendingProbes = new ArrayList<>();

  private ClientSummonDebugProbe() {}

  private static final class PendingProbe {
    final String petName;
    int ticksLeft = PROBE_DELAY_TICKS;

    PendingProbe(String petName) {
      this.petName = petName;
    }
  }

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(ClientSummonDebugProbe::tick);
  }

  public static void schedule(String petName) {
    pendingProbes.add(new PendingProbe(petName));
  }

  private static void tick(MinecraftClient client) {
    if (pendingProbes.isEmpty()) {
      return;
    }
    final Iterator<PendingProbe> iterator = pendingProbes.iterator();
    while (iterator.hasNext()) {
      final PendingProbe probe = iterator.next();
      if (--probe.ticksLeft <= 0) {
        iterator.remove();
        run(client, probe.petName);
      }
    }
  }

  private static void run(MinecraftClient client, String petName) {
    if (client.player == null || client.world == null) {
      DogsUnleashed.log.info(
          "[PetDebug/Client] Probe for {} skipped: no client player or world", petName);
      return;
    }
    final List<String> nearbyDogs = new ArrayList<>();
    final List<String> allDogs = new ArrayList<>();
    for (final var entity : client.world.getEntities()) {
      if (!(entity instanceof UnleashedDogEntity dog)) {
        continue;
      }
      final String label =
          dog.getUuid().toString().substring(0, 8) + "@" + dog.getBlockPos().toShortString();
      allDogs.add(label);
      if (dog.squaredDistanceTo(client.player) <= PROBE_RADIUS * PROBE_RADIUS) {
        nearbyDogs.add(label);
      }
    }
    DogsUnleashed.log.info(
        "[PetDebug/Client] {} ticks after summoning {}: clientPlayerPos={} dogsWithin{}Blocks={} allDogsInClientWorld({})={}",
        PROBE_DELAY_TICKS,
        petName,
        client.player.getBlockPos().toShortString(),
        (int) PROBE_RADIUS,
        nearbyDogs,
        allDogs.size(),
        allDogs.size() > 20 ? allDogs.subList(0, 20) + "..." : allDogs);
  }
}
