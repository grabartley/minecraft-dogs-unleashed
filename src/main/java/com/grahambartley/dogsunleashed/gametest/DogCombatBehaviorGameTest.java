package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogCombatBehaviorGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-combat";
  private static final int TICK_LIMIT = 160;
  private static final int FIRST_HIT_TICK = 20;
  private static final int HIT_INTERVAL_TICKS = 15;
  private static final int HIT_COUNT = 8;
  private static final int ASSERTION_TICK = 140;
  private static final float HIT_DAMAGE = 1.0F;
  private static final int BURN_TICKS = 200;
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void guardDogNeverRunsTheEscapeGoalWhileHurt(final TestContext context) {
    assertEscapeGoalWhileHurt(context, DogCommand.GUARD, false, false);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void huntDogNeverRunsTheEscapeGoalWhileHurt(final TestContext context) {
    assertEscapeGoalWhileHurt(context, DogCommand.HUNT, false, false);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void followDogRunsTheEscapeGoalWhileHurt(final TestContext context) {
    assertEscapeGoalWhileHurt(context, DogCommand.FOLLOW, false, true);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void burningGuardDogStillRunsTheEscapeGoal(final TestContext context) {
    assertEscapeGoalWhileHurt(context, DogCommand.GUARD, true, true);
  }

  private void assertEscapeGoalWhileHurt(
      final TestContext context,
      final DogCommand command,
      final boolean onFire,
      final boolean expectedToEscape) {
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    dog.getCommandController().apply(command);

    final boolean[] everEscaped = {false};
    scheduleHits(context, dog, onFire);
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= FIRST_HIT_TICK && isRunningEscapeGoal(dog)) {
            everEscaped[0] = true;
          }
        });

    context.runAtTick(
        ASSERTION_TICK,
        () -> {
          context.assertTrue(dog.isAlive(), "The dog must survive for its goals to mean anything");
          context.assertTrue(
              everEscaped[0] == expectedToEscape,
              "A dog on "
                  + command
                  + (onFire ? " while burning" : "")
                  + " should"
                  + (expectedToEscape ? "" : " never")
                  + " run the escape goal while being hurt");
          context.complete();
        });
  }

  private void scheduleHits(
      final TestContext context, final UnleashedDogEntity dog, final boolean onFire) {
    for (int hit = 0; hit < HIT_COUNT; hit++) {
      context.runAtTick(
          FIRST_HIT_TICK + (long) hit * HIT_INTERVAL_TICKS,
          () -> {
            if (onFire) {
              dog.setFireTicks(BURN_TICKS);
            }
            dog.damage(dog.getWorld().getDamageSources().magic(), HIT_DAMAGE);
          });
    }
  }

  private static boolean isRunningEscapeGoal(final UnleashedDogEntity dog) {
    return dog.goalSelector.getGoals().stream()
        .filter(PrioritizedGoal::isRunning)
        .anyMatch(goal -> goal.getGoal() instanceof EscapeDangerGoal);
  }

  private UnleashedDogEntity spawnOwnedDog(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    final BlockPos absDogPos = context.getAbsolutePos(DOG_POS);
    owner.refreshPositionAndAngles(
        absDogPos.getX() + 0.5, absDogPos.getY(), absDogPos.getZ() + 0.5, 0f, 0f);
    return dog;
  }
}
