package com.grahambartley.network;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import com.grahambartley.pet.PetManagerPreferencesState;
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

public final class ModNetworking {

  public static final Identifier SET_PET_NAME_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "set_pet_name");
  public static final Identifier SUMMON_PET_ID = Identifier.of(DogsUnleashed.MOD_ID, "summon_pet");
  public static final Identifier REQUEST_PETS_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_pets");
  public static final Identifier REQUEST_PET_MANAGER_STATE_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_pet_manager_state");
  public static final Identifier SYNC_PETS_ID = Identifier.of(DogsUnleashed.MOD_ID, "sync_pets");
  public static final Identifier SYNC_PET_MANAGER_STATE_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_pet_manager_state");
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

  public record RequestPetManagerStatePayload() implements CustomPayload {

    public static final CustomPayload.Id<RequestPetManagerStatePayload> ID =
        new CustomPayload.Id<>(REQUEST_PET_MANAGER_STATE_ID);
    public static final PacketCodec<RegistryByteBuf, RequestPetManagerStatePayload> CODEC =
        PacketCodec.unit(new RequestPetManagerStatePayload());

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

  public record SyncPetManagerStatePayload(
      String breedFilter, String aliveFilter, List<PetSyncData> pets) implements CustomPayload {

    public static final CustomPayload.Id<SyncPetManagerStatePayload> ID =
        new CustomPayload.Id<>(SYNC_PET_MANAGER_STATE_ID);
    public static final PacketCodec<RegistryByteBuf, SyncPetManagerStatePayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING,
            SyncPetManagerStatePayload::breedFilter,
            PacketCodecs.STRING,
            SyncPetManagerStatePayload::aliveFilter,
            PetSyncData.CODEC.collect(PacketCodecs.toList()),
            SyncPetManagerStatePayload::pets,
            SyncPetManagerStatePayload::new);

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
    PayloadTypeRegistry.playC2S()
        .register(RequestPetManagerStatePayload.ID, RequestPetManagerStatePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(SyncPetsPayload.ID, SyncPetsPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(SyncPetManagerStatePayload.ID, SyncPetManagerStatePayload.CODEC);
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
    ServerPlayNetworking.registerGlobalReceiver(
        RequestPetManagerStatePayload.ID, ModNetworking::handleRequestPetManagerState);
  }

  private static void handleSetPetName(
      SetPetNamePayload payload, ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final Entity entity = findEntityByUuid(world, payload.petId());
              if (entity instanceof UnleashedDogEntity dog && dog.isOwner(player)) {
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
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final PetManager petManager = PetManager.get(world.getServer());
              final PetData petData = petManager.getPet(player.getUuid(), payload.petId());

              if (petData != null && petData.isAlive()) {
                final Entity entity = findEntityByUuid(world, payload.petId());
                if (entity instanceof UnleashedDogEntity dog) {
                  dog.wakeUp();
                  dog.teleport(
                      world,
                      player.getX(),
                      player.getY(),
                      player.getZ(),
                      java.util.Set.of(),
                      dog.getYaw(),
                      dog.getPitch());
                  dog.setSitting(false);
                  petData.setLastKnownPosition(player.getBlockPos());
                  petManager.updatePet(petData);
                }
              }
            });
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
              final PetManagerPreferencesState preferencesState =
                  PetManagerPreferencesState.get(world.getServer());
              final String breedFilter =
                  payload.breedFilter().isEmpty() ? null : payload.breedFilter();
              final Boolean aliveFilter = payload.filterAlive() ? payload.aliveValue() : null;
              preferencesState.setPreferences(
                  player.getUuid(),
                  payload.breedFilter(),
                  aliveFilterToPreferenceValue(aliveFilter));

              final List<PetData> pets =
                  petManager.getPetsByOwnerFiltered(
                      player.getUuid(), breedFilter, aliveFilter, payload.searchQuery());
              final List<PetSyncData> syncData = syncPetData(world, petManager, pets);
              ServerPlayNetworking.send(player, new SyncPetsPayload(syncData));
            });
  }

  private static void handleRequestPetManagerState(
      RequestPetManagerStatePayload payload, ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final PetManagerPreferencesState preferencesState =
                  PetManagerPreferencesState.get(world.getServer());
              final PetManagerPreferencesState.PetManagerPreferences preferences =
                  preferencesState.getPreferences(player.getUuid());
              final String breedFilter = preferences.breedFilter();
              final Boolean aliveFilter = preferenceValueToAliveFilter(preferences.aliveFilter());
              final List<PetData> pets =
                  PetManager.get(world.getServer())
                      .getPetsByOwnerFiltered(player.getUuid(), breedFilter, aliveFilter, "");
              final List<PetSyncData> syncData =
                  syncPetData(world, PetManager.get(world.getServer()), pets);
              ServerPlayNetworking.send(
                  player,
                  new SyncPetManagerStatePayload(
                      breedFilter, aliveFilterToPreferenceValue(aliveFilter), syncData));
            });
  }

  private static String aliveFilterToPreferenceValue(final Boolean aliveFilter) {
    if (aliveFilter == null) {
      return "ALL";
    }
    return aliveFilter ? "ALIVE" : "DECEASED";
  }

  private static Boolean preferenceValueToAliveFilter(final String aliveFilter) {
    return switch (aliveFilter) {
      case "ALIVE" -> true;
      case "DECEASED" -> false;
      default -> null;
    };
  }

  private static List<PetSyncData> syncPetData(
      final ServerWorld world, final PetManager petManager, final List<PetData> pets) {
    for (final PetData pet : pets) {
      if (pet.isAlive()) {
        final Entity entity = findEntityByUuid(world, pet.getPetId());
        if (entity instanceof UnleashedDogEntity dog) {
          pet.setHealth(dog.getHealth());
          pet.setLastKnownPosition(dog.getBlockPos());
          pet.setDimension(world.getRegistryKey().getValue().toString());
          pet.syncAppearanceFrom(dog);
          petManager.updatePet(pet);
        }
      }
    }
    return pets.stream().map(PetSyncData::from).toList();
  }

  private static Entity findEntityByUuid(ServerWorld world, UUID uuid) {
    return world.getEntity(uuid);
  }

  public static void sendOpenNamingScreen(
      ServerPlayerEntity player, UUID petId, String breedType, String suggestedName) {
    ServerPlayNetworking.send(player, new OpenNamingScreenPayload(petId, breedType, suggestedName));
  }
}
