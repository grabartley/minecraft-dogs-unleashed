package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayArrival;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class DeliveryDeparture {

  public static final String DEPARTED_KEY = "message.dogs-unleashed.delivery.departed";

  private DeliveryDeparture() {}

  public static Optional<Delivery> send(
      final UnleashedDogEntity dog,
      final ServerPlayerEntity owner,
      final ServerPlayerEntity recipient) {
    if (!dog.isOwner(owner)
        || recipient == owner
        || !DogDeliveries.isPresent(recipient)
        || DogDeliveries.refusalKey(dog, owner) != null
        || !(dog.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final long ticks = legTicks(dog, recipient);
    final long now = world.getServer().getOverworld().getTime();
    final Delivery delivery =
        new Delivery(
            dog.getUuid(),
            owner.getUuid(),
            recipient.getUuid(),
            EntityType.getId(dog.getType()),
            dog.writeNbt(new NbtCompound()),
            DeliveryLeg.TO_RECIPIENT,
            ticks,
            now + ticks);
    PetLocationSyncListener.recordLocation(dog, world);
    if (!Deliveries.get(world.getServer()).add(delivery)) {
      return Optional.empty();
    }
    AwayArrival.puff(world, dog.getPos());
    owner.sendMessage(
        Text.translatable(
            DEPARTED_KEY, PetNames.tamedNameOf(dog), recipient.getDisplayName().getString()),
        true);
    DogsUnleashed.log.info(
        "[Delivery] {} set off to {} ({} ticks)", dog.getUuid(), recipient.getUuid(), ticks);
    dog.discard();
    return Optional.of(delivery);
  }

  static long legTicks(final Entity from, final Entity to) {
    final double distance =
        DeliveryTravel.distance(
            from.getX(),
            from.getZ(),
            from.getWorld().getDimension().coordinateScale(),
            to.getX(),
            to.getZ(),
            to.getWorld().getDimension().coordinateScale());
    return DeliveryTravel.legTicks(distance, from.getWorld() != to.getWorld());
  }
}
