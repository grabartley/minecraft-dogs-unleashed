package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.goal.AutoSleepGoal;
import com.grahambartley.dogsunleashed.entity.goal.CommandFollowOwnerGoal;
import com.grahambartley.dogsunleashed.entity.goal.DogEscapeDangerGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchChaseGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchRetrieveGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchReturnGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchTemptGoal;
import com.grahambartley.dogsunleashed.entity.goal.FollowParentDogGoal;
import com.grahambartley.dogsunleashed.entity.goal.GuardTargetGoal;
import com.grahambartley.dogsunleashed.entity.goal.HuntTargetGoal;
import com.grahambartley.dogsunleashed.entity.goal.PuppyAwareWanderGoal;
import com.grahambartley.dogsunleashed.entity.goal.ReturnToAnchorGoal;
import com.grahambartley.dogsunleashed.entity.goal.SleepInBedGoal;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.player.PlayerEntity;

/** The dog's AI goal set and the priorities that order it. */
public final class DogGoalRegistrar {

  private static final double ESCAPE_DANGER_SPEED = 1.5;
  private static final float POUNCE_STRENGTH = 0.4F;
  private static final double DEFAULT_GOAL_SPEED = 1.0;
  private static final float FOLLOW_OWNER_MAX_DISTANCE = 10.0F;
  private static final float FOLLOW_OWNER_MIN_DISTANCE = 2.0F;
  private static final float HEEL_MAX_DISTANCE = 4.0F;
  private static final float HEEL_MIN_DISTANCE = 1.5F;
  private static final float LOOK_AT_PLAYER_RANGE = 8.0F;
  private static final int PLAYER_ANGER_TARGET_CHANCE = 10;

  private DogGoalRegistrar() {}

  public static void registerGoals(
      final UnleashedDogEntity dog,
      final GoalSelector goalSelector,
      final GoalSelector targetSelector) {
    goalSelector.add(1, new SwimGoal(dog));
    goalSelector.add(2, new SitGoal(dog));
    goalSelector.add(3, new SleepInBedGoal(dog));
    goalSelector.add(3, new FetchChaseGoal(dog));
    goalSelector.add(3, new FetchRetrieveGoal(dog));
    goalSelector.add(3, new FetchReturnGoal(dog));
    goalSelector.add(4, new AutoSleepGoal(dog));
    goalSelector.add(5, new DogEscapeDangerGoal(dog, ESCAPE_DANGER_SPEED));
    goalSelector.add(6, new PounceAtTargetGoal(dog, POUNCE_STRENGTH));
    goalSelector.add(7, new MeleeAttackGoal(dog, DEFAULT_GOAL_SPEED, true));
    goalSelector.add(8, new AnimalMateGoal(dog, DEFAULT_GOAL_SPEED));
    goalSelector.add(9, new TemptGoal(dog, DEFAULT_GOAL_SPEED, DogFoods.tamingIngredient(), false));
    goalSelector.add(
        9, new FetchTemptGoal(dog, DEFAULT_GOAL_SPEED, FetchTypes.asIngredient(), false));
    // The three goals below share a priority; their command gates keep them mutually exclusive.
    goalSelector.add(10, new ReturnToAnchorGoal(dog));
    goalSelector.add(
        10,
        new CommandFollowOwnerGoal(
            dog,
            DEFAULT_GOAL_SPEED,
            FOLLOW_OWNER_MAX_DISTANCE,
            FOLLOW_OWNER_MIN_DISTANCE,
            EnumSet.of(DogCommand.FOLLOW, DogCommand.HUNT)));
    goalSelector.add(
        10,
        new CommandFollowOwnerGoal(
            dog,
            DEFAULT_GOAL_SPEED,
            HEEL_MAX_DISTANCE,
            HEEL_MIN_DISTANCE,
            EnumSet.of(DogCommand.HEEL)));
    goalSelector.add(11, new FollowParentDogGoal(dog, DEFAULT_GOAL_SPEED));
    goalSelector.add(12, new PuppyAwareWanderGoal(dog, DEFAULT_GOAL_SPEED));
    goalSelector.add(13, new LookAtEntityGoal(dog, PlayerEntity.class, LOOK_AT_PLAYER_RANGE));
    goalSelector.add(14, new LookAroundGoal(dog));

    targetSelector.add(1, new TrackOwnerAttackerGoal(dog));
    targetSelector.add(2, new AttackWithOwnerGoal(dog));
    targetSelector.add(3, new RevengeGoal(dog, DogFriendlyTargetPolicy.FRIENDLY_TARGET_TYPES));
    targetSelector.add(
        4,
        new ActiveTargetGoal<>(
            dog, PlayerEntity.class, PLAYER_ANGER_TARGET_CHANCE, true, false, dog::shouldAngerAt));
    targetSelector.add(5, new UniversalAngerGoal<>(dog, true));
    targetSelector.add(6, new HuntTargetGoal(dog));
    targetSelector.add(6, new GuardTargetGoal(dog));
  }
}
