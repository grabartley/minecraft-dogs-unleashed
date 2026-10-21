package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogTrickLearnedCriterion;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.entity.trick.TrickLearningRules;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgressNbt;
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
  public static final float HAPPY_BARK_PITCH_BOOST = 1.2f;

  private final UnleashedDogEntity dog;
  private TrickProgress progress = TrickProgress.NONE;
  private @Nullable DogTrick offered;
  private long offeredAt = TrickLearningRules.NEVER;

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

  public void perform(final DogTrick trick) {
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

  @Override
  void tick() {
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
        this.dog.isTamed(), this.isIdle(), this.dog.distanceTo(owner))) {
      TrickLearningRules.pickUnlearned(this.progress, this.dog.getRandom()::nextInt)
          .ifPresent(this::offer);
    }
  }

  private boolean isIdle() {
    return DogIdleness.isFreeForChores(this.dog)
        && !this.dog.hasVehicle()
        && !this.dog.hasPassengers();
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
