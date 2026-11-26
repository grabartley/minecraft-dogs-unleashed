package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import com.grahambartley.dogsunleashed.gesture.PlayerGestures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class DogPetting extends DogComponent {

  private static final float LOOK_TURN_DEGREES = 30.0f;

  private final UnleashedDogEntity dog;
  private long lastPettedAt = PettingRules.NEVER;
  private @Nullable PlayerEntity petter;

  DogPetting(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  ActionResult tryPet(final PlayerEntity player, final Hand hand) {
    if (!PettingRules.isPetGesture(
        player.isSneaking(), hand == Hand.MAIN_HAND, player.getStackInHand(hand).isEmpty())) {
      return null;
    }
    final long now = this.dog.getWorld().getTime();
    if (!PettingRules.welcomes(this.stateFor(player), now, this.lastPettedAt)) {
      return null;
    }
    this.lastPettedAt = now;
    this.petter = player;
    this.holdStillFacing(player);
    this.dog.triggerAnim(DogAnimationKeys.PETTED, DogAnimationKeys.PETTED);
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getAmbienceEffects().burstHearts();
    this.dog.playSound(
        SoundEvents.ENTITY_WOLF_PANT,
        DogsUnleashed.SERVER_CONFIG.sound().barkVolume(),
        this.dog.getVocalization().getBarkPitch());
    if (this.dog.isOwner(player)) {
      this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.PET);
    }
    if (player instanceof ServerPlayerEntity serverPlayer) {
      PlayerGestures.show(serverPlayer, hand, PlayerGesture.PAT);
    }
    return ActionResult.CONSUME;
  }

  @Override
  void tick() {
    if (this.petter == null) {
      return;
    }
    if (!PettingRules.isBeingPetted(this.dog.getWorld().getTime(), this.lastPettedAt)
        || this.petter.isRemoved()) {
      this.petter = null;
      return;
    }
    this.holdStillFacing(this.petter);
  }

  private void holdStillFacing(final PlayerEntity player) {
    this.dog.getNavigation().stop();
    this.dog.getLookControl().lookAt(player, LOOK_TURN_DEGREES, LOOK_TURN_DEGREES);
  }

  private PettingRules.DogState stateFor(final PlayerEntity player) {
    return new PettingRules.DogState(
        this.dog.getSleepController().isAsleep(),
        this.dog.getTarget() == player,
        this.dog.shouldAngerAt(player),
        DogIdleness.isOccupied(this.dog),
        this.dog.getComponents().get(DogTrickPerformance.class).isPerforming(),
        this.dog.hasVehicle(),
        this.dog.hasPassengers(),
        this.dog.getComponents().get(DogSledHarness.class).isHitched(),
        this.dog.getAnimationState().isHowling(),
        this.dog.getAnimationState().isShaking(),
        this.dog.getGrowl().isGrowling());
  }
}
