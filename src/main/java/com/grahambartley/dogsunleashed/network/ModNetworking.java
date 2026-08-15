package com.grahambartley.dogsunleashed.network;

import static com.grahambartley.dogsunleashed.network.PacketLimits.CONNECTIONS_LIST_MAX_SIZE;
import static com.grahambartley.dogsunleashed.network.PacketLimits.OWNER_NAME_MAX_LENGTH;
import static com.grahambartley.dogsunleashed.network.PacketLimits.REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH;
import static com.grahambartley.dogsunleashed.network.PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import com.grahambartley.dogsunleashed.pet.DirectConnections;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetManagerPreferencesState;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.util.math.BlockPos;

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
  public static final Identifier OPEN_COMMAND_WHEEL_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "open_command_wheel");
  public static final Identifier SELECT_WHEEL_ACTION_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "select_wheel_action");
  public static final Identifier REQUEST_DOG_CONNECTIONS_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "request_dog_connections");
  public static final Identifier SYNC_DOG_CONNECTIONS_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "sync_dog_connections");

  public record SetPetNamePayload(UUID petId, String name) implements CustomPayload {

    public static final CustomPayload.Id<SetPetNamePayload> ID =
        new CustomPayload.Id<>(SET_PET_NAME_ID);
    public static final PacketCodec<RegistryByteBuf, SetPetNamePayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            SetPetNamePayload::petId,
            PacketCodecs.string(SET_PET_NAME_NAME_MAX_LENGTH),
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
      buf.writeString(this.searchQuery, REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH);
    }

    private static RequestPetsPayload read(final RegistryByteBuf buf) {
      final UnleashedDogBreed breedFilter =
          buf.readBoolean() ? UnleashedDogBreed.fromSerializedId(buf.readString()) : null;
      return new RequestPetsPayload(
          breedFilter,
          PetAliveFilter.fromSerializedName(buf.readString()),
          buf.readString(REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH));
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

  public record OpenCommandWheelPayload(
      int entityId, UUID dogId, int currentCommandId, boolean hasBed, String dogName)
      implements CustomPayload {

    public static final CustomPayload.Id<OpenCommandWheelPayload> ID =
        new CustomPayload.Id<>(OPEN_COMMAND_WHEEL_ID);
    public static final PacketCodec<RegistryByteBuf, OpenCommandWheelPayload> CODEC =
        PacketCodec.of(OpenCommandWheelPayload::write, OpenCommandWheelPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      buf.writeVarInt(this.entityId);
      buf.writeString(this.dogId.toString());
      buf.writeVarInt(this.currentCommandId);
      buf.writeBoolean(this.hasBed);
      buf.writeString(this.dogName);
    }

    private static OpenCommandWheelPayload read(final RegistryByteBuf buf) {
      return new OpenCommandWheelPayload(
          buf.readVarInt(),
          UUID.fromString(buf.readString()),
          buf.readVarInt(),
          buf.readBoolean(),
          buf.readString());
    }
  }

  public record RequestDogConnectionsPayload(UUID dogId) implements CustomPayload {

    public static final CustomPayload.Id<RequestDogConnectionsPayload> ID =
        new CustomPayload.Id<>(REQUEST_DOG_CONNECTIONS_ID);
    public static final PacketCodec<RegistryByteBuf, RequestDogConnectionsPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            RequestDogConnectionsPayload::dogId,
            RequestDogConnectionsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }
  }

  public record ConnectionDogSyncData(PetSyncData pet, String ownerId, String ownerName) {

    private void write(final RegistryByteBuf buf) {
      this.pet.write(buf);
      buf.writeString(this.ownerId);
      buf.writeString(this.ownerName, OWNER_NAME_MAX_LENGTH);
    }

    private static ConnectionDogSyncData read(final RegistryByteBuf buf) {
      return new ConnectionDogSyncData(
          PetSyncData.read(buf), buf.readString(), buf.readString(OWNER_NAME_MAX_LENGTH));
    }
  }

  public record SyncDogConnectionsPayload(
      ConnectionDogSyncData self,
      List<ConnectionDogSyncData> parents,
      List<ConnectionDogSyncData> mates,
      List<ConnectionDogSyncData> siblings,
      List<ConnectionDogSyncData> children,
      boolean truncated)
      implements CustomPayload {

    public static final CustomPayload.Id<SyncDogConnectionsPayload> ID =
        new CustomPayload.Id<>(SYNC_DOG_CONNECTIONS_ID);
    public static final PacketCodec<RegistryByteBuf, SyncDogConnectionsPayload> CODEC =
        PacketCodec.of(SyncDogConnectionsPayload::write, SyncDogConnectionsPayload::read);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
    }

    private void write(final RegistryByteBuf buf) {
      this.self.write(buf);
      writeConnectionList(buf, this.parents);
      writeConnectionList(buf, this.mates);
      writeConnectionList(buf, this.siblings);
      writeConnectionList(buf, this.children);
      buf.writeBoolean(this.truncated);
    }

    private static SyncDogConnectionsPayload read(final RegistryByteBuf buf) {
      return new SyncDogConnectionsPayload(
          ConnectionDogSyncData.read(buf),
          readConnectionList(buf),
          readConnectionList(buf),
          readConnectionList(buf),
          readConnectionList(buf),
          buf.readBoolean());
    }

    private static void writeConnectionList(
        final RegistryByteBuf buf, final List<ConnectionDogSyncData> connections) {
      buf.writeVarInt(connections.size());
      for (final ConnectionDogSyncData connection : connections) {
        connection.write(buf);
      }
    }

    private static List<ConnectionDogSyncData> readConnectionList(final RegistryByteBuf buf) {
      final int count = buf.readVarInt();
      final List<ConnectionDogSyncData> connections = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        connections.add(ConnectionDogSyncData.read(buf));
      }
      return connections;
    }
  }

  public record SelectWheelActionPayload(UUID dogId, int actionId) implements CustomPayload {

    public static final CustomPayload.Id<SelectWheelActionPayload> ID =
        new CustomPayload.Id<>(SELECT_WHEEL_ACTION_ID);
    public static final PacketCodec<RegistryByteBuf, SelectWheelActionPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            SelectWheelActionPayload::dogId,
            PacketCodecs.VAR_INT,
            SelectWheelActionPayload::actionId,
            SelectWheelActionPayload::new);

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
    PayloadTypeRegistry.playS2C()
        .register(OpenCommandWheelPayload.ID, OpenCommandWheelPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(SelectWheelActionPayload.ID, SelectWheelActionPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(RequestDogConnectionsPayload.ID, RequestDogConnectionsPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(SyncDogConnectionsPayload.ID, SyncDogConnectionsPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(SyncServerConfigS2CPayload.ID, SyncServerConfigS2CPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(EditServerConfigC2SPayload.ID, EditServerConfigC2SPayload.CODEC);
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
    ServerPlayNetworking.registerGlobalReceiver(
        EditServerConfigC2SPayload.ID, ModNetworking::handleEditServerConfig);
    ServerPlayNetworking.registerGlobalReceiver(
        SelectWheelActionPayload.ID, ModNetworking::handleSelectWheelAction);
    ServerPlayNetworking.registerGlobalReceiver(
        RequestDogConnectionsPayload.ID, ModNetworking::handleRequestDogConnections);
  }

  private static void handleRequestDogConnections(
      final RequestDogConnectionsPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    world
        .getServer()
        .execute(
            () -> {
              final MinecraftServer server = world.getServer();
              final PetManager petManager = PetManager.get(server);
              final DirectConnections connections =
                  petManager.getDirectConnections(payload.dogId());
              if (connections == null) {
                return;
              }
              final boolean requesterOwnsDog =
                  connections.self().getOwnerId().equals(player.getUuid());
              if (!requesterOwnsDog
                  && !petManager.isConnectedToOwnedPet(player.getUuid(), payload.dogId())) {
                return;
              }
              ServerPlayNetworking.send(
                  player, buildConnectionsPayload(server, petManager, connections));
            });
  }

  private static SyncDogConnectionsPayload buildConnectionsPayload(
      final MinecraftServer server,
      final PetManager petManager,
      final DirectConnections connections) {
    final List<PetData> parents = capConnectionList(connections.parents());
    final List<PetData> mates = capConnectionList(connections.mates());
    final List<PetData> siblings = capConnectionList(connections.siblings());
    final List<PetData> children = capConnectionList(connections.children());
    final boolean truncated =
        parents.size() < connections.parents().size()
            || mates.size() < connections.mates().size()
            || siblings.size() < connections.siblings().size()
            || children.size() < connections.children().size();

    final Set<PetData> allPets = new LinkedHashSet<>();
    allPets.add(connections.self());
    allPets.addAll(parents);
    allPets.addAll(mates);
    allPets.addAll(siblings);
    allPets.addAll(children);
    syncPetData(server, petManager, List.copyOf(allPets));

    return new SyncDogConnectionsPayload(
        toConnectionSyncData(server, connections.self()),
        toConnectionSyncDataList(server, parents),
        toConnectionSyncDataList(server, mates),
        toConnectionSyncDataList(server, siblings),
        toConnectionSyncDataList(server, children),
        truncated);
  }

  private static List<PetData> capConnectionList(final List<PetData> pets) {
    return pets.size() > CONNECTIONS_LIST_MAX_SIZE
        ? pets.subList(0, CONNECTIONS_LIST_MAX_SIZE)
        : pets;
  }

  private static List<ConnectionDogSyncData> toConnectionSyncDataList(
      final MinecraftServer server, final List<PetData> pets) {
    return pets.stream().map(pet -> toConnectionSyncData(server, pet)).toList();
  }

  private static ConnectionDogSyncData toConnectionSyncData(
      final MinecraftServer server, final PetData pet) {
    return new ConnectionDogSyncData(
        PetSyncData.from(pet),
        pet.getOwnerId().toString(),
        resolveOwnerName(server, pet.getOwnerId()));
  }

  // Empty when the owner has never been seen by this server's profile cache; the client renders
  // its own unknown-owner fallback so the server never has to localize.
  private static String resolveOwnerName(final MinecraftServer server, final UUID ownerId) {
    final ServerPlayerEntity onlineOwner = server.getPlayerManager().getPlayer(ownerId);
    if (onlineOwner != null) {
      return onlineOwner.getGameProfile().getName();
    }
    return Optional.ofNullable(server.getUserCache())
        .flatMap(cache -> cache.getByUuid(ownerId))
        .map(GameProfile::getName)
        .orElse("");
  }

  // Same convention as handleSetPetName: no ACK packet, the COMMAND DataTracker broadcast is the
  // source of truth for the client.
  private static void handleSelectWheelAction(
      final SelectWheelActionPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();
    final DogWheelAction action = DogWheelAction.fromId(payload.actionId());
    if (action == null) {
      return;
    }

    world
        .getServer()
        .execute(
            () -> {
              if (!(world.getEntity(payload.dogId()) instanceof UnleashedDogEntity dog)
                  || !dog.isOwner(player)
                  || !dog.isAlive()) {
                return;
              }
              final String dogName = dog.getTamedName();
              if (action == DogWheelAction.GO_TO_BED) {
                final BlockPos bedPos = dog.getAssignedBedPos().orElse(null);
                if (bedPos == null) {
                  return;
                }
                dog.commandToSleep(bedPos);
                player.sendMessage(
                    Text.translatable("block.dogs-unleashed.dog_bed.sleep_command", dogName), true);
                return;
              }
              final DogCommand command = action.command();
              dog.applyCommand(command);
              dog.acknowledgeCommand();
              player.sendMessage(Text.translatable(command.messageKey(), dogName), true);
            });
  }

  private static void handleEditServerConfig(
      final EditServerConfigC2SPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    player
        .getServer()
        .execute(
            () -> {
              if (!player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL)) {
                ServerConfigService.sendTo(player);
                return;
              }
              ServerConfigService.update(player.getServer(), payload.config());
            });
  }

  // DataTracker round-trip is the source of truth: the client sends a rename request, and the
  // displayed name only updates when the entity DataTracker broadcast confirms it. If the server
  // rejects the name here, it never propagates back to the client. No ACK packet is needed.
  private static void handleSetPetName(
      final SetPetNamePayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    String name = payload.name().trim();
    if (name.isBlank()) {
      return;
    }
    name = stripControlChars(name);
    if (name.length() > SET_PET_NAME_NAME_MAX_LENGTH) {
      return;
    }

    final String finalName = name;
    world
        .getServer()
        .execute(
            () -> {
              if (world.getEntity(payload.petId()) instanceof UnleashedDogEntity dog
                  && dog.isOwner(player)) {
                dog.setCustomName(Text.literal(finalName));
                dog.setCustomNameVisible(true);

                final PetManager petManager = PetManager.get(world.getServer());
                final PetData petData = petManager.getPetByEntityId(payload.petId());
                if (petData != null) {
                  petData.setName(finalName);
                  petManager.updatePet(petData);
                }
              }
            });
  }

  static String stripControlChars(final String input) {
    final StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (c >= 0x20 && c != 0x7f) {
        sb.append(c);
      }
    }
    return sb.toString();
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
              if (petData == null) {
                DogsUnleashed.log.warn(
                    "[PetSummon] Summon requested for unknown pet {} by {}",
                    payload.petId(),
                    player.getUuid());
                return;
              }
              if (!petData.isAlive()) {
                player.sendMessage(
                    Text.translatable("message.dogs-unleashed.summon_deceased", petData.getName()),
                    true);
                return;
              }
              PetLocationService.loadAndSummon(playerWorld.getServer(), petData, player);
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

              final String clampedQuery =
                  payload.searchQuery().length() > REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH
                      ? payload.searchQuery().substring(0, REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH)
                      : payload.searchQuery();
              final List<PetData> pets =
                  petManager.getPetsByOwnerFiltered(
                      player.getUuid(), payload.breedFilter(), payload.aliveFilter(), clampedQuery);
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
          final float newHealth = dog.getHealth();
          final BlockPos newPos = dog.getBlockPos();
          final String newDim =
              ((ServerWorld) dog.getWorld()).getRegistryKey().getValue().toString();
          final boolean newBaby = dog.isBaby();
          final int newCollar = dog.getCollarColor().getId();
          final int newCoat = PetData.coatVariantOf(dog);
          final int newHuskyEye = PetData.huskyEyeVariantOf(dog);
          if (pet.differsFrom(
              newHealth, newPos, newDim, newBaby, newCollar, newCoat, newHuskyEye)) {
            pet.setHealth(newHealth);
            pet.setLastKnownPosition(newPos);
            pet.setDimension(newDim);
            pet.syncAppearanceFrom(dog);
            petManager.updatePet(pet);
          }
        }
      }
    }
    return pets.stream().map(PetSyncData::from).toList();
  }

  public static void sendOpenCommandWheel(
      final ServerPlayerEntity player, final UnleashedDogEntity dog) {
    ServerPlayNetworking.send(
        player,
        new OpenCommandWheelPayload(
            dog.getId(),
            dog.getUuid(),
            dog.getCommand().id(),
            dog.hasAssignedBed(),
            dog.getTamedName()));
  }

  public static void sendOpenNamingScreen(
      final ServerPlayerEntity player,
      final UUID petId,
      final UnleashedDogBreed breed,
      final String suggestedName) {
    ServerPlayNetworking.send(player, new OpenNamingScreenPayload(petId, breed, suggestedName));
  }
}
