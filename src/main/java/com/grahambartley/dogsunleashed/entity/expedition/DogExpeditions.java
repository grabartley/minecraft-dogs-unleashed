package com.grahambartley.dogsunleashed.entity.expedition;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.advancement.DogExpeditionReturnedCriterion;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogExpeditions {

  public static final int WHEEL_ACTION_ID = 0;
  public static final String WHEEL_LABEL_KEY = "command.dogs-unleashed.explore";
  public static final String DEPARTED_KEY = "message.dogs-unleashed.expedition.departed";
  public static final String RETURNED_KEY = "message.dogs-unleashed.expedition.returned";
  public static final String AWAY_KEY = "message.dogs-unleashed.expedition.away";

  private DogExpeditions() {}

  public static void registerWheelAction() {
    DogWheelSubActions.REGISTERED.register(
        new DogWheelSubAction(
            WHEEL_ACTION_ID,
            WHEEL_LABEL_KEY,
            Identifier.ofVanilla("filled_map"),
            DogExpeditions::refusalKey,
            DogExpeditions::send));
  }

  public static @Nullable String refusalKey(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    return ExpeditionEligibility.refusalKey(
            dog.isUndead(),
            dog.hasPassengers() || dog.hasVehicle(),
            dog.getSleepController().isAsleep() || dog.getSleepController().isCommandedToSleep(),
            dog.isSitting() || dog.isInSittingPose(),
            dog.isLeashed())
        .orElse(null);
  }

  public static boolean isAway(final MinecraftServer server, final PetData pet) {
    return Expeditions.get(server).isAway(pet.getPetId());
  }

  public static Optional<Expedition> send(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    if (refusalKey(dog, player) != null || !(dog.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final MinecraftServer server = world.getServer();
    final ServerWorld overworld = server.getOverworld();
    final ExpeditionRoute route =
        ExpeditionRoute.plan(
            dog.getBlockX(), dog.getBlockZ(), new Random(world.getRandom().nextLong()));
    final ExpeditionRoute.Stop find = route.furthest();
    final NbtCompound snapshot = dog.writeNbt(new NbtCompound());
    final Expedition expedition =
        new Expedition(
            dog.getUuid(),
            player.getUuid(),
            EntityType.getId(dog.getType()),
            snapshot,
            world.getRegistryKey().getValue(),
            find.x(),
            find.z(),
            ExpeditionSchedule.returnTimeOfDay(overworld.getTimeOfDay()),
            overworld.getTime());
    if (!Expeditions.get(server).add(expedition)) {
      return Optional.empty();
    }
    puff(world, dog.getPos());
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
    if (!Expeditions.get(server).remove(expedition.pet())) {
      return Optional.empty();
    }
    final EntityType<?> type = Registries.ENTITY_TYPE.get(expedition.entityType());
    if (!(type.create(world) instanceof UnleashedDogEntity dog)) {
      DogsUnleashed.log.warn(
          "[Expedition] Could not rebuild {} as {}", expedition.pet(), expedition.entityType());
      return Optional.empty();
    }
    dog.readNbt(expedition.dog());
    final Vec3d spot =
        Optional.ofNullable(
                PetLocationService.findSafeSummonPosition(world, owner.getBlockPos(), dog))
            .orElse(owner.getPos());
    dog.refreshPositionAndAngles(spot.x, spot.y, spot.z, owner.getYaw() + 180.0f, 0.0f);
    discardStaleTwins(server, expedition);
    dog.getCommandController().apply(DogCommand.FOLLOW);
    world.spawnEntity(dog);
    puff(world, spot);

    final ServerWorld origin = originWorld(server, expedition);
    final BlockPos findSpot =
        new BlockPos(expedition.findX(), origin.getSeaLevel(), expedition.findZ());
    final List<ItemStack> finds = ExpeditionFinds.roll(origin, findSpot);
    final List<ItemStack> drops = new ArrayList<>(finds);
    drops.add(ExpeditionFinds.map(origin, findSpot));
    drops.forEach(stack -> dropAtFeet(owner, stack));

    final PetData pet = PetManager.get(server).getPetByEntityId(dog.getUuid());
    if (pet != null) {
      pet.setDimension(world.getRegistryKey().getValue().toString());
      pet.setLastKnownPosition(BlockPos.ofFloored(spot));
      PetManager.get(server).updatePet(pet);
    }
    owner.sendMessage(Text.translatable(RETURNED_KEY, PetNames.tamedNameOf(dog)), true);
    DogExpeditionReturnedCriterion.INSTANCE.trigger(owner, ExpeditionFinds.isRare(finds));
    return Optional.of(dog);
  }

  private static ServerWorld originWorld(
      final MinecraftServer server, final Expedition expedition) {
    final ServerWorld origin =
        server.getWorld(RegistryKey.of(RegistryKeys.WORLD, expedition.originDimension()));
    return origin != null ? origin : server.getOverworld();
  }

  private static void discardStaleTwins(final MinecraftServer server, final Expedition expedition) {
    for (final ServerWorld candidate : server.getWorlds()) {
      final Entity existing = candidate.getEntity(expedition.pet());
      if (existing != null) {
        DogsUnleashed.log.warn(
            "[Expedition] Removing stale copy of {} from {}",
            expedition.pet(),
            candidate.getRegistryKey().getValue());
        existing.discard();
      }
    }
  }

  private static void dropAtFeet(final ServerPlayerEntity owner, final ItemStack stack) {
    final ItemEntity item =
        new ItemEntity(
            owner.getServerWorld(), owner.getX(), owner.getY(), owner.getZ(), stack, 0, 0, 0);
    item.setToDefaultPickupDelay();
    owner.getServerWorld().spawnEntity(item);
  }

  private static void puff(final ServerWorld world, final Vec3d pos) {
    world.spawnParticles(ParticleTypes.POOF, pos.x, pos.y + 0.4, pos.z, 12, 0.3, 0.3, 0.3, 0.02);
  }
}
