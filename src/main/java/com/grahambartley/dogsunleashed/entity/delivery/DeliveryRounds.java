package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayArrival;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.network.handler.OwnerNameResolver;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class DeliveryRounds {

  public static final String ARRIVED_KEY = "message.dogs-unleashed.delivery.arrived";
  public static final String DELIVERED_KEY = "message.dogs-unleashed.delivery.delivered";
  public static final String RETURNED_KEY = "message.dogs-unleashed.delivery.returned";
  public static final String UNDELIVERED_KEY = "message.dogs-unleashed.delivery.undelivered";

  public static final String WAITING_TAG = "dogs-unleashed.delivery_waiting";

  private DeliveryRounds() {}

  public static void run(final MinecraftServer server) {
    final long now = server.getOverworld().getTime();
    for (final Delivery delivery : Deliveries.get(server).all()) {
      advance(server, delivery, now);
    }
  }

  public static void advance(
      final MinecraftServer server, final Delivery delivery, final long now) {
    switch (delivery.leg()) {
      case TO_RECIPIENT -> {
        if (delivery.isLegOver(now)) {
          reachRecipient(server, delivery, now);
        }
      }
      case WAITING -> {
        final UnleashedDogEntity dog = loadedDog(server, delivery.pet());
        if (dog != null && headsHome(server, delivery, dog, now)) {
          sendHome(dog);
        }
      }
      case TO_OWNER -> {
        if (delivery.isLegOver(now)) {
          reachOwner(server, delivery);
        }
      }
    }
  }

  public static void recipientLeft(final MinecraftServer server, final ServerPlayerEntity player) {
    for (final Delivery delivery : Deliveries.get(server).all()) {
      if (delivery.leg() == DeliveryLeg.WAITING && delivery.recipient().equals(player.getUuid())) {
        sendHome(server, delivery.pet());
      }
    }
  }

  public static void sendHome(final MinecraftServer server, final UUID pet) {
    final UnleashedDogEntity dog = loadedDog(server, pet);
    if (dog != null) {
      sendHome(dog);
    }
  }

  public static void unloaded(final UnleashedDogEntity dog, final ServerWorld world) {
    final MinecraftServer server = world.getServer();
    final Deliveries deliveries = Deliveries.get(server);
    deliveries
        .of(dog.getUuid())
        .filter(delivery -> delivery.leg() == DeliveryLeg.WAITING)
        .ifPresent(
            waiting -> {
              final NbtCompound snapshot = dog.writeNbt(new NbtCompound());
              withoutWaitingTag(snapshot);
              deliveries.replace(
                  waiting.homeward(snapshot, server.getOverworld().getTime(), waiting.legTicks()));
              DogsUnleashed.log.info(
                  "[Delivery] {} unloaded while waiting, heading home", dog.getUuid());
            });
  }

  public static boolean isStaleCopy(final MinecraftServer server, final UnleashedDogEntity dog) {
    return dog.getCommandTags().contains(WAITING_TAG)
        && Deliveries.get(server)
            .of(dog.getUuid())
            .map(delivery -> delivery.leg() != DeliveryLeg.WAITING)
            .orElse(true);
  }

  public static void died(final MinecraftServer server, final UnleashedDogEntity dog) {
    if (Deliveries.get(server).remove(dog.getUuid())) {
      DogsUnleashed.log.info("[Delivery] {} died on a delivery", dog.getUuid());
    }
  }

  private static void withoutWaitingTag(final NbtCompound snapshot) {
    final NbtList tags = snapshot.getList("Tags", NbtElement.STRING_TYPE);
    tags.removeIf(tag -> WAITING_TAG.equals(tag.asString()));
  }

  public static Optional<Delivery> sendHome(final UnleashedDogEntity dog) {
    if (dog.isRemoved() || !(dog.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final MinecraftServer server = world.getServer();
    final Deliveries deliveries = Deliveries.get(server);
    final Delivery waiting =
        deliveries
            .of(dog.getUuid())
            .filter(delivery -> delivery.leg() == DeliveryLeg.WAITING)
            .orElse(null);
    if (waiting == null) {
      return Optional.empty();
    }
    final ServerPlayerEntity owner = server.getPlayerManager().getPlayer(waiting.owner());
    final long ticks =
        DogDeliveries.isPresent(owner)
            ? DeliveryDeparture.legTicks(dog, owner)
            : waiting.legTicks();
    final long now = server.getOverworld().getTime();
    PetLocationSyncListener.recordLocation(dog, world);
    dog.removeCommandTag(WAITING_TAG);
    final Delivery homeward = waiting.homeward(dog.writeNbt(new NbtCompound()), now, ticks);
    deliveries.replace(homeward);
    AwayArrival.puff(world, dog.getPos());
    DogsUnleashed.log.info("[Delivery] {} heading home ({} ticks)", dog.getUuid(), ticks);
    dog.discard();
    return Optional.of(homeward);
  }

  private static void reachRecipient(
      final MinecraftServer server, final Delivery delivery, final long now) {
    final Deliveries deliveries = Deliveries.get(server);
    final ServerPlayerEntity recipient = server.getPlayerManager().getPlayer(delivery.recipient());
    final ServerPlayerEntity owner = server.getPlayerManager().getPlayer(delivery.owner());
    if (!DogDeliveries.isPresent(recipient)) {
      deliveries.replace(delivery.homeward(delivery.dog(), now, delivery.legTicks()));
      if (owner != null) {
        owner.sendMessage(Text.translatable(UNDELIVERED_KEY, nameOf(server, delivery)), true);
      }
      return;
    }
    final Delivery waiting = delivery.waiting(now, DeliveryWaitRules.WAIT_TICKS);
    AwayArrival.arrive(
            delivery.pet(),
            delivery.entityType(),
            delivery.dog(),
            recipient,
            DogCommand.STAY,
            () -> deliveries.replace(waiting),
            () -> deliveries.replace(delivery))
        .ifPresent(
            dog -> {
              dog.addCommandTag(WAITING_TAG);
              final String dogName = PetNames.tamedNameOf(dog);
              final String ownerName = OwnerNameResolver.resolve(server, delivery.owner());
              recipient.sendMessage(Text.translatable(ARRIVED_KEY, dogName, ownerName), false);
              if (owner != null) {
                owner.sendMessage(
                    Text.translatable(
                        DELIVERED_KEY, dogName, recipient.getDisplayName().getString()),
                    true);
              }
            });
  }

  private static void reachOwner(final MinecraftServer server, final Delivery delivery) {
    final ServerPlayerEntity owner = server.getPlayerManager().getPlayer(delivery.owner());
    if (!DogDeliveries.isPresent(owner)) {
      return;
    }
    final Deliveries deliveries = Deliveries.get(server);
    AwayArrival.arrive(
            delivery.pet(),
            delivery.entityType(),
            delivery.dog(),
            owner,
            DogCommand.FOLLOW,
            () -> deliveries.remove(delivery.pet()),
            () -> deliveries.add(delivery))
        .ifPresent(
            dog ->
                owner.sendMessage(
                    Text.translatable(RETURNED_KEY, PetNames.tamedNameOf(dog)), true));
  }

  private static boolean headsHome(
      final MinecraftServer server,
      final Delivery delivery,
      final UnleashedDogEntity dog,
      final long now) {
    final ServerPlayerEntity recipient = server.getPlayerManager().getPlayer(delivery.recipient());
    final boolean present = DogDeliveries.isPresent(recipient);
    final boolean sameWorld = present && recipient.getWorld() == dog.getWorld();
    return DeliveryWaitRules.headsHome(
        present,
        sameWorld,
        sameWorld ? dog.distanceTo(recipient) : Double.MAX_VALUE,
        delivery.isLegOver(now));
  }

  public static @Nullable UnleashedDogEntity loadedDog(
      final MinecraftServer server, final UUID pet) {
    for (final ServerWorld world : server.getWorlds()) {
      if (world.getEntity(pet) instanceof UnleashedDogEntity dog && !dog.isRemoved()) {
        return dog;
      }
    }
    return null;
  }

  private static String nameOf(final MinecraftServer server, final Delivery delivery) {
    final PetData pet = PetManager.get(server).getPetByEntityId(delivery.pet());
    return pet != null ? pet.getName() : "";
  }
}
