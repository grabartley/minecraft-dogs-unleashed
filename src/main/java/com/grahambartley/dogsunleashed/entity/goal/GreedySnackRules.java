package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public final class GreedySnackRules {

  public static final double ITEM_RANGE = 8.0;
  public static final int CHEST_RANGE = 5;
  public static final int CHEST_VERTICAL_RANGE = 2;
  public static final double REACH = 1.5;
  public static final double CHEST_REACH = 2.0;
  public static final int LOOK_ONE_IN = 200;
  public static final long COOLDOWN_TICKS = 2 * 60 * 20;
  public static final int MAX_TICKS = 300;
  public static final long NEVER = Long.MIN_VALUE;
  public static final int NO_SLOT = -1;

  public enum Step {
    GIVE_UP,
    EAT,
    MOVE
  }

  private GreedySnackRules() {}

  public static boolean mayLook(
      final DogCommand command, final boolean sitting, final boolean carrying) {
    return (command == DogCommand.FOLLOW || command == DogCommand.FREE_ROAM)
        && !sitting
        && !carrying;
  }

  public static boolean cooledDown(final long now, final long lastSnackAt) {
    return lastSnackAt == NEVER || now - lastSnackAt >= COOLDOWN_TICKS;
  }

  public static boolean isSnack(final ItemStack stack) {
    return !stack.isEmpty() && stack.contains(DataComponentTypes.FOOD);
  }

  public static int firstSnackSlot(final List<ItemStack> stacks) {
    for (int slot = 0; slot < stacks.size(); slot++) {
      if (isSnack(stacks.get(slot))) {
        return slot;
      }
    }
    return NO_SLOT;
  }

  public static Step step(final int ticks, final double squaredDistance, final double reach) {
    if (ticks > MAX_TICKS) {
      return Step.GIVE_UP;
    }
    return squaredDistance <= reach * reach ? Step.EAT : Step.MOVE;
  }
}
