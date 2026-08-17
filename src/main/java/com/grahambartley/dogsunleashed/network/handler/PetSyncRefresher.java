package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class PetSyncRefresher {

  private PetSyncRefresher() {}

  public static List<PetSyncData> refreshAndProject(
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
}
