package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.advancement.DogExpeditionReturnedCriterion;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayArrival;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class ExpeditionHomecoming {

  public static final String RETURNED_KEY = "message.dogs-unleashed.expedition.returned";

  private ExpeditionHomecoming() {}

  public static void returnDue(final MinecraftServer server) {
    final ServerWorld overworld = server.getOverworld();
    for (final Expedition expedition :
        Expeditions.get(server).due(overworld.getTimeOfDay(), overworld.getTime())) {
      final ServerPlayerEntity owner = server.getPlayerManager().getPlayer(expedition.owner());
      if (owner != null && owner.isAlive() && !owner.isSpectator()) {
        returnHome(expedition, owner);
      }
    }
  }

  public static Optional<UnleashedDogEntity> returnHome(
      final Expedition expedition, final ServerPlayerEntity owner) {
    final MinecraftServer server = owner.getServer();
    final Expeditions expeditions = Expeditions.get(server);
    if (!expeditions.isAway(expedition.pet())) {
      return Optional.empty();
    }
    final Optional<UnleashedDogEntity> dog =
        AwayArrival.arrive(
            expedition.pet(),
            expedition.entityType(),
            expedition.dog(),
            owner,
            DogCommand.FOLLOW,
            () -> expeditions.remove(expedition.pet()),
            () -> expeditions.add(expedition));
    dog.ifPresent(home -> welcome(server, expedition, owner, home));
    return dog;
  }

  private static void welcome(
      final MinecraftServer server,
      final Expedition expedition,
      final ServerPlayerEntity owner,
      final UnleashedDogEntity dog) {
    final ServerWorld origin = originWorld(server, expedition);
    final BlockPos findSpot =
        new BlockPos(expedition.findX(), origin.getSeaLevel(), expedition.findZ());
    final List<ItemStack> finds = ExpeditionFinds.roll(origin, findSpot);
    final List<ItemStack> drops = new ArrayList<>(finds);
    drops.add(ExpeditionFinds.map(origin, findSpot));
    drops.forEach(stack -> dropAtFeet(owner, stack));
    owner.sendMessage(Text.translatable(RETURNED_KEY, PetNames.tamedNameOf(dog)), true);
    DogExpeditionReturnedCriterion.INSTANCE.trigger(owner, ExpeditionFinds.isRare(finds));
  }

  private static ServerWorld originWorld(
      final MinecraftServer server, final Expedition expedition) {
    final ServerWorld origin =
        server.getWorld(RegistryKey.of(RegistryKeys.WORLD, expedition.originDimension()));
    return origin != null ? origin : server.getOverworld();
  }

  private static void dropAtFeet(final ServerPlayerEntity owner, final ItemStack stack) {
    final ItemEntity item =
        new ItemEntity(
            owner.getServerWorld(), owner.getX(), owner.getY(), owner.getZ(), stack, 0, 0, 0);
    item.setToDefaultPickupDelay();
    owner.getServerWorld().spawnEntity(item);
  }
}
