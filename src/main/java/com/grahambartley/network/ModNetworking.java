package com.grahambartley.network;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.pet.PetAliveFilter;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetLocationService;
import com.grahambartley.pet.PetManager;
import com.grahambartley.pet.PetManagerPreferencesState;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
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
      UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, String searchQuery)
      implements CustomPayload {

    public static final CustomPayload.Id<RequestPetsPayload> ID =
        new CustomPayload.Id<>(REQUEST_PETS_ID);
    public static final PacketCodec<RegistryByteBuf, RequestPetsPayload> CODEC =
        PacketCodec.of(RequestPetsPayload::write, RequestPetsPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      buf.writeBoolean(this.breedFilter != null);
      if (this.breedFilter != null) {
        buf.writeString(this.breedFilter.serializedId());
      }
      buf.writeString(this.aliveFilter.serializedName());
      buf.writeString(this.searchQuery);
    }

    private static RequestPetsPayload read(final RegistryByteBuf buf) {
      final UnleashedDogBreed breedFilter =
          buf.readBoolean() ? UnleashedDogBreed.fromSerializedId(buf.readString()) : null;
      return new RequestPetsPayload(
          breedFilter, PetAliveFilter.fromSerializedName(buf.readString()), buf.readString());
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
      UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, List<PetSyncData> pets)
      implements CustomPayload {

    public static final CustomPayload.Id<SyncPetManagerStatePayload> ID =
        new CustomPayload.Id<>(SYNC_PET_MANAGER_STATE_ID);
    public static final PacketCodec<RegistryByteBuf, SyncPetManagerStatePayload> CODEC =
        PacketCodec.of(SyncPetManagerStatePayload::write, SyncPetManagerStatePayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      buf.writeBoolean(this.breedFilter != null);
      if (this.breedFilter != null) {
        buf.writeString(this.breedFilter.serializedId());
      }
      buf.writeString(this.aliveFilter.serializedName());
      buf.writeInt(this.pets.size());
      for (final PetSyncData pet : this.pets) {
        pet.write(buf);
      }
    }

    private static SyncPetManagerStatePayload read(final RegistryByteBuf buf) {
      final UnleashedDogBreed breedFilter =
          buf.readBoolean() ? UnleashedDogBreed.fromSerializedId(buf.readString()) : null;
      final PetAliveFilter aliveFilter = PetAliveFilter.fromSerializedName(buf.readString());
      final int petCount = buf.readInt();
      final List<PetSyncData> pets = new java.util.ArrayList<>(petCount);
      for (int i = 0; i < petCount; i++) {
        pets.add(PetSyncData.read(buf));
      }
      return new SyncPetManagerStatePayload(breedFilter, aliveFilter, pets);
    }
  }

  public record PetSyncData(
      String petId,
      UnleashedDogBreed breed,
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

    public static PetSyncData from(final PetData petData) {
      return new PetSyncData(
          petData.getPetId().toString(),
          petData.getBreed(),
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

    private void write(final RegistryByteBuf buf) {
      buf.writeString(this.petId);
      buf.writeString(this.breed.serializedId());
      buf.writeString(this.name);
      buf.writeFloat(this.health);
      buf.writeFloat(this.maxHealth);
      buf.writeInt(this.posX);
      buf.writeInt(this.posY);
      buf.writeInt(this.posZ);
      buf.writeString(this.dimension);
      buf.writeBoolean(this.alive);
      buf.writeBoolean(this.baby);
      buf.writeInt(this.collarColor);
      buf.writeInt(this.coatVariant);
      buf.writeInt(this.huskyEyeVariant);
    }

    private static PetSyncData read(final RegistryByteBuf buf) {
      return new PetSyncData(
          buf.readString(),
          UnleashedDogBreed.fromSerializedId(buf.readString()),
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

  public record OpenNamingScreenPayload(UUID petId, UnleashedDogBreed breed, String suggestedName)
      implements CustomPayload {

    public static final CustomPayload.Id<OpenNamingScreenPayload> ID =
        new CustomPayload.Id<>(OPEN_NAMING_SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, OpenNamingScreenPayload> CODEC =
        PacketCodec.of(OpenNamingScreenPayload::write, OpenNamingScreenPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      buf.writeString(this.petId.toString());
      buf.writeString(this.breed.serializedId());
      buf.writeString(this.suggestedName);
    }

    private static OpenNamingScreenPayload read(final RegistryByteBuf buf) {
      return new OpenNamingScreenPayload(
          UUID.fromString(buf.readString()),
          UnleashedDogBreed.fromSerializedId(buf.readString()),
          buf.readString());
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
      final SetPetNamePayload payload, final ServerPlayNetworking.Context context) {
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
      final SummonPetPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld playerWorld = player.getServerWorld();

    playerWorld
        .getServer()
        .execute(
            () -> {
              final PetData petData =
                  PetManager.get(playerWorld.getServer()).getPet(player.getUuid(), payload.petId());
              if (petData != null && petData.isAlive()) {
                PetLocationService.loadAndSummon(playerWorld.getServer(), petData, player);
              }
            });
  }

  private static void handleRequestPets(
      final RequestPetsPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final PetManager petManager = PetManager.get(world.getServer());
              final PetManagerPreferencesState preferencesState =
                  PetManagerPreferencesState.get(world.getServer());
              preferencesState.setPreferences(
                  player.getUuid(), payload.breedFilter(), payload.aliveFilter());

              final List<PetData> pets =
                  petManager.getPetsByOwnerFiltered(
                      player.getUuid(),
                      payload.breedFilter(),
                      payload.aliveFilter(),
                      payload.searchQuery());
              final List<PetSyncData> syncData = syncPetData(world.getServer(), petManager, pets);
              ServerPlayNetworking.send(player, new SyncPetsPayload(syncData));
            });
  }

  private static void handleRequestPetManagerState(
      final RequestPetManagerStatePayload payload, final ServerPlayNetworking.Context context) {
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
              final List<PetData> pets =
                  PetManager.get(world.getServer())
                      .getPetsByOwnerFiltered(
                          player.getUuid(),
                          preferences.breedFilter(),
                          preferences.aliveFilter(),
                          "");
              final List<PetSyncData> syncData =
                  syncPetData(world.getServer(), PetManager.get(world.getServer()), pets);
              ServerPlayNetworking.send(
                  player,
                  new SyncPetManagerStatePayload(
                      preferences.breedFilter(), preferences.aliveFilter(), syncData));
            });
  }

  private static List<PetSyncData> syncPetData(
      final MinecraftServer server, final PetManager petManager, final List<PetData> pets) {
    for (final PetData pet : pets) {
      if (pet.isAlive()) {
        final UnleashedDogEntity dog = PetLocationService.findDog(server, pet);
        if (dog != null) {
          pet.setHealth(dog.getHealth());
          pet.setLastKnownPosition(dog.getBlockPos());
          pet.setDimension(((ServerWorld) dog.getWorld()).getRegistryKey().getValue().toString());
          pet.syncAppearanceFrom(dog);
          petManager.updatePet(pet);
        }
      }
    }
    return pets.stream().map(PetSyncData::from).toList();
  }

  public static void sendOpenNamingScreen(
      final ServerPlayerEntity player,
      final UUID petId,
      final UnleashedDogBreed breed,
      final String suggestedName) {
    ServerPlayNetworking.send(player, new OpenNamingScreenPayload(petId, breed, suggestedName));
  }
}
