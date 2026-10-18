package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.WildPackRules;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WildHuntGoal extends OwnTargetGoal<LivingEntity> {

  private static final int TARGET_CHANCE = 10;
  private static final Map<World, PenMemory> PEN_MEMORIES = new WeakHashMap<>();

  private final UnleashedDogEntity dog;

  public WildHuntGoal(final UnleashedDogEntity dog) {
    super(dog, LivingEntity.class, TARGET_CHANCE, WildHuntGoal::isPrey);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.dog.getPack().mayHunt() && super.canStart();
  }

  @Override
  protected boolean stillWants(final LivingEntity target) {
    return this.dog.getPack().keepsHunting();
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
    final World world = target.getWorld();
    final BlockPos center = target.getBlockPos();
    return PEN_MEMORIES
        .computeIfAbsent(world, ignored -> new PenMemory())
        .isPenned(center.asLong(), world.getTime(), () -> scanForPen(world, center));
  }

  private static boolean scanForPen(final World world, final BlockPos center) {
    for (final BlockPos pos :
        BlockPos.iterate(
            center.add(-WildPackRules.PEN_RANGE, -1, -WildPackRules.PEN_RANGE),
            center.add(WildPackRules.PEN_RANGE, 1, WildPackRules.PEN_RANGE))) {
      final BlockState state = world.getBlockState(pos);
      if (state.isIn(BlockTags.FENCES)
          || state.isIn(BlockTags.FENCE_GATES)
          || state.isIn(BlockTags.WALLS)) {
        return true;
      }
    }
    return false;
  }
}
