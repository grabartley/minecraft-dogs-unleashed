package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.ModPointsOfInterest;
import com.grahambartley.dogsunleashed.block.WaterBowlBlock;
import com.grahambartley.dogsunleashed.block.entity.FoodBowlBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogBowlMeal;
import com.grahambartley.dogsunleashed.entity.DogFoods;
import com.grahambartley.dogsunleashed.entity.DogIdleness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.DogBowlVisitRules.Bowl;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

public class DogBowlGoal extends Goal {

  private static final double SPEED = 1.0;
  private static final int REPATH_INTERVAL_TICKS = 10;
  private static final int EFFECT_INTERVAL_TICKS = 6;
  private static final int MEAL_PARTICLES = 4;
  private static final double PARTICLE_SPREAD = 0.15;
  private static final double BOWL_TOP = 0.3;

  private final UnleashedDogEntity dog;
  private @Nullable BlockPos bowlPos;
  private @Nullable Bowl bowl;
  private @Nullable Path path;
  private long nextSearchTime;
  private int walkTicks;
  private int mealTicks = -1;
  private int repathCooldown;

  public DogBowlGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (this.world().getTime() < this.nextSearchTime || !this.dog.isOnGround() || !this.isFree()) {
      return false;
    }
    this.waitFor(DogBowlVisitRules.SEARCH_INTERVAL_TICKS);
    boolean sawBowl = false;
    for (final Bowl candidate : Bowl.values()) {
      if (!this.wants(candidate)) {
        continue;
      }
      for (final BlockPos pos : this.nearby(candidate)) {
        sawBowl = true;
        final Path found = this.dog.getNavigation().findPathTo(pos, 1);
        if (found != null && found.reachesTarget()) {
          this.bowl = candidate;
          this.bowlPos = pos;
          this.path = found;
          return true;
        }
      }
    }
    if (sawBowl) {
      this.waitFor(DogBowlVisitRules.UNREACHABLE_COOLDOWN_TICKS);
    }
    return false;
  }

  @Override
  public boolean shouldContinue() {
    return this.isStillWanted();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.walkTicks = 0;
    this.mealTicks = -1;
    this.repathCooldown = REPATH_INTERVAL_TICKS;
    this.dog.getNavigation().startMovingAlong(this.path, SPEED);
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.bowl = null;
    this.bowlPos = null;
    this.path = null;
    this.mealTicks = -1;
    this.meal().setEating(false);
  }

  @Override
  public void tick() {
    if (!this.isStillWanted()) {
      this.bowl = null;
      this.meal().setEating(false);
      return;
    }
    final Bowl visiting = this.bowl;
    final BlockPos pos = this.bowlPos;
    final Vec3d center = Vec3d.ofBottomCenter(pos);
    if (this.mealTicks >= 0) {
      this.dog.getLookControl().lookAt(center.x, center.y, center.z);
      if (this.mealTicks % EFFECT_INTERVAL_TICKS == 0) {
        this.mealEffects(visiting, pos, center);
      }
      if (--this.mealTicks < 0) {
        this.finishMeal(visiting, pos);
      }
      return;
    }
    if (DogBowlVisitRules.isBeside(
        pos.getX(), pos.getY(), pos.getZ(), this.dog.getX(), this.dog.getY(), this.dog.getZ())) {
      this.dog.getNavigation().stop();
      this.mealTicks = DogBowlVisitRules.MEAL_TICKS;
      this.meal().setEating(true);
      return;
    }
    if (++this.walkTicks > DogBowlVisitRules.MAX_WALK_TICKS) {
      this.waitFor(DogBowlVisitRules.UNREACHABLE_COOLDOWN_TICKS);
      this.bowl = null;
      return;
    }
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = REPATH_INTERVAL_TICKS;
      this.dog.getNavigation().startMovingTo(center.x, center.y, center.z, SPEED);
    }
  }

  private boolean isStillWanted() {
    return this.bowl != null
        && this.bowlPos != null
        && this.isFree()
        && this.wants(this.bowl)
        && this.isUsable(this.bowl, this.bowlPos);
  }

  private void finishMeal(final Bowl eaten, final BlockPos pos) {
    switch (eaten) {
      case FOOD -> {
        if (this.world().getBlockEntity(pos) instanceof FoodBowlBlockEntity food
            && !food.takeOne(this.dog::isBreedingItem).isEmpty()) {
          this.dog.heal(DogFoods.HEAL_AMOUNT);
          this.dog.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0F, 1.0F);
        }
      }
      case WATER -> {
        WaterBowlBlock.drink(this.world(), pos, this.world().getBlockState(pos));
        this.dog.addStatusEffect(
            new StatusEffectInstance(
                StatusEffects.REGENERATION, DogBowlVisitRules.REGENERATION_TICKS));
        this.dog.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
      }
    }
    this.waitFor(DogBowlVisitRules.MEAL_COOLDOWN_TICKS);
    this.bowl = null;
    this.meal().setEating(false);
  }

  private void mealEffects(final Bowl eating, final BlockPos pos, final Vec3d center) {
    this.world()
        .spawnParticles(
            this.mealParticle(eating, pos),
            center.x,
            center.y + BOWL_TOP,
            center.z,
            MEAL_PARTICLES,
            PARTICLE_SPREAD,
            0.0,
            PARTICLE_SPREAD,
            0.05);
  }

  private ParticleEffect mealParticle(final Bowl eating, final BlockPos pos) {
    if (eating == Bowl.FOOD
        && this.world().getBlockEntity(pos) instanceof FoodBowlBlockEntity food) {
      for (int slot = 0; slot < food.size(); slot++) {
        final ItemStack stack = food.getStack(slot);
        if (!stack.isEmpty()) {
          return new ItemStackParticleEffect(ParticleTypes.ITEM, stack.copyWithCount(1));
        }
      }
    }
    return ParticleTypes.SPLASH;
  }

  private void waitFor(final int ticks) {
    this.nextSearchTime = this.world().getTime() + ticks;
  }

  private boolean wants(final Bowl candidate) {
    return DogBowlVisitRules.wants(
        candidate,
        this.dog.getHealth(),
        this.dog.getMaxHealth(),
        this.dog.isUndead(),
        this.dog.hasStatusEffect(StatusEffects.REGENERATION));
  }

  private List<BlockPos> nearby(final Bowl candidate) {
    final RegistryKey<PointOfInterestType> type =
        candidate == Bowl.FOOD ? ModPointsOfInterest.FOOD_BOWL : ModPointsOfInterest.WATER_BOWL;
    final BlockPos origin = this.dog.getBlockPos();
    return this.world()
        .getPointOfInterestStorage()
        .getInCircle(
            entry -> entry.matchesKey(type),
            origin,
            DogBowlVisitRules.SEARCH_RANGE,
            PointOfInterestStorage.OccupationStatus.ANY)
        .map(PointOfInterest::getPos)
        .filter(pos -> this.isUsable(candidate, pos))
        .sorted(Comparator.comparingDouble(pos -> pos.getSquaredDistance(origin)))
        .limit(DogBowlVisitRules.PATH_CHECKS_PER_SEARCH)
        .toList();
  }

  private boolean isUsable(final Bowl candidate, final BlockPos pos) {
    final boolean holdsSomething =
        switch (candidate) {
          case FOOD ->
              this.world().getBlockEntity(pos) instanceof FoodBowlBlockEntity food
                  && food.hasFood(this.dog::isBreedingItem);
          case WATER -> WaterBowlBlock.hasWater(this.world().getBlockState(pos));
        };
    return holdsSomething && this.isWithinPost(pos);
  }

  private boolean isWithinPost(final BlockPos pos) {
    final BlockPos anchor = this.dog.getCommandController().getAnchorPos();
    return DogBowlVisitRules.isWithinPost(
        this.dog.getCommand().isAnchored() && anchor != null,
        anchor == null ? 0.0 : pos.getSquaredDistance(anchor));
  }

  private boolean isFree() {
    return this.dog.isTamed()
        && DogBowlVisitRules.isSafe(
            this.dog.isOnFire(), this.dog.getAttacker() != null, this.dog.shouldEscapePowderSnow())
        && !this.dog.getFetchCarry().isCarrying()
        && DogIdleness.isFreeForChores(this.dog);
  }

  private DogBowlMeal meal() {
    return this.dog.getComponents().get(DogBowlMeal.class);
  }

  private ServerWorld world() {
    return (ServerWorld) this.dog.getWorld();
  }
}
