package com.grahambartley.dogsunleashed.network.handler;

import static com.grahambartley.dogsunleashed.network.PacketLimits.REQUEST_PETS_SEARCH_QUERY_MAX_LENGTH;
import static com.grahambartley.dogsunleashed.network.PacketLimits.SET_PET_NAME_NAME_MAX_LENGTH;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RequestPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.RequestPetsPayload;
import com.grahambartley.dogsunleashed.network.payload.SetPetNamePayload;
import com.grahambartley.dogsunleashed.network.payload.SummonPetPayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetManagerStatePayload;
import com.grahambartley.dogsunleashed.network.payload.SyncPetsPayload;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import com.grahambartley.dogsunleashed.pet.PetManagerPreferencesState;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class PetManagerNetworkHandler {

  private PetManagerNetworkHandler() {}

  public static void handleSetPetName(
      final SetPetNamePayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    final ServerWorld world = player.getServerWorld();

    String name = payload.name().trim();
    if (name.isBlank()) {
      return;
    }
    name = PetNameSanitizer.stripControlChars(name);
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

  public static void handleSummonPet(
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

  public static void handleRequestPets(
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
              final List<PetSyncData> syncData =
                  PetSyncRefresher.refreshAndProject(world.getServer(), petManager, pets);
              ServerPlayNetworking.send(player, new SyncPetsPayload(syncData));
            });
  }

  public static void handleRequestPetManagerState(
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
                  PetSyncRefresher.refreshAndProject(
                      world.getServer(), PetManager.get(world.getServer()), pets);
              ServerPlayNetworking.send(
                  player,
                  new SyncPetManagerStatePayload(
                      preferences.breedFilter(), preferences.aliveFilter(), syncData));
            });
  }
}
