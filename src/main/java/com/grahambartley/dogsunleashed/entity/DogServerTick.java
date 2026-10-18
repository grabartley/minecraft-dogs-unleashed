package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.player.PlayerEntity;

final class DogServerTick {

  static final double NEARBY_PLAYER_RANGE = 10.0D;

  private DogServerTick() {}

  static void run(final UnleashedDogEntity dog) {
    if (dog.isLeashed() && (dog.isSleepingInBed() || dog.isCommandedToSleep())) {
      dog.wakeUp();
    }
    final PlayerEntity nearbyPlayer = dog.getWorld().getClosestPlayer(dog, NEARBY_PLAYER_RANGE);
    dog.getAmbienceEffects().updateSocialCues(nearbyPlayer);
    dog.getVocalization().tick(nearbyPlayer);
    dog.getTreatBuffState().tick();
    dog.getAmbienceEffects().tickShakeOff();
    dog.getUndeadState().tick();
    dog.getCuring().tick();
    dog.getPendants().tick();
    dog.getFur().tick();
    dog.getGrowth().tick();
    dog.getGrowl().tick();
    dog.getTrust().tick();
    dog.getRiding().tick();
  }
}
