package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import com.grahambartley.dogsunleashed.network.handler.DogConnectionsNetworkHandler;
import com.grahambartley.dogsunleashed.network.handler.OwnerNameResolver;
import com.grahambartley.dogsunleashed.network.handler.PetManagerNetworkHandler;
import com.grahambartley.dogsunleashed.network.handler.RideSprintNetworkHandler;
import com.grahambartley.dogsunleashed.network.handler.ServerConfigNetworkHandler;
import com.grahambartley.dogsunleashed.network.handler.WheelActionNetworkHandler;
import com.grahambartley.dogsunleashed.network.payload.OpenCommandWheelPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenDogInspectPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenNamingScreenPayload;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RequestDogConnectionsPayload;
import com.grahambartley.dogsunleashed.network.payload.RequestPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.RequestPetsPayload;
import com.grahambartley.dogsunleashed.network.payload.RideSprintPayload;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelActionPayload;
import com.grahambartley.dogsunleashed.network.payload.SetPetNamePayload;
import com.grahambartley.dogsunleashed.network.payload.SummonPetPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetsPayload;
import com.grahambartley.dogsunleashed.network.payload.WildTrustView;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ModNetworking {

  private ModNetworking() {}

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
    PayloadTypeRegistry.playS2C().register(OpenDogInspectPayload.ID, OpenDogInspectPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(SyncServerConfigS2CPayload.ID, SyncServerConfigS2CPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(EditServerConfigC2SPayload.ID, EditServerConfigC2SPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(RideSprintPayload.ID, RideSprintPayload.CODEC);
  }

  public static void registerServerReceivers() {
    ServerPlayNetworking.registerGlobalReceiver(
        SetPetNamePayload.ID, PetManagerNetworkHandler::handleSetPetName);
    ServerPlayNetworking.registerGlobalReceiver(
        SummonPetPayload.ID, PetManagerNetworkHandler::handleSummonPet);
    ServerPlayNetworking.registerGlobalReceiver(
        RequestPetsPayload.ID, PetManagerNetworkHandler::handleRequestPets);
    ServerPlayNetworking.registerGlobalReceiver(
        RequestPetManagerStatePayload.ID, PetManagerNetworkHandler::handleRequestPetManagerState);
    ServerPlayNetworking.registerGlobalReceiver(
        EditServerConfigC2SPayload.ID, ServerConfigNetworkHandler::handleEditServerConfig);
    ServerPlayNetworking.registerGlobalReceiver(
        SelectWheelActionPayload.ID, WheelActionNetworkHandler::handleSelectWheelAction);
    ServerPlayNetworking.registerGlobalReceiver(
        RequestDogConnectionsPayload.ID, DogConnectionsNetworkHandler::handleRequestDogConnections);
    ServerPlayNetworking.registerGlobalReceiver(
        RideSprintPayload.ID, RideSprintNetworkHandler::handleRideSprint);
  }

  public static void sendOpenCommandWheel(
      final ServerPlayerEntity player, final UnleashedDogEntity dog) {
    ServerPlayNetworking.send(
        player,
        new OpenCommandWheelPayload(
            dog.getId(),
            dog.getUuid(),
            dog.getCommand().id(),
            dog.getAssignedBedPos().isPresent(),
            PetNames.tamedNameOf(dog),
            Objects.requireNonNullElse(dog.getRiding().refusalKey(player), "")));
  }

  public static void sendOpenNamingScreen(
      final ServerPlayerEntity player,
      final UnleashedDogEntity dog,
      final boolean bornPuppy,
      final String suggestedName) {
    final DogGenome genome = dog.getGenome();
    ServerPlayNetworking.send(
        player,
        new OpenNamingScreenPayload(
            dog.getUuid(),
            dog.getBreed(),
            genome != null ? genome.composition() : List.of(),
            bornPuppy,
            suggestedName));
  }

  public static void sendOpenDogInspect(
      final ServerPlayerEntity player, final UnleashedDogEntity dog) {
    final MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    final UUID ownerId = dog.getOwnerUuid();
    final String ownerName = ownerId != null ? OwnerNameResolver.resolve(server, ownerId) : "";
    final DogGenome genome = dog.getGenome();
    final List<BreedShare> composition =
        genome != null
            ? genome.composition()
            : PetManager.get(server).getBreedComposition(dog.getUuid(), dog.getBreed());
    ServerPlayNetworking.send(
        player,
        new OpenDogInspectPayload(
            PetSyncData.fromDog(dog),
            dog.isTamed(),
            ownerName,
            composition,
            trustView(player, dog)));
  }

  public static WildTrustView trustView(
      final ServerPlayerEntity player, final UnleashedDogEntity dog) {
    if (!dog.getTrust().applies()) {
      return WildTrustView.NONE;
    }
    if (dog.getTrust().isShunnedBy(player)) {
      return WildTrustView.SHUNNED;
    }
    return dog.getTrust().canBeWon()
        ? WildTrustView.building(dog.getTrust().trustOf(player))
        : WildTrustView.TOO_WILD;
  }
}
