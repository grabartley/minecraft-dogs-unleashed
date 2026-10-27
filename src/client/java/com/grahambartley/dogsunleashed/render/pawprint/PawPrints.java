package com.grahambartley.dogsunleashed.render.pawprint;

import com.grahambartley.dogsunleashed.entity.DogMud;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public final class PawPrints {

  private static final PawPrintTrail TRAIL = new PawPrintTrail();
  private static ClientWorld lastWorld;

  private PawPrints() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(PawPrints::tick);
    WorldRenderEvents.AFTER_ENTITIES.register(context -> PawPrintRenderer.render(context, TRAIL));
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TRAIL.clear());
  }

  private static void tick(final MinecraftClient client) {
    final ClientWorld world = client.world;
    if (world != lastWorld) {
      TRAIL.clear();
      lastWorld = world;
    }
    if (world == null || client.isPaused()) {
      return;
    }
    TRAIL.advance();
    final Set<Integer> seen = new HashSet<>();
    for (final Entity entity : world.getEntities()) {
      if (entity instanceof UnleashedDogEntity dog) {
        seen.add(dog.getId());
        step(dog);
      }
    }
    TRAIL.keepOnly(seen);
  }

  private static void step(final UnleashedDogEntity dog) {
    if (!dog.getComponents().get(DogMud.class).isMuddy()
        || !dog.isOnGround()
        || dog.isTouchingWater()
        || dog.hasVehicle()
        || !PawPrintGround.holdsPrint(dog.getWorld(), dog.getX(), dog.getY(), dog.getZ())) {
      TRAIL.forget(dog.getId());
      return;
    }
    TRAIL.step(dog.getId(), dog.getX(), dog.getY(), dog.getZ(), dog.getBodyYaw(), dog.getWidth());
  }
}
