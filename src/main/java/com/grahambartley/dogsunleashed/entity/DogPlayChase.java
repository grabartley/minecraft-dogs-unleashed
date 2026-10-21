package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class DogPlayChase extends DogComponent {

  private final UnleashedDogEntity dog;
  private @Nullable PlayChaseState game;
  private long restUntil = Long.MIN_VALUE;

  DogPlayChase(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable PlayChaseState game() {
    return this.game;
  }

  public boolean isPlaying() {
    return this.game != null;
  }

  public boolean isResting() {
    return this.now() < this.restUntil;
  }

  public void startWith(final UnleashedDogEntity partner) {
    final long now = this.now();
    final Spot centre =
        PlayChaseRules.midpoint(
            new Spot(this.dog.getX(), this.dog.getZ()), new Spot(partner.getX(), partner.getZ()));
    final int gameTicks = PlayChaseRules.gameTicks(this.dog.getRandom()::nextInt);
    this.game = PlayChaseState.begin(partner.getUuid(), false, centre, now, gameTicks);
    partner.getComponents().get(DogPlayChase.class).game =
        PlayChaseState.begin(this.dog.getUuid(), true, centre, now, gameTicks);
    this.dog.triggerAnim(DogAnimationKeys.PLAY_BOW, DogAnimationKeys.PLAY_BOW);
  }

  public @Nullable UnleashedDogEntity partner() {
    if (this.game == null
        || !(this.dog.getWorld() instanceof ServerWorld world)
        || !(world.getEntity(this.game.partner()) instanceof UnleashedDogEntity partner)
        || !partner.isAlive()) {
      return null;
    }
    final PlayChaseState theirs = partner.getComponents().get(DogPlayChase.class).game;
    return theirs != null && theirs.partner().equals(this.dog.getUuid()) ? partner : null;
  }

  public void swapRolesWith(final UnleashedDogEntity partner) {
    final long now = this.now();
    final DogPlayChase theirs = partner.getComponents().get(DogPlayChase.class);
    if (this.game == null || theirs.game == null) {
      return;
    }
    this.game = this.game.swapped(now);
    theirs.game = theirs.game.swapped(now);
  }

  public void end() {
    if (this.game != null) {
      this.game = null;
      this.restUntil = this.now() + PlayChaseRules.REST_TICKS;
    }
  }

  private long now() {
    return this.dog.getWorld().getTime();
  }
}
