package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayDogs;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public final class PetSyncRefresher {

  private PetSyncRefresher() {}

  public static List<PetSyncData> refreshAndProject(
      final MinecraftServer server, final PetManager petManager, final List<PetData> pets) {
    for (final PetData pet : pets) {
      if (pet.isAlive()) {
        final UnleashedDogEntity dog = PetLocationService.findLoadedDog(server, pet);
        if (dog != null) {
          final PetData current =
              PetData.snapshotOf(dog, pet.getOwnerId(), pet.getName(), pet.getLifeState());
          if (pet.differsFrom(current, DogsUnleashed.SERVER_CONFIG.aging())) {
            pet.setHealth(current.getHealth());
            pet.setLastKnownPosition(current.getLastKnownPosition());
            pet.setDimension(current.getDimension());
            pet.syncFrom(dog);
            petManager.updatePet(pet);
          }
        }
      }
    }
    return pets.stream()
        .map(pet -> PetSyncData.from(pet, AwayDogs.statusOf(server, pet.getPetId())))
        .toList();
  }
}
