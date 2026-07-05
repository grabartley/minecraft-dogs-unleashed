package com.grahambartley.dogsunleashed.entity.fetch;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;

public interface FetchProjectileEntity {
  int NOTIFY_PLAY_RANGE = 128;

  FetchItemType getFetchItemType();

  static void notifyPlayingDogsOfLandedFetchItem(
      ProjectileEntity projectile, BlockPos fetchItemPos) {
    if (!(projectile.getOwner() instanceof PlayerEntity player)) {
      return;
    }

    if (!(projectile instanceof FetchProjectileEntity fetchProjectileEntity)) {
      return;
    }

    UUID playerUuid = player.getUuid();
    List<UnleashedDogEntity> playingDogs =
        projectile
            .getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                projectile.getBoundingBox().expand(NOTIFY_PLAY_RANGE),
                dog -> dog.isInPlayMode() && playerUuid.equals(dog.getPlayPartnerPlayerUuid()));
    for (UnleashedDogEntity dog : playingDogs) {
      dog.setActiveFetchType(fetchProjectileEntity.getFetchItemType());
      dog.setActiveFetchBlockPos(fetchItemPos);
    }
  }

  static void notifyPlayingDogsToEndPlayMode(ProjectileEntity projectile) {
    if (!(projectile.getOwner() instanceof PlayerEntity player)) {
      return;
    }

    UUID playerUuid = player.getUuid();
    List<UnleashedDogEntity> playingDogs =
        projectile
            .getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                projectile.getBoundingBox().expand(NOTIFY_PLAY_RANGE),
                dog -> dog.isInPlayMode() && playerUuid.equals(dog.getPlayPartnerPlayerUuid()));
    for (UnleashedDogEntity dog : playingDogs) {
      dog.endPlayMode();
    }
  }
}
