package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import com.grahambartley.dogsunleashed.gesture.PlayerGestures;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class DogPetting extends DogComponent {

  private final UnleashedDogEntity dog;
  private long lastPettedAt = PettingRules.NEVER;
  private long requestedAt = PettingRules.NEVER;
  private @Nullable UUID petterUuid;

  DogPetting(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  ActionResult tryPet(final PlayerEntity player, final Hand hand) {
    if (!PettingRules.isPetGesture(
        player.isSneaking(), hand == Hand.MAIN_HAND, player.getStackInHand(hand).isEmpty())) {
      return null;
    }
    if (!this.welcomes(player)) {
      return null;
    }
    this.petterUuid = player.getUuid();
    if (this.canReach(player)) {
      this.begin(player);
    } else {
      this.requestedAt = this.now();
    }
    return ActionResult.CONSUME;
  }

  public Optional<PlayerEntity> approachingPetter() {
    return PettingRules.isApproaching(this.now(), this.requestedAt)
        ? this.petter()
        : Optional.empty();
  }

  public Optional<PlayerEntity> currentPetter() {
    return PettingRules.isBeingPetted(this.now(), this.lastPettedAt)
        ? this.petter()
        : Optional.empty();
  }

  public boolean arriveAt(final PlayerEntity player) {
    if (!this.canReach(player)) {
      return false;
    }
    this.requestedAt = PettingRules.NEVER;
    if (this.welcomes(player)) {
      this.begin(player);
    }
    return true;
  }

  private void begin(final PlayerEntity player) {
    this.lastPettedAt = this.now();
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
      PlayerGestures.show(serverPlayer, Hand.MAIN_HAND, PlayerGesture.PAT, this.dog.getId());
    }
  }

  private boolean welcomes(final PlayerEntity player) {
    return PettingRules.welcomes(
        this.stateFor(player), this.now(), this.lastPettedAt, this.requestedAt);
  }

  private boolean canReach(final PlayerEntity player) {
    return PettingRules.inReach(
        this.dog.getPos().subtract(player.getPos()).horizontalLength(), this.dog.getWidth() / 2.0);
  }

  private Optional<PlayerEntity> petter() {
    return this.petterUuid == null
        ? Optional.empty()
        : Optional.ofNullable(this.dog.getWorld().getPlayerByUuid(this.petterUuid));
  }

  private long now() {
    return this.dog.getWorld().getTime();
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
