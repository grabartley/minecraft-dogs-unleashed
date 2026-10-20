package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.advancement.DogExpeditionReturnedCriterion;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogRecreation;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
    final ServerWorld world = owner.getServerWorld();
    final Expeditions expeditions = Expeditions.get(server);
    if (!expeditions.isAway(expedition.pet())) {
      return Optional.empty();
    }
    final UnleashedDogEntity dog =
        Registries.ENTITY_TYPE
            .getOrEmpty(expedition.entityType())
            .map(type -> DogRecreation.fromSnapshot(type, expedition.dog(), world))
            .orElse(null);
    if (dog == null) {
      DogsUnleashed.log.error(
          "[Expedition] Could not rebuild {} as {}, keeping it away",
          expedition.pet(),
          expedition.entityType());
      return Optional.empty();
    }
    final Vec3d spot =
        Optional.ofNullable(
                PetLocationService.findSafeSummonPosition(world, owner.getBlockPos(), dog))
            .orElse(owner.getPos());
    dog.refreshPositionAndAngles(spot.x, spot.y, spot.z, owner.getYaw() + 180.0f, 0.0f);
    dog.setVelocity(Vec3d.ZERO);
    dog.fallDistance = 0.0f;
    dog.setFireTicks(0);
    dog.getCommandController().apply(DogCommand.FOLLOW);
    server
        .getWorlds()
        .forEach(candidate -> DogRecreation.discardStaleTwin(candidate, dog.getUuid(), null));
    expeditions.remove(expedition.pet());
    if (!world.spawnEntity(dog)) {
      expeditions.add(expedition);
      DogsUnleashed.log.error("[Expedition] Could not spawn {}, keeping it away", dog.getUuid());
      return Optional.empty();
    }
    DogExpeditions.puff(world, spot);
    welcome(server, expedition, owner, dog, spot);
    return Optional.of(dog);
  }

  private static void welcome(
      final MinecraftServer server,
      final Expedition expedition,
      final ServerPlayerEntity owner,
      final UnleashedDogEntity dog,
      final Vec3d spot) {
    final ServerWorld origin = originWorld(server, expedition);
    final BlockPos findSpot =
        new BlockPos(expedition.findX(), origin.getSeaLevel(), expedition.findZ());
    final List<ItemStack> finds = ExpeditionFinds.roll(origin, findSpot);
    final List<ItemStack> drops = new ArrayList<>(finds);
    drops.add(ExpeditionFinds.map(origin, findSpot));
    drops.forEach(stack -> dropAtFeet(owner, stack));

    final PetData pet = PetManager.get(server).getPetByEntityId(dog.getUuid());
    if (pet != null) {
      pet.setDimension(owner.getServerWorld().getRegistryKey().getValue().toString());
      pet.setLastKnownPosition(BlockPos.ofFloored(spot));
      PetManager.get(server).updatePet(pet);
    }
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
