package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import com.grahambartley.dogsunleashed.entity.goal.TugRules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public final class DogTug extends DogComponent {

  public static final int NO_PLAYER = -1;
  private static final float HAPPY_BARK_PITCH_BOOST = 1.2f;

  private final UnleashedDogEntity dog;
  private @Nullable TugState state;

  DogTug(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable TugState state() {
    return this.state;
  }

  public boolean isTugging() {
    return this.state != null && !this.state.keepingAway();
  }

  public boolean isKeepingAway() {
    return this.state != null && this.state.keepingAway();
  }

  public int tuggingPlayerId() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.TUG_PLAYER_ID);
  }

  public boolean isBeingTugged() {
    return this.tuggingPlayerId() != NO_PLAYER;
  }

  public float balance() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.TUG_BALANCE);
  }

  public @Nullable PlayerEntity player() {
    return this.state == null ? null : this.dog.getWorld().getPlayerByUuid(this.state.player());
  }

  public boolean isCarryingRope() {
    return this.dog.getFetchCarry().isCarryingFetchedItem()
        && this.dog.getFetchCarry().carriedStack().isOf(ModItems.ROPE_TOY);
  }

  @Nullable
  ActionResult interact(final PlayerEntity player, final ItemStack stack) {
    if (this.state != null) {
      if (this.state.isPlayer(player.getUuid())) {
        this.handleOwnClick(player);
        return ActionResult.SUCCESS;
      }
      if (stack.isOf(ModItems.ROPE_TOY)) {
        DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_busy");
        return ActionResult.SUCCESS;
      }
      return null;
    }
    if (!stack.isOf(ModItems.ROPE_TOY) || !this.dog.isTamed()) {
      return null;
    }
    if (!this.readiness(player).mayStart()) {
      if (this.dog.isOwner(player) && !player.isSneaking()) {
        DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_not_now");
        return ActionResult.SUCCESS;
      }
      return null;
    }
    this.start(player, stack);
    return ActionResult.SUCCESS;
  }

  @Override
  void tick() {
    if (this.state == null || this.dog.getWorld().isClient) {
      return;
    }
    final PlayerEntity player = this.player();
    if (!this.isCarryingRope()) {
      this.clear();
      return;
    }
    if (player == null || !player.isAlive()) {
      this.abandon();
      return;
    }
    if (this.state.keepingAway()) {
      if (this.state.isKeepAwayOver(this.now(), this.dog.hurtTime > 0)) {
        this.clear();
      }
      return;
    }
    if (this.dog.hurtTime > 0
        || TugRules.isAbandoned(this.state.tugTicks(this.now()), this.dog.distanceTo(player))) {
      this.abandon();
      return;
    }
    this.state = this.state.dragged(this.dog.getRandom().nextDouble());
    this.resolve(player);
  }

  public void abandon() {
    if (this.state == null) {
      return;
    }
    final PlayerEntity player = this.player();
    if (player != null && player.isAlive()) {
      this.giveRopeTo(player);
      DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_stopped");
    }
    this.clear();
  }

  private void start(final PlayerEntity player, final ItemStack stack) {
    final ItemStack rope = stack.copyWithCount(1);
    stack.decrementUnlessCreative(1, player);
    this.dog.getCommandController().demoteSitToFollow();
    this.dog.getNavigation().stop();
    final DogFetchCarry carry = this.dog.getFetchCarry();
    carry.setActiveType(null);
    carry.setCarrying(true);
    carry.setCarriedStack(rope);
    this.state =
        TugState.start(
            player.getUuid(),
            TugRules.dogStrength(
                this.dog.getMaxHealth(),
                this.dog.isBaby(),
                this.dog.getComponents().get(DogBond.class).step().ordinal()),
            new Spot(this.dog.getX(), this.dog.getZ()),
            this.now());
    this.sync(player.getId());
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_start");
  }

  private void handleOwnClick(final PlayerEntity player) {
    if (this.state == null) {
      return;
    }
    if (this.state.keepingAway()) {
      this.giveRopeTo(player);
      DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_caught");
      this.clear();
      return;
    }
    this.state = this.state.pulled(this.now());
    this.resolve(player);
  }

  private void resolve(final PlayerEntity player) {
    if (this.state == null) {
      return;
    }
    switch (this.state.outcome()) {
      case PLAYER_WINS -> this.letGo(player);
      case DOG_WINS -> this.runOff(player);
      case ONGOING -> this.sync(player.getId());
    }
  }

  private void letGo(final PlayerEntity player) {
    this.giveRopeTo(player);
    this.dog.getAmbienceEffects().celebrate(HAPPY_BARK_PITCH_BOOST);
    this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.PLAY);
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_won");
    this.clear();
  }

  private void runOff(final PlayerEntity player) {
    if (this.state == null) {
      return;
    }
    this.state = this.state.keptAway(this.now());
    this.sync(NO_PLAYER);
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getVocalization().barkIfReady(this.dog.getVocalization().getBarkPitch());
    this.dog.getComponents().get(DogBond.class).reward(DogBondRules.Source.PLAY);
    DogInteractions.tell(this.dog, player, "message.dogs-unleashed.tug_lost");
  }

  private void giveRopeTo(final PlayerEntity player) {
    if (this.isCarryingRope()) {
      final ItemStack rope = this.dog.getFetchCarry().carriedStack().copy();
      this.dog.getFetchCarry().setCarrying(false);
      player.getInventory().offerOrDrop(rope);
    }
  }

  private void clear() {
    this.state = null;
    this.sync(NO_PLAYER);
  }

  private void sync(final int playerId) {
    this.dog.getDataTracker().set(UnleashedDogEntity.TUG_PLAYER_ID, playerId);
    this.dog
        .getDataTracker()
        .set(
            UnleashedDogEntity.TUG_BALANCE,
            this.state == null ? 0.0f : (float) this.state.balance());
  }

  private TugRules.Readiness readiness(final PlayerEntity player) {
    return new TugRules.Readiness(
        this.dog.isOwner(player),
        player.isSneaking(),
        this.dog.getFetchCarry().isCarrying(),
        this.dog.getPlaySession().isInPlayMode(),
        this.dog.getSleepController().isAsleep(),
        this.dog.hasPassengers()
            || this.dog.hasVehicle()
            || this.dog.getComponents().get(DogSledHarness.class).isHitched(),
        this.dog.isOnGround(),
        this.dog.hurtTime > 0);
  }

  private long now() {
    return this.dog.getWorld().getTime();
  }
}
