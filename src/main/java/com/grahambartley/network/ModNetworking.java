package com.grahambartley.network;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ModNetworking {
  public static final Identifier SET_PET_NAME_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "set_pet_name");
  public static final Identifier SUMMON_PET_ID = Identifier.of(DogsUnleashed.MOD_ID, "summon_pet");
  public static final Identifier REQUEST_PETS_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_pets");
  public static final Identifier SYNC_PETS_ID = Identifier.of(DogsUnleashed.MOD_ID, "sync_pets");
  public static final Identifier OPEN_NAMING_SCREEN_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_naming_screen");

  public record SetPetNamePayload(UUID petId, String name) implements CustomPayload {

    public static final CustomPayload.Id<SetPetNamePayload> ID =
        new CustomPayload.Id<>(SET_PET_NAME_ID);
    public static final PacketCodec<RegistryByteBuf, SetPetNamePayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            SetPetNamePayload::petId,
            PacketCodecs.STRING,
            SetPetNamePayload::name,
            SetPetNamePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public record SummonPetPayload(UUID petId) implements CustomPayload {

    public static final CustomPayload.Id<SummonPetPayload> ID =
        new CustomPayload.Id<>(SUMMON_PET_ID);
    public static final PacketCodec<RegistryByteBuf, SummonPetPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            SummonPetPayload::petId,
            SummonPetPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public record RequestPetsPayload(
      String breedFilter, boolean filterAlive, boolean aliveValue, String searchQuery)
      implements CustomPayload {

    public static final CustomPayload.Id<RequestPetsPayload> ID =
        new CustomPayload.Id<>(REQUEST_PETS_ID);
    public static final PacketCodec<RegistryByteBuf, RequestPetsPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING,
            RequestPetsPayload::breedFilter,
            PacketCodecs.BOOL,
            RequestPetsPayload::filterAlive,
            PacketCodecs.BOOL,
            RequestPetsPayload::aliveValue,
            PacketCodecs.STRING,
            RequestPetsPayload::searchQuery,
            RequestPetsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public record SyncPetsPayload(List<PetSyncData> pets) implements CustomPayload {

    public static final CustomPayload.Id<SyncPetsPayload> ID = new CustomPayload.Id<>(SYNC_PETS_ID);
    public static final PacketCodec<RegistryByteBuf, SyncPetsPayload> CODEC =
        PacketCodec.tuple(
            PetSyncData.CODEC.collect(PacketCodecs.toList()),
            SyncPetsPayload::pets,
            SyncPetsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public record PetSyncData(
      String petId,
      String breedType,
      String name,
      float health,
      float maxHealth,
      int posX,
      int posY,
      int posZ,
      String dimension,
      boolean alive,
      boolean baby,
      int collarColor,
      int coatVariant,
      int huskyEyeVariant) {

    public static final PacketCodec<RegistryByteBuf, PetSyncData> CODEC =
        PacketCodec.of(PetSyncData::write, PetSyncData::read);

    public static PetSyncData from(PetData petData) {
      return new PetSyncData(
          petData.getPetId().toString(),
          petData.getBreedType(),
          petData.getName(),
          petData.getHealth(),
          petData.getMaxHealth(),
          petData.getLastKnownPosition().getX(),
          petData.getLastKnownPosition().getY(),
          petData.getLastKnownPosition().getZ(),
          petData.getDimension(),
          petData.isAlive(),
          petData.isBaby(),
          petData.getCollarColorId(),
          petData.getCoatVariant(),
          petData.getHuskyEyeVariant());
    }

    private void write(RegistryByteBuf buf) {
      buf.writeString(petId);
      buf.writeString(breedType);
      buf.writeString(name);
      buf.writeFloat(health);
      buf.writeFloat(maxHealth);
      buf.writeInt(posX);
      buf.writeInt(posY);
      buf.writeInt(posZ);
      buf.writeString(dimension);
      buf.writeBoolean(alive);
      buf.writeBoolean(baby);
      buf.writeInt(collarColor);
      buf.writeInt(coatVariant);
      buf.writeInt(huskyEyeVariant);
    }

    private static PetSyncData read(RegistryByteBuf buf) {
      return new PetSyncData(
          buf.readString(),
          buf.readString(),
          buf.readString(),
          buf.readFloat(),
          buf.readFloat(),
          buf.readInt(),
          buf.readInt(),
          buf.readInt(),
          buf.readString(),
          buf.readBoolean(),
          buf.readBoolean(),
          buf.readInt(),
          buf.readInt(),
          buf.readInt());
    }
  }

  public record OpenNamingScreenPayload(UUID petId, String breedType, String suggestedName)
      implements CustomPayload {

    public static final CustomPayload.Id<OpenNamingScreenPayload> ID =
        new CustomPayload.Id<>(OPEN_NAMING_SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, OpenNamingScreenPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            OpenNamingScreenPayload::petId,
            PacketCodecs.STRING,
            OpenNamingScreenPayload::breedType,
            PacketCodecs.STRING,
            OpenNamingScreenPayload::suggestedName,
            OpenNamingScreenPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public static void registerPayloads() {
    PayloadTypeRegistry.playC2S().register(SetPetNamePayload.ID, SetPetNamePayload.CODEC);
    PayloadTypeRegistry.playC2S().register(SummonPetPayload.ID, SummonPetPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(RequestPetsPayload.ID, RequestPetsPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(SyncPetsPayload.ID, SyncPetsPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(OpenNamingScreenPayload.ID, OpenNamingScreenPayload.CODEC);
  }

  public static void registerServerReceivers() {
    ServerPlayNetworking.registerGlobalReceiver(
        SetPetNamePayload.ID, ModNetworking::handleSetPetName);
    ServerPlayNetworking.registerGlobalReceiver(
        SummonPetPayload.ID, ModNetworking::handleSummonPet);
    ServerPlayNetworking.registerGlobalReceiver(
        RequestPetsPayload.ID, ModNetworking::handleRequestPets);
  }

  private static void handleSetPetName(
      SetPetNamePayload payload, ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              if (world.getEntity(payload.petId()) instanceof UnleashedDogEntity dog
                  && dog.isOwner(player)) {
                dog.setCustomName(Text.literal(payload.name()));
                dog.setCustomNameVisible(true);

                final PetManager petManager = PetManager.get(world.getServer());
                final PetData petData = petManager.getPetByEntityId(payload.petId());
                if (petData != null) {
                  petData.setName(payload.name());
                  petManager.updatePet(petData);
                }
              }
            });
  }

  private static void handleSummonPet(
      SummonPetPayload payload, ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld playerWorld = player.getServerWorld();
    final UUID playerId = player.getUuid();

    playerWorld
        .getServer()
        .execute(
            () -> {
              final PetManager petManager = PetManager.get(playerWorld.getServer());
              final PetData petData = petManager.getPet(player.getUuid(), payload.petId());

              if (petData != null && petData.isAlive()) {
                UnleashedDogEntity.findAndLoadWithTicket(
                    playerWorld.getServer(),
                    petData,
                    dog -> {
                      final ServerPlayerEntity currentPlayer =
                          playerWorld.getServer().getPlayerManager().getPlayer(playerId);
                      if (currentPlayer == null) return;

                      final ServerWorld currentPlayerWorld = currentPlayer.getServerWorld();
                      if (dog.getWorld() != currentPlayerWorld) {
                        dog.followOwnerToDimension(currentPlayer, currentPlayerWorld);
                        petData.setLastKnownPosition(currentPlayer.getBlockPos());
                      } else {
                        final Vec3d summonPos =
                            findSafeSummonPosition(
                                currentPlayerWorld, currentPlayer.getBlockPos(), dog);
                        dog.wakeUp();
                        dog.setSitting(false);
                        dog.teleport(
                            currentPlayerWorld,
                            summonPos.x,
                            summonPos.y,
                            summonPos.z,
                            java.util.Set.of(),
                            dog.getYaw(),
                            dog.getPitch());
                        refreshEntityTracking(currentPlayerWorld, dog, currentPlayer);
                        petData.setLastKnownPosition(BlockPos.ofFloored(summonPos));
                      }

                      petData.setDimension(
                          currentPlayerWorld.getRegistryKey().getValue().toString());
                      petManager.updatePet(petData);
                    },
                    () -> {});
              }
            });
  }

  private static Vec3d findSafeSummonPosition(
      ServerWorld world, BlockPos center, UnleashedDogEntity dog) {
    for (final BlockPos basePos : BlockPos.iterateOutwards(center, 2, 1, 2)) {
      if (!isSafeSummonBase(world, basePos)) {
        continue;
      }

      final Vec3d candidate = new Vec3d(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5);
      final net.minecraft.util.math.Box box =
          dog.getBoundingBox()
              .offset(candidate.x - dog.getX(), candidate.y - dog.getY(), candidate.z - dog.getZ());
      if (world.getBlockCollisions(dog, box).iterator().hasNext()) {
        continue;
      }
      return candidate;
    }

    return new Vec3d(playerSafeX(center), center.getY(), playerSafeZ(center));
  }

  private static boolean isSafeSummonBase(ServerWorld world, BlockPos basePos) {
    final var stateAtPos = world.getBlockState(basePos);
    final var stateAbove = world.getBlockState(basePos.up());
    final var stateBelow = world.getBlockState(basePos.down());

    final boolean openAtFeet = stateAtPos.isAir() || stateAtPos.isReplaceable();
    final boolean openAtHead = stateAbove.isAir() || stateAbove.isReplaceable();
    final boolean stableFloor = stateBelow.isSolidBlock(world, basePos.down());
    final boolean notInFluid =
        !stateAtPos.getFluidState().isStill() && !stateAbove.getFluidState().isStill();

    return openAtFeet && openAtHead && stableFloor && notInFluid;
  }

  private static double playerSafeX(BlockPos center) {
    return center.getX() + 0.5;
  }

  private static double playerSafeZ(BlockPos center) {
    return center.getZ() + 0.5;
  }

  private static void refreshEntityTracking(
      ServerWorld world, Entity entity, ServerPlayerEntity player) {
    final var chunkManager = world.getChunkManager();
    chunkManager.unloadEntity(entity);
    chunkManager.loadEntity(entity);
    chunkManager.updatePosition(player);
  }

  private static void handleRequestPets(
      RequestPetsPayload payload, ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final PetManager petManager = PetManager.get(world.getServer());
              List<PetData> pets;

              if (!payload.searchQuery().isEmpty()) {
                pets = petManager.searchPetsByName(player.getUuid(), payload.searchQuery());
              } else {
                final Boolean aliveFilter = payload.filterAlive() ? payload.aliveValue() : null;
                final String breedFilter =
                    payload.breedFilter().isEmpty() ? null : payload.breedFilter();
                pets =
                    petManager.getPetsByOwnerFiltered(player.getUuid(), breedFilter, aliveFilter);
              }

              for (final PetData pet : pets) {
                if (pet.isAlive()) {
                  final UnleashedDogEntity dog =
                      UnleashedDogEntity.findAndLoad(world.getServer(), pet);
                  if (dog != null) {
                    pet.setHealth(dog.getHealth());
                    pet.setLastKnownPosition(dog.getBlockPos());
                    pet.setDimension(
                        ((ServerWorld) dog.getWorld()).getRegistryKey().getValue().toString());
                    pet.syncAppearanceFrom(dog);
                    petManager.updatePet(pet);
                  }
                }
              }

              final List<PetSyncData> syncData = pets.stream().map(PetSyncData::from).toList();
              ServerPlayNetworking.send(player, new SyncPetsPayload(syncData));
            });
  }

  public static void sendOpenNamingScreen(
      ServerPlayerEntity player, UUID petId, String breedType, String suggestedName) {
    ServerPlayNetworking.send(player, new OpenNamingScreenPayload(petId, breedType, suggestedName));
  }
}
