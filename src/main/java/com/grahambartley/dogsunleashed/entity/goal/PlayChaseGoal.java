package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogIdleness;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.DogPlayChase;
import com.grahambartley.dogsunleashed.entity.PlayChaseState;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Playground;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import org.jetbrains.annotations.Nullable;

public class PlayChaseGoal extends Goal {

  private static final double CHASE_SPEED = 1.3;
  private static final double RUN_SPEED = 1.2;
  private static final double HOSTILE_RANGE = 8.0;
  private static final int REPATH_TICKS = 10;

  private final UnleashedDogEntity dog;
  private int checkCooldown;
  private int repathCooldown;

  public PlayChaseGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.checkCooldown = PlayChaseRules.CHECK_INTERVAL_TICKS;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (this.play().isPlaying()) {
      return this.play().partner() != null && readiness(this.dog).mayPlay();
    }
    if (--this.checkCooldown > 0) {
      return false;
    }
    this.checkCooldown = PlayChaseRules.CHECK_INTERVAL_TICKS;
    if (this.play().isResting()
        || !readiness(this.dog).mayPlay()
        || !PlayChaseRules.startsGame(this.isPlayful(), this.dog.getRandom()::nextInt)) {
      return false;
    }
    final UnleashedDogEntity playmate = this.findPlaymate();
    if (playmate == null) {
      return false;
    }
    this.play().startWith(playmate);
    return true;
  }

  @Override
  public boolean shouldContinue() {
    final PlayChaseState game = this.play().game();
    return game != null
        && !game.isOver(this.now())
        && this.play().partner() != null
        && PlayChaseRules.isInPlayArea(game.centre(), spotOf(this.dog))
        && readiness(this.dog).mayPlay();
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
  }

  @Override
  public void stop() {
    this.play().end();
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    final PlayChaseState game = this.play().game();
    final UnleashedDogEntity partner = this.play().partner();
    if (game == null || partner == null) {
      return;
    }
    this.dog.getLookControl().lookAt(partner);
    if (game.isBowing(this.now())) {
      this.dog.getNavigation().stop();
      return;
    }
    if (game.chasing()
        && PlayChaseRules.swapsRoles(game.legTicks(this.now()), this.dog.distanceTo(partner))) {
      this.play().swapRolesWith(partner);
      this.repathCooldown = 0;
      return;
    }
    if (--this.repathCooldown > 0) {
      return;
    }
    this.repathCooldown = REPATH_TICKS;
    if (game.chasing()) {
      this.dog.getNavigation().startMovingTo(partner, CHASE_SPEED);
      return;
    }
    final Spot target = PlayChaseRules.fleeTarget(game.centre(), spotOf(this.dog), spotOf(partner));
    this.dog.getNavigation().startMovingTo(target.x(), this.dog.getY(), target.z(), RUN_SPEED);
  }

  public @Nullable UnleashedDogEntity findPlaymate() {
    final Playground mine = playgroundOf(this.dog);
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            this.dog.getBoundingBox().expand(PlayChaseRules.START_RANGE),
            other ->
                other != this.dog
                    && other.isAlive()
                    && this.dog.distanceTo(other) <= PlayChaseRules.START_RANGE
                    && PlayChaseRules.arePlaymates(
                        mine, playgroundOf(other), this.dog.getPack().isPackmate(other))
                    && !other.getComponents().get(DogPlayChase.class).isPlaying()
                    && !other.getComponents().get(DogPlayChase.class).isResting()
                    && readiness(other).mayPlay())
        .stream()
        .min(Comparator.comparingDouble(this.dog::squaredDistanceTo))
        .orElse(null);
  }

  private static PlayChaseRules.Readiness readiness(final UnleashedDogEntity dog) {
    return new PlayChaseRules.Readiness(
        playgroundOf(dog),
        DogIdleness.isFreeForChores(dog),
        dog.isOnGround() || dog.getComponents().get(DogPlayChase.class).isPlaying(),
        dog.getFetchCarry().isCarrying(),
        DogSleepWindow.isSleepTime(dog.getWorld().getTimeOfDay(), dog.isBaby(), dog.isUndead()),
        GroundNapRules.isHurtRecently(
            dog.getWorld().getTime(), dog.getSleepController().getLastHurtAt()),
        isInDanger(dog));
  }

  private static Playground playgroundOf(final UnleashedDogEntity dog) {
    return PlayChaseRules.playgroundOf(
        dog.isTamed(), dog.getCommand() == DogCommand.FREE_ROAM, dog.getPack().staysOutOfFights());
  }

  private static boolean isInDanger(final UnleashedDogEntity dog) {
    return dog.isOnFire()
        || dog.isInLava()
        || !dog.getWorld()
            .getEntitiesByClass(
                MobEntity.class,
                dog.getBoundingBox().expand(HOSTILE_RANGE),
                mob -> mob instanceof Monster && mob.isAlive())
            .isEmpty();
  }

  private static Spot spotOf(final UnleashedDogEntity dog) {
    return new Spot(dog.getX(), dog.getZ());
  }

  private boolean isPlayful() {
    return this.dog.getComponents().get(DogPersonality.class).traits().has(DogTrait.PLAYFUL);
  }

  private DogPlayChase play() {
    return this.dog.getComponents().get(DogPlayChase.class);
  }

  private long now() {
    return this.dog.getWorld().getTime();
  }
}
