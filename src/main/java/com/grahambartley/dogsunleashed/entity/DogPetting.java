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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogPetting extends DogComponent {

  private final UnleashedDogEntity dog;
  private long lastPettedAt = PettingRules.NEVER;
  private long requestedAt = PettingRules.NEVER;
  private @Nullable UUID petterUuid;
  private Vec3d askedFrom = Vec3d.ZERO;

  DogPetting(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  ActionResult tryPet(final PlayerEntity player, final Hand hand) {
    if (!PettingRules.isPetGesture(
        player.isSneaking(), hand == Hand.MAIN_HAND, player.getStackInHand(hand).isEmpty())) {
      return null;
    }
    if (!PettingRules.welcomes(
        this.stateFor(player), this.now(), this.lastPettedAt, this.requestedAt)) {
      return null;
    }
    this.petterUuid = player.getUuid();
    this.askedFrom = player.getPos();
    this.requestedAt = this.now();
    this.show(player, PlayerGesture.INVITE);
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

  public boolean isAtHandOf(final PlayerEntity player) {
    return PettingRules.inReach(
        this.dog.getPos().subtract(player.getPos()).horizontalLength(), this.dog.getWidth() / 2.0);
  }

  public void settleAt(final PlayerEntity player) {
    if (!this.isAtHandOf(player) || !PettingRules.hasSettled(this.now(), this.requestedAt)) {
      return;
    }
    this.requestedAt = PettingRules.NEVER;
    final PettingRules.DogState state = this.stateFor(player);
    if (state.calm() && state.free()) {
      this.begin(player);
    } else {
      this.release(player);
    }
  }

  @Override
  void tick() {
    if (this.petterUuid == null) {
      return;
    }
    final boolean asking = this.requestedAt != PettingRules.NEVER;
    final boolean petting = PettingRules.isBeingPetted(this.now(), this.lastPettedAt);
    if (!asking && !petting) {
      this.petterUuid = null;
      return;
    }
    final Optional<PlayerEntity> petter = this.petter();
    if (petter.isEmpty()
        || !petter.get().isAlive()
        || PettingRules.walkedAway(petter.get().getPos().distanceTo(this.askedFrom))
        || (asking && !PettingRules.isApproaching(this.now(), this.requestedAt))) {
      petter.ifPresent(this::release);
      this.requestedAt = PettingRules.NEVER;
      this.lastPettedAt = PettingRules.NEVER;
      this.petterUuid = null;
    }
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
    this.show(player, PlayerGesture.PAT);
  }

  private void release(final PlayerEntity player) {
    this.show(player, PlayerGesture.RELEASE);
  }

  private void show(final PlayerEntity player, final PlayerGesture gesture) {
    if (player instanceof ServerPlayerEntity serverPlayer) {
      PlayerGestures.show(serverPlayer, Hand.MAIN_HAND, gesture, this.dog.getId());
    }
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
