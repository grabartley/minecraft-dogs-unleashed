package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class DeliveryLifecycle {

  public static final String WAITING_TAG = "dogs-unleashed.delivery_waiting";

  private DeliveryLifecycle() {}

  public static void unloaded(final UnleashedDogEntity dog, final ServerWorld world) {
    if (!DeliveryWaitRules.isChunkUnload(dog.getRemovalReason())) {
      return;
    }
    final MinecraftServer server = world.getServer();
    final Deliveries deliveries = Deliveries.get(server);
    deliveries
        .of(dog.getUuid())
        .filter(delivery -> delivery.leg() == DeliveryLeg.WAITING)
        .ifPresent(
            waiting -> {
              final NbtCompound snapshot =
                  DeliverySnapshots.withoutTag(dog.writeNbt(new NbtCompound()), WAITING_TAG);
              deliveries.replace(
                  waiting.homeward(snapshot, server.getOverworld().getTime(), waiting.legTicks()));
              DogsUnleashed.log.info(
                  "[Delivery] {} unloaded while waiting, heading home", dog.getUuid());
            });
  }

  public static boolean isStaleCopy(final MinecraftServer server, final Entity dog) {
    return DeliveryWaitRules.isStaleCopy(
        dog.getCommandTags().contains(WAITING_TAG),
        Deliveries.get(server).of(dog.getUuid()).map(Delivery::leg));
  }

  public static void died(final MinecraftServer server, final UnleashedDogEntity dog) {
    final Deliveries deliveries = Deliveries.get(server);
    if (deliveries.of(dog.getUuid()).filter(d -> d.leg() == DeliveryLeg.WAITING).isPresent()) {
      deliveries.remove(dog.getUuid());
      DogsUnleashed.log.info("[Delivery] {} died on a delivery", dog.getUuid());
    }
  }
}
