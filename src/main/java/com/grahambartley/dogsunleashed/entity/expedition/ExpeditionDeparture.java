package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.Optional;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class ExpeditionDeparture {

  public static final String DEPARTED_KEY = "message.dogs-unleashed.expedition.departed";

  private ExpeditionDeparture() {}

  public static Optional<Expedition> send(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    if (DogExpeditions.refusalKey(dog, player) != null
        || !(dog.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final MinecraftServer server = world.getServer();
    final ServerWorld overworld = server.getOverworld();
    final ExpeditionRoute.Stop find =
        ExpeditionRoute.plan(
                dog.getBlockX(), dog.getBlockZ(), new Random(world.getRandom().nextLong()))
            .furthest();
    final Expedition expedition =
        new Expedition(
            dog.getUuid(),
            player.getUuid(),
            EntityType.getId(dog.getType()),
            dog.writeNbt(new NbtCompound()),
            world.getRegistryKey().getValue(),
            find.x(),
            find.z(),
            ExpeditionSchedule.returnTimeOfDay(overworld.getTimeOfDay()),
            overworld.getTime());
    PetLocationSyncListener.recordLocation(dog, world);
    if (!Expeditions.get(server).add(expedition)) {
      return Optional.empty();
    }
    DogExpeditions.puff(world, dog.getPos());
    player.sendMessage(Text.translatable(DEPARTED_KEY, PetNames.tamedNameOf(dog)), true);
    DogsUnleashed.log.info(
        "[Expedition] {} set off from {} towards {}, {}",
        dog.getUuid(),
        dog.getBlockPos(),
        find.x(),
        find.z());
    dog.discard();
    return Optional.of(expedition);
  }
}
