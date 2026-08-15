package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import com.grahambartley.dogsunleashed.pet.PetAliveFilter;
import com.grahambartley.dogsunleashed.screen.DogCommandWheelScreen;
import com.grahambartley.dogsunleashed.screen.DogInspectScreen;
import com.grahambartley.dogsunleashed.screen.PetManagerScreen;
import com.grahambartley.dogsunleashed.screen.PetNamingScreen;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class ModNetworkingClient {

  public static void registerClientReceivers() {
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.OpenNamingScreenPayload.ID, ModNetworkingClient::handleOpenNamingScreen);
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.OpenCommandWheelPayload.ID, ModNetworkingClient::handleOpenCommandWheel);

    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.SyncPetsPayload.ID, ModNetworkingClient::handleSyncPets);
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.SyncPetManagerStatePayload.ID,
        ModNetworkingClient::handleSyncPetManagerState);
    ClientPlayNetworking.registerGlobalReceiver(
        SyncServerConfigS2CPayload.ID, ModNetworkingClient::handleSyncServerConfig);
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.SyncDogConnectionsPayload.ID, ModNetworkingClient::handleSyncDogConnections);
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.OpenDogInspectPayload.ID, ModNetworkingClient::handleOpenDogInspect);
  }

  private static void handleOpenDogInspect(
      ModNetworking.OpenDogInspectPayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              final MinecraftClient client = MinecraftClient.getInstance();
              if (client.currentScreen != null || client.world == null) {
                return;
              }
              client.setScreen(new DogInspectScreen(payload));
            });
  }

  private static void handleSyncDogConnections(
      ModNetworking.SyncDogConnectionsPayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              if (MinecraftClient.getInstance().currentScreen
                  instanceof DogConnectionsListener listener) {
                listener.onDogConnections(payload);
              }
            });
  }

  private static void handleSyncServerConfig(
      SyncServerConfigS2CPayload payload, ClientPlayNetworking.Context context) {
    context.client().execute(() -> DogsUnleashed.SERVER_CONFIG = payload.config());
  }

  private static void handleOpenNamingScreen(
      ModNetworking.OpenNamingScreenPayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              MinecraftClient.getInstance()
                  .setScreen(
                      new PetNamingScreen(
                          payload.petId(), payload.breed(), payload.suggestedName()));
            });
  }

  private static void handleOpenCommandWheel(
      ModNetworking.OpenCommandWheelPayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              final MinecraftClient client = MinecraftClient.getInstance();
              if (client.currentScreen != null || client.world == null) {
                return;
              }
              client.setScreen(new DogCommandWheelScreen(payload));
            });
  }

  private static void handleSyncPets(
      ModNetworking.SyncPetsPayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              if (MinecraftClient.getInstance().currentScreen instanceof PetManagerScreen screen) {
                screen.updatePetsList(payload.pets());
              }
            });
  }

  private static void handleSyncPetManagerState(
      ModNetworking.SyncPetManagerStatePayload payload, ClientPlayNetworking.Context context) {
    context
        .client()
        .execute(
            () -> {
              if (MinecraftClient.getInstance().currentScreen instanceof PetManagerScreen screen) {
                screen.applySavedFilters(payload.breedFilter(), payload.aliveFilter());
                screen.updatePetsList(payload.pets());
              }
            });
  }

  public static void sendSelectWheelAction(UUID dogId, DogWheelAction action) {
    ClientPlayNetworking.send(new ModNetworking.SelectWheelActionPayload(dogId, action.id()));
  }

  public static void sendSetPetName(UUID petId, String name) {
    ClientPlayNetworking.send(new ModNetworking.SetPetNamePayload(petId, name));
  }

  public static void sendSummonPet(UUID petId) {
    ClientPlayNetworking.send(new ModNetworking.SummonPetPayload(petId));
  }

  public static void sendRequestPets(
      UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, String searchQuery) {
    ClientPlayNetworking.send(
        new ModNetworking.RequestPetsPayload(breedFilter, aliveFilter, searchQuery));
  }

  public static void sendRequestPetManagerState() {
    ClientPlayNetworking.send(new ModNetworking.RequestPetManagerStatePayload());
  }

  public static void sendRequestDogConnections(UUID dogId) {
    ClientPlayNetworking.send(new ModNetworking.RequestDogConnectionsPayload(dogId));
  }
}
