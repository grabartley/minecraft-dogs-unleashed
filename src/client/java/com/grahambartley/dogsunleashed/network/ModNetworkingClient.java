package com.grahambartley.dogsunleashed.network;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.DogWheelAction;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenCommandWheelPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenDogInspectPayload;
import com.grahambartley.dogsunleashed.network.payload.OpenNamingScreenPayload;
import com.grahambartley.dogsunleashed.network.payload.RequestDogConnectionsPayload;
import com.grahambartley.dogsunleashed.network.payload.RequestPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.RequestPetsPayload;
import com.grahambartley.dogsunleashed.network.payload.SelectWheelActionPayload;
import com.grahambartley.dogsunleashed.network.payload.SetPetNamePayload;
import com.grahambartley.dogsunleashed.network.payload.SummonPetPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncDogConnectionsPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetsPayload;
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
        OpenNamingScreenPayload.ID, ModNetworkingClient::handleOpenNamingScreen);
    ClientPlayNetworking.registerGlobalReceiver(
        OpenCommandWheelPayload.ID, ModNetworkingClient::handleOpenCommandWheel);

    ClientPlayNetworking.registerGlobalReceiver(
        SyncPetsPayload.ID, ModNetworkingClient::handleSyncPets);
    ClientPlayNetworking.registerGlobalReceiver(
        SyncPetManagerStatePayload.ID, ModNetworkingClient::handleSyncPetManagerState);
    ClientPlayNetworking.registerGlobalReceiver(
        SyncServerConfigS2CPayload.ID, ModNetworkingClient::handleSyncServerConfig);
    ClientPlayNetworking.registerGlobalReceiver(
        SyncDogConnectionsPayload.ID, ModNetworkingClient::handleSyncDogConnections);
    ClientPlayNetworking.registerGlobalReceiver(
        OpenDogInspectPayload.ID, ModNetworkingClient::handleOpenDogInspect);
  }

  private static void handleOpenDogInspect(
      OpenDogInspectPayload payload, ClientPlayNetworking.Context context) {
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
      SyncDogConnectionsPayload payload, ClientPlayNetworking.Context context) {
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
      OpenNamingScreenPayload payload, ClientPlayNetworking.Context context) {
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
      OpenCommandWheelPayload payload, ClientPlayNetworking.Context context) {
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
      SyncPetsPayload payload, ClientPlayNetworking.Context context) {
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
      SyncPetManagerStatePayload payload, ClientPlayNetworking.Context context) {
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
    ClientPlayNetworking.send(new SelectWheelActionPayload(dogId, action.id()));
  }

  public static void sendSetPetName(UUID petId, String name) {
    ClientPlayNetworking.send(new SetPetNamePayload(petId, name));
  }

  public static void sendSummonPet(UUID petId) {
    ClientPlayNetworking.send(new SummonPetPayload(petId));
  }

  public static void sendRequestPets(
      UnleashedDogBreed breedFilter, PetAliveFilter aliveFilter, String searchQuery) {
    ClientPlayNetworking.send(new RequestPetsPayload(breedFilter, aliveFilter, searchQuery));
  }

  public static void sendRequestPetManagerState() {
    ClientPlayNetworking.send(new RequestPetManagerStatePayload());
  }

  public static void sendRequestDogConnections(UUID dogId) {
    ClientPlayNetworking.send(new RequestDogConnectionsPayload(dogId));
  }
}
