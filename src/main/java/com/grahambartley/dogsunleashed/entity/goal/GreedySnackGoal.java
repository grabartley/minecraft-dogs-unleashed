package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogFoods;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GreedySnackGoal extends Goal {

  private static final double SNACK_SPEED = 1.1;
  private static final int REPATH_TICKS = 10;
  private static final int CRUMB_COUNT = 8;
  private static final double CRUMB_SPREAD = 0.15;
  private static final double CRUMB_SPEED = 0.05;

  private final UnleashedDogEntity dog;
  private final BooleanSupplier looks;
  private @Nullable ItemEntity item;
  private @Nullable BlockPos chest;
  private long lastSnackAt = GreedySnackRules.NEVER;
  private int ticks;

  public GreedySnackGoal(final UnleashedDogEntity dog) {
    this(dog, () -> dog.getRandom().nextInt(GreedySnackRules.LOOK_ONE_IN) == 0);
  }

  public GreedySnackGoal(final UnleashedDogEntity dog, final BooleanSupplier looks) {
    this.dog = dog;
    this.looks = looks;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.mayLook()
        || !GreedySnackRules.cooledDown(this.dog.getWorld().getTime(), this.lastSnackAt)
        || !this.looks.getAsBoolean()) {
      return false;
    }
    this.item = this.nearestDroppedSnack();
    this.chest = this.item == null ? this.nearestOpenChestWithSnack() : null;
    return this.item != null || this.chest != null;
  }

  @Override
  public boolean shouldContinue() {
    return this.mayLook() && this.hasSnackTarget();
  }

  @Override
  public void start() {
    this.ticks = 0;
  }

  @Override
  public void tick() {
    final Vec3d target = this.targetPos();
    if (target == null) {
      return;
    }
    final double reach = this.item != null ? GreedySnackRules.REACH : GreedySnackRules.CHEST_REACH;
    final GreedySnackRules.Step step =
        GreedySnackRules.step(++this.ticks, this.dog.squaredDistanceTo(target), reach);
    if (step == GreedySnackRules.Step.GIVE_UP) {
      this.clearTarget();
      return;
    }
    this.dog.getLookControl().lookAt(target.x, target.y, target.z);
    if (step == GreedySnackRules.Step.EAT) {
      this.eat(this.takeOne());
      this.clearTarget();
      return;
    }
    if (this.ticks % REPATH_TICKS == 1) {
      this.dog.getNavigation().startMovingTo(target.x, target.y, target.z, SNACK_SPEED);
    }
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.clearTarget();
  }

  private boolean mayLook() {
    final LivingEntity owner = this.dog.getOwner();
    return this.dog.isTamed()
        && owner != null
        && owner.getWorld() == this.dog.getWorld()
        && this.dog.getComponents().get(DogPersonality.class).traits().snacks()
        && GreedySnackRules.mayLook(
            this.dog.getCommand(),
            this.dog.isInSittingPose(),
            this.dog.getFetchCarry().isCarrying(),
            this.dog.squaredDistanceTo(owner));
  }

  private boolean hasSnackTarget() {
    if (this.item != null) {
      return this.item.isAlive() && GreedySnackRules.isSnack(this.item.getStack());
    }
    return this.chest != null && this.openChestSnackSlot(this.chest) != GreedySnackRules.NO_SLOT;
  }

  private @Nullable Vec3d targetPos() {
    if (this.item != null) {
      return this.item.getPos();
    }
    return this.chest != null ? Vec3d.ofCenter(this.chest) : null;
  }

  private ItemStack takeOne() {
    if (this.item != null && this.item.isAlive()) {
      final ItemStack stack = this.item.getStack();
      final ItemStack bite = stack.copyWithCount(1);
      stack.decrement(1);
      if (stack.isEmpty()) {
        this.item.discard();
      }
      return bite;
    }
    if (this.chest != null
        && this.dog.getWorld().getBlockEntity(this.chest) instanceof ChestBlockEntity chestEntity) {
      final int slot = this.openChestSnackSlot(this.chest);
      if (slot != GreedySnackRules.NO_SLOT) {
        return chestEntity.removeStack(slot, 1);
      }
    }
    return ItemStack.EMPTY;
  }

  private void eat(final ItemStack snack) {
    if (snack.isEmpty()) {
      return;
    }
    this.lastSnackAt = this.dog.getWorld().getTime();
    this.dog.heal(DogFoods.HEAL_AMOUNT);
    this.dog.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
    if (this.dog.getWorld() instanceof ServerWorld serverWorld) {
      serverWorld.spawnParticles(
          new ItemStackParticleEffect(ParticleTypes.ITEM, snack),
          this.dog.getX(),
          this.dog.getEyeY(),
          this.dog.getZ(),
          CRUMB_COUNT,
          CRUMB_SPREAD,
          CRUMB_SPREAD,
          CRUMB_SPREAD,
          CRUMB_SPEED);
    }
  }

  private void clearTarget() {
    this.item = null;
    this.chest = null;
  }

  private @Nullable ItemEntity nearestDroppedSnack() {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            this.dog.getBoundingBox().expand(GreedySnackRules.ITEM_RANGE),
            entity -> entity.isAlive() && GreedySnackRules.isSnack(entity.getStack()))
        .stream()
        .min(Comparator.comparingDouble(this.dog::squaredDistanceTo))
        .orElse(null);
  }

  private @Nullable BlockPos nearestOpenChestWithSnack() {
    for (final BlockPos pos :
        BlockPos.iterateOutwards(
            this.dog.getBlockPos(),
            GreedySnackRules.CHEST_RANGE,
            GreedySnackRules.CHEST_VERTICAL_RANGE,
            GreedySnackRules.CHEST_RANGE)) {
      if (this.openChestSnackSlot(pos) != GreedySnackRules.NO_SLOT) {
        return pos.toImmutable();
      }
    }
    return null;
  }

  private int openChestSnackSlot(final BlockPos pos) {
    final World world = this.dog.getWorld();
    if (!(world.getBlockEntity(pos) instanceof ChestBlockEntity chestEntity)
        || ChestBlockEntity.getPlayersLookingInChestCount(world, pos) <= 0) {
      return GreedySnackRules.NO_SLOT;
    }
    final List<ItemStack> stacks = new ArrayList<>(chestEntity.size());
    for (int slot = 0; slot < chestEntity.size(); slot++) {
      stacks.add(chestEntity.getStack(slot));
    }
    return GreedySnackRules.firstSnackSlot(stacks);
  }
}
