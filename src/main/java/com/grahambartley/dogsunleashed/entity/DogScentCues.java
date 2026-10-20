package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;

public final class DogScentCues {

  private final UnleashedDogEntity dog;

  DogScentCues(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  void sniff() {
    this.dog.playSound(
        SoundEvents.ENTITY_FOX_SNIFF, 1.0F, this.dog.getVocalization().getBarkPitch());
  }

  public void whine() {
    this.dog.playSound(
        SoundEvents.ENTITY_WOLF_WHINE, 1.0F, this.dog.getVocalization().getBarkPitch());
    this.tellOwner("message.dogs-unleashed.scent.nothing");
  }

  public void found() {
    this.dog.getVocalization().forceBark(this.dog.getVocalization().getBarkPitch());
    this.tellOwner("message.dogs-unleashed.scent.found");
  }

  private void tellOwner(final String translationKey) {
    if (this.dog.getOwner() instanceof PlayerEntity player) {
      DogInteractions.tell(this.dog, player, translationKey);
    }
  }
}
