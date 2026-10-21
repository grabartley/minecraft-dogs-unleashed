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

  @Override
  void tick() {
    if (this.game != null && (this.game.isOver(this.now()) || this.partner() == null)) {
      this.end();
    }
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
    final PlayChaseState.Pair pair =
        PlayChaseState.pair(
            this.dog.getUuid(),
            partner.getUuid(),
            PlayChaseRules.midpoint(spotOf(this.dog), spotOf(partner)),
            this.now(),
            PlayChaseRules.gameTicks(this.dog.getRandom()::nextInt));
    this.game = pair.inviter();
    play(partner).game = pair.invitee();
    this.dog.triggerAnim(DogAnimationKeys.PLAY_BOW, DogAnimationKeys.PLAY_BOW);
  }

  public void join() {
    if (this.game != null) {
      this.game = this.game.asJoined();
    }
  }

  public @Nullable UnleashedDogEntity partner() {
    if (this.game == null
        || !(this.dog.getWorld() instanceof ServerWorld world)
        || !(world.getEntity(this.game.partner()) instanceof UnleashedDogEntity partner)
        || !partner.isAlive()) {
      return null;
    }
    final PlayChaseState theirs = play(partner).game;
    return theirs != null && theirs.isPartneredWith(this.dog.getUuid()) ? partner : null;
  }

  public boolean isStood() {
    final UnleashedDogEntity partner = this.partner();
    return this.game != null
        && (partner == null || this.game.isStood(this.now(), play(partner).game.joined()));
  }

  public void swapRolesWith(final UnleashedDogEntity partner) {
    final DogPlayChase theirs = play(partner);
    if (this.game == null || theirs.game == null) {
      return;
    }
    final long now = this.now();
    this.game = this.game.swapped(now);
    theirs.game = theirs.game.swapped(now);
  }

  public void end() {
    if (this.game != null) {
      this.game = null;
      this.restUntil = this.now() + PlayChaseRules.REST_TICKS;
    }
  }

  private static DogPlayChase play(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPlayChase.class);
  }

  private static Spot spotOf(final UnleashedDogEntity dog) {
    return new Spot(dog.getX(), dog.getZ());
  }

  private long now() {
    return this.dog.getWorld().getTime();
  }
}
