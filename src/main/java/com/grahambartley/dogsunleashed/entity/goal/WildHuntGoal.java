package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.WildPackRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class WildHuntGoal extends ActiveTargetGoal<LivingEntity> {

  private static final int TARGET_CHANCE = 10;

  private final UnleashedDogEntity dog;

  public WildHuntGoal(final UnleashedDogEntity dog) {
    super(dog, LivingEntity.class, TARGET_CHANCE, true, false, WildHuntGoal::isPrey);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.dog.getPack().mayHunt() && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getPack().isWildMember() && super.shouldContinue();
  }

  @Override
  protected double getFollowRange() {
    return WildPackRules.HUNT_RANGE;
  }

  static boolean isPrey(final LivingEntity target) {
    return WildPackRules.isPreyType(target.getClass())
        && HuntTargetGoal.isHuntableTarget(target)
        && !(target instanceof MobEntity mob && mob.isLeashed())
        && !isPenned(target);
  }

  static boolean isPenned(final LivingEntity target) {
    final BlockPos center = target.getBlockPos();
    for (final BlockPos pos :
        BlockPos.iterate(
            center.add(-WildPackRules.PEN_RANGE, -1, -WildPackRules.PEN_RANGE),
            center.add(WildPackRules.PEN_RANGE, 1, WildPackRules.PEN_RANGE))) {
      final BlockState state = target.getWorld().getBlockState(pos);
      if (state.isIn(BlockTags.FENCES)
          || state.isIn(BlockTags.FENCE_GATES)
          || state.isIn(BlockTags.WALLS)) {
        return true;
      }
    }
    return false;
  }
}
