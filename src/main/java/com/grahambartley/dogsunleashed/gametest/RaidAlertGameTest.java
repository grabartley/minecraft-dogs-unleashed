package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.RaidAlertGoal;
import com.grahambartley.dogsunleashed.entity.goal.RaidAlertRules;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class RaidAlertGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos RAIDER_POS = new BlockPos(1, 2, 1);
  private static final double OUT_OF_RANGE_OFFSET = 52.0;
  private static final int ALERT_WINDOW_TICKS = 60;
  private static final int TICK_LIMIT = 100;

  @GameTest(templateName = ARENA, batchId = "raid-alert-in-range", tickLimit = TICK_LIMIT)
  public void aPillagerInRangeSetsAnOwnedDogBarking(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context);
    this.spawnStillPillager(context);
    context.runAtEveryTick(
        () -> {
          if (isAlerting(dog) && dog.getVocalization().ticksSinceBark(0.0f) < ALERT_WINDOW_TICKS) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = "raid-alert-out-of-range", tickLimit = TICK_LIMIT)
  public void aPillagerOutOfRangeLeavesTheDogQuiet(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context);
    final PillagerEntity pillager = this.spawnStillPillager(context);
    final Vec3d overhead = pillager.getPos().add(0.0, OUT_OF_RANGE_OFFSET, 0.0);
    pillager.setNoGravity(true);
    pillager.refreshPositionAndAngles(overhead.x, overhead.y, overhead.z, 0.0f, 0.0f);
    context.runAtEveryTick(
        () ->
            context.assertTrue(
                pillager.isAlive() && !RaidAlertRules.inRange(dog.squaredDistanceTo(pillager)),
                "the pillager should stay alive and out of range"));
    this.expectNoAlert(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = "raid-alert-sitting", tickLimit = TICK_LIMIT)
  public void aDogToldToSitStaysQuiet(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context);
    dog.getCommandController().apply(DogCommand.SIT);
    this.spawnStillPillager(context);
    this.expectNoAlert(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = "raid-alert-staying", tickLimit = TICK_LIMIT)
  public void aDogToldToStayStaysQuiet(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context);
    dog.getCommandController().apply(DogCommand.STAY);
    this.spawnStillPillager(context);
    this.expectNoAlert(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = "raid-alert-witch", tickLimit = TICK_LIMIT)
  public void aWitchOutsideARaidIsNoIntruder(final TestContext context) {
    final UnleashedDogEntity dog = this.spawnOwnedDog(context);
    final WitchEntity witch = context.spawnEntity(EntityType.WITCH, RAIDER_POS);
    witch.setAiDisabled(true);
    this.expectNoAlert(context, dog);
  }

  @GameTest(templateName = ARENA, batchId = "raid-alert-wild", tickLimit = TICK_LIMIT)
  public void aWildDogIgnoresPillagers(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, DogTestData.BEAGLE, DOG_POS);
    this.spawnStillPillager(context);
    this.expectNoAlert(context, dog);
  }

  private void expectNoAlert(final TestContext context, final UnleashedDogEntity dog) {
    context.runAtEveryTick(
        () -> context.assertFalse(isAlerting(dog), "dog should not be warning of intruders"));
    context.runAtTick(ALERT_WINDOW_TICKS, context::complete);
  }

  private static boolean isAlerting(final UnleashedDogEntity dog) {
    return dog.goalSelector.getGoals().stream()
        .filter(PrioritizedGoal::isRunning)
        .anyMatch(goal -> goal.getGoal() instanceof RaidAlertGoal);
  }

  private UnleashedDogEntity spawnOwnedDog(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    return DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, DOG_POS, owner.getUuid());
  }

  private PillagerEntity spawnStillPillager(final TestContext context) {
    final PillagerEntity pillager = context.spawnEntity(EntityType.PILLAGER, RAIDER_POS);
    pillager.setAiDisabled(true);
    return pillager;
  }
}
