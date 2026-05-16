package com.grahambartley.network;

import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.pet.PetAliveFilter;
import com.grahambartley.screen.PetManagerScreen;
import com.grahambartley.screen.PetNamingScreen;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class ModNetworkingClient {

  public static void registerClientReceivers() {
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.OpenNamingScreenPayload.ID, ModNetworkingClient::handleOpenNamingScreen);

    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.SyncPetsPayload.ID, ModNetworkingClient::handleSyncPets);
    ClientPlayNetworking.registerGlobalReceiver(
        ModNetworking.SyncPetManagerStatePayload.ID,
        ModNetworkingClient::handleSyncPetManagerState);
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
}
