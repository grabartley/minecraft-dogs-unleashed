package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogTrickLearnedCriterion;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.entity.trick.TrickLearningRules;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgressNbt;
import com.grahambartley.dogsunleashed.entity.trick.TrickReadiness;
import com.grahambartley.dogsunleashed.pet.PetNames;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class DogTricks extends DogComponent {

  public static final String LEARNED_MESSAGE_KEY = "message.dogs-unleashed.trick_learned";
  public static final String SHOWN_MESSAGE_KEY = "message.dogs-unleashed.trick_shown";

  private final UnleashedDogEntity dog;
  private TrickProgress progress = TrickProgress.NONE;
  private @Nullable DogTrick offered;
  private long offeredAt = TrickLearningRules.NEVER;
  private @Nullable DogTrick performing;
  private long performingUntil = TrickLearningRules.NEVER;

  DogTricks(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public TrickProgress progress() {
    return this.progress;
  }

  public void restore(final TrickProgress progress) {
    this.progress = progress;
  }

  public Optional<DogTrick> offered() {
    if (this.offered == null
        || !TrickLearningRules.isWindowOpen(this.offeredAt, this.dog.getWorld().getTime())) {
      return Optional.empty();
    }
    return Optional.of(this.offered);
  }

  public void offer(final DogTrick trick) {
    this.offered = trick;
    this.offeredAt = this.dog.getWorld().getTime();
    this.perform(trick);
  }

  public boolean isPerforming() {
    return this.performing != null && this.dog.getWorld().getTime() < this.performingUntil;
  }

  public TrickReadiness readiness() {
    return new TrickReadiness(
        this.dog.getSleepController().isAsleep(),
        this.dog.getComponents().get(DogSledHarness.class).isHitched(),
        this.dog.hasVehicle() || this.dog.hasPassengers(),
        this.dog.getTarget() != null,
        this.dog.hurtTime > 0,
        this.dog.isTouchingWater(),
        DogIdleness.isOnErrand(this.dog)
            || this.dog.getComponents().get(DogBowlMeal.class).isEating()
            || this.dog.getComponents().get(DogBoneBurial.class).isDigging(),
        this.isPerforming());
  }

  public void perform(final DogTrick trick) {
    this.performing = trick;
    this.performingUntil = trick.endsAt(this.dog.getWorld().getTime());
    this.dog.triggerAnim(trick.animationKey(), trick.animationKey());
    if (this.dog.getOwner() instanceof PlayerEntity owner) {
      owner.sendMessage(
          Text.translatable(
              SHOWN_MESSAGE_KEY,
              PetNames.tamedNameOf(this.dog),
              Text.translatable(trick.translationKey())),
          true);
    }
  }

  public boolean rewardOffered(final PlayerEntity player) {
    final DogTrick trick = this.offered().orElse(null);
    if (trick == null) {
      return false;
    }
    this.offered = null;
    this.offeredAt = TrickLearningRules.NEVER;
    final boolean learns = this.progress.learnsWithNextReward(trick);
    this.progress = this.progress.reward(trick);
    if (learns) {
      this.announceLearned(player, trick);
    }
    return learns;
  }

  private void announceLearned(final PlayerEntity player, final DogTrick trick) {
    player.sendMessage(
        Text.translatable(
            LEARNED_MESSAGE_KEY,
            PetNames.tamedNameOf(this.dog),
            Text.translatable(trick.translationKey())),
        false);
    if (player instanceof ServerPlayerEntity serverPlayer) {
      DogTrickLearnedCriterion.INSTANCE.trigger(serverPlayer, this.progress.learned().size());
    }
  }

  private void stopPerforming() {
    if (this.performing != null && this.isPerforming()) {
      this.dog.stopTriggeredAnim(this.performing.animationKey(), this.performing.animationKey());
    }
    this.performing = null;
    this.performingUntil = TrickLearningRules.NEVER;
  }

  @Override
  void tick() {
    if (this.performing != null && (!this.isPerforming() || this.readiness().interruptsTrick())) {
      this.stopPerforming();
    }
    if (this.offered != null && this.offered().isEmpty()) {
      this.offered = null;
      this.offeredAt = TrickLearningRules.NEVER;
    }
    if (this.offered != null || !TrickLearningRules.rollsOffer(this.dog.getRandom()::nextInt)) {
      return;
    }
    final LivingEntity owner = this.dog.getOwner();
    if (owner == null || owner.getWorld() != this.dog.getWorld()) {
      return;
    }
    if (TrickLearningRules.mayOffer(
        this.dog.isTamed(),
        DogIdleness.isFreeForChores(this.dog) && this.readiness().mayStart(),
        this.dog.distanceTo(owner))) {
      TrickLearningRules.pickUnlearned(this.progress, this.dog.getRandom()::nextInt)
          .ifPresent(this::offer);
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.put(ModNbtKeys.TRICKS, TrickProgressNbt.write(this.progress));
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.progress =
        nbt.contains(ModNbtKeys.TRICKS, NbtElement.COMPOUND_TYPE)
            ? TrickProgressNbt.read(nbt.getCompound(ModNbtKeys.TRICKS))
            : TrickProgress.NONE;
  }
}
