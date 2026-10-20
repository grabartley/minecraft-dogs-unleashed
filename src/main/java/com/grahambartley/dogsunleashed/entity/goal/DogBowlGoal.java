package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.ModPointsOfInterest;
import com.grahambartley.dogsunleashed.block.WaterBowlBlock;
import com.grahambartley.dogsunleashed.block.entity.FoodBowlBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogFoods;
import com.grahambartley.dogsunleashed.entity.DogPointedOrders;
import com.grahambartley.dogsunleashed.entity.DogScent;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.DogBowlVisitRules.Bowl;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
  private int searchCooldown;
  private int walkTicks;
  private int mealTicks = -1;
  private int repathCooldown;

  public DogBowlGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (--this.searchCooldown > 0 || !this.dog.isOnGround() || !this.isFree()) {
      return false;
    }
    this.searchCooldown = DogBowlVisitRules.SEARCH_INTERVAL_TICKS;
    boolean sawBowl = false;
    for (final Bowl candidate : Bowl.values()) {
      if (!this.wants(candidate)) {
        continue;
      }
      final List<BlockPos> nearby = this.nearby(candidate);
      sawBowl |= !nearby.isEmpty();
      final Optional<BlockPos> reachable = nearby.stream().filter(this::canReach).findFirst();
      if (reachable.isPresent()) {
        this.bowl = candidate;
        this.bowlPos = reachable.get();
        return true;
      }
    }
    if (sawBowl) {
      this.searchCooldown = DogBowlVisitRules.UNREACHABLE_COOLDOWN_TICKS;
    }
    return false;
  }

  @Override
  public boolean shouldContinue() {
    return this.bowl != null
        && this.bowlPos != null
        && this.isFree()
        && this.wants(this.bowl)
        && this.isUsable(this.bowl, this.bowlPos);
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.walkTicks = 0;
    this.mealTicks = -1;
    this.repathCooldown = 0;
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.bowl = null;
    this.bowlPos = null;
    this.mealTicks = -1;
  }

  @Override
  public void tick() {
    if (this.bowl == null || this.bowlPos == null) {
      return;
    }
    final Vec3d center = Vec3d.ofBottomCenter(this.bowlPos);
    if (this.mealTicks >= 0) {
      this.dog.getLookControl().lookAt(center.x, center.y, center.z);
      if (this.mealTicks % EFFECT_INTERVAL_TICKS == 0) {
        this.mealEffects(this.bowl, center);
      }
      if (--this.mealTicks < 0) {
        this.finishMeal(this.bowl, this.bowlPos);
      }
      return;
    }
    if (DogBowlVisitRules.isBeside(
        this.bowlPos.getX(),
        this.bowlPos.getY(),
        this.bowlPos.getZ(),
        this.dog.getX(),
        this.dog.getY(),
        this.dog.getZ())) {
      this.dog.getNavigation().stop();
      this.mealTicks = DogBowlVisitRules.MEAL_TICKS;
      return;
    }
    if (++this.walkTicks > DogBowlVisitRules.MAX_WALK_TICKS) {
      this.searchCooldown = DogBowlVisitRules.UNREACHABLE_COOLDOWN_TICKS;
      this.bowl = null;
      return;
    }
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = REPATH_INTERVAL_TICKS;
      this.dog.getNavigation().startMovingTo(center.x, center.y, center.z, SPEED);
    }
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
        if (WaterBowlBlock.hasWater(this.world().getBlockState(pos))) {
          WaterBowlBlock.drink(this.world(), pos, this.world().getBlockState(pos));
          this.dog.addStatusEffect(
              new StatusEffectInstance(
                  StatusEffects.REGENERATION, DogBowlVisitRules.REGENERATION_TICKS));
          this.dog.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
        }
      }
    }
    this.searchCooldown = DogBowlVisitRules.MEAL_COOLDOWN_TICKS;
    this.bowl = null;
  }

  private void mealEffects(final Bowl eating, final Vec3d center) {
    final ParticleEffect particle =
        eating == Bowl.FOOD
                && this.bowlPos != null
                && this.world().getBlockEntity(this.bowlPos) instanceof FoodBowlBlockEntity food
            ? new ItemStackParticleEffect(ParticleTypes.ITEM, firstFood(food))
            : ParticleTypes.SPLASH;
    this.world()
        .spawnParticles(
            particle,
            center.x,
            center.y + BOWL_TOP,
            center.z,
            MEAL_PARTICLES,
            PARTICLE_SPREAD,
            0.0,
            PARTICLE_SPREAD,
            0.05);
  }

  private ItemStack firstFood(final FoodBowlBlockEntity food) {
    for (int slot = 0; slot < food.size(); slot++) {
      if (!food.getStack(slot).isEmpty()) {
        return food.getStack(slot).copyWithCount(1);
      }
    }
    return ItemStack.EMPTY;
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
    return switch (candidate) {
      case FOOD ->
          this.world().getBlockEntity(pos) instanceof FoodBowlBlockEntity food
              && food.hasFood(this.dog::isBreedingItem);
      case WATER -> WaterBowlBlock.hasWater(this.world().getBlockState(pos));
    };
  }

  private boolean canReach(final BlockPos pos) {
    final Path path = this.dog.getNavigation().findPathTo(pos, 1);
    return path != null && path.reachesTarget();
  }

  private boolean isFree() {
    final DogPointedOrders orders = this.dog.getComponents().get(DogPointedOrders.class);
    return this.dog.isTamed()
        && this.dog.getTarget() == null
        && !this.dog.isInSittingPose()
        && !this.dog.isLeashed()
        && !this.dog.getSleepController().isAsleep()
        && !this.dog.getPlaySession().isInPlayMode()
        && !this.dog.getFetchCarry().isCarrying()
        && !this.dog.getComponents().get(DogScent.class).isTracking()
        && orders.attackTarget() == null
        && orders.fetchItem() == null
        && !this.dog.getCommandController().isHeadingToAnchor();
  }

  private ServerWorld world() {
    return (ServerWorld) this.dog.getWorld();
  }
}
