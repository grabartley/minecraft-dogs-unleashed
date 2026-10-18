package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DireWolfStamina;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DireWolfRidingGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_WOLF = new BlockPos(5, FLOOR_Y, 4);
  private static final Vec3d REL_PLAYER = new Vec3d(8.5, FLOOR_Y, 4.5);
  private static final int SPRINT_TICKS = 20;

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void anOwnedPrimeOrAlphaCarriesItsOwner(final TestContext context) {
    for (final DireWolfGrowthStage stage :
        new DireWolfGrowthStage[] {DireWolfGrowthStage.PRIME, DireWolfGrowthStage.ALPHA}) {
      final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
      final UnleashedDogEntity wolf = ownedWolf(context, owner, stage);
      context.assertTrue(wolf.getRiding().mount(owner), stage + " should carry its owner");
      context.assertTrue(owner.getVehicle() == wolf, stage + " is not carrying the owner");
      context.assertTrue(
          wolf.getControllingPassenger() == owner, "The owner should steer the " + stage);
      owner.stopRiding();
      SurvivalTestPlayer.leave(context, owner);
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void youngWolvesAndOtherBreedsCannotBeRidden(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    for (final DireWolfGrowthStage stage :
        new DireWolfGrowthStage[] {DireWolfGrowthStage.YEARLING, DireWolfGrowthStage.ADULT}) {
      final UnleashedDogEntity wolf = ownedWolf(context, owner, stage);
      context.assertFalse(wolf.getRiding().mount(owner), stage + " carried its owner");
      context.assertTrue(
          "screen.dogs-unleashed.command_wheel.ride.too_young"
              .equals(wolf.getRiding().refusalKey(owner)),
          stage + " should be refused as too young");
    }
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, REL_WOLF);
    husky.setAiDisabled(true);
    husky.setOwner(owner);
    context.assertFalse(husky.getRiding().mount(owner), "A husky carried its owner");
    context.assertTrue(
        "screen.dogs-unleashed.command_wheel.ride.breed"
            .equals(husky.getRiding().refusalKey(owner)),
        "A husky should be refused by breed");
    context.assertTrue(owner.getVehicle() == null, "The owner is riding something");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aStrangerCannotRideSomeoneElsesPrime(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    context.assertFalse(wolf.getRiding().mount(stranger), "A stranger rode the prime");
    SurvivalTestPlayer.leave(context, owner);
    SurvivalTestPlayer.leave(context, stranger);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aSittingOrLeashedPrimeRefusesToCarry(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity sitting = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    sitting.getCommandController().apply(DogCommand.SIT);
    context.assertFalse(sitting.getRiding().mount(owner), "A sitting prime carried its owner");
    final UnleashedDogEntity leashed = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    leashed.attachLeash(owner, true);
    context.assertFalse(leashed.getRiding().mount(owner), "A leashed prime carried its owner");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void gettingOffLeavesTheCommandAsItWas(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    wolf.getCommandController().apply(DogCommand.HEEL);
    wolf.getRiding().mount(owner);
    owner.stopRiding();
    context.assertTrue(
        wolf.getCommand() == DogCommand.HEEL,
        "The command should still be heel, but is " + wolf.getCommand());
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aRiddenWolfTakesNoTargetsAndRunsNoGoals(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    wolf.setAiDisabled(false);
    wolf.getRiding().mount(owner);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_WOLF.east(3));
    zombie.setAiDisabled(true);
    wolf.setTarget(zombie);
    context.assertTrue(wolf.getTarget() == null, "A ridden wolf took a target");
    context.assertFalse(wolf.canMoveVoluntarily(), "A ridden wolf's goals should be paused");
    owner.stopRiding();
    context.assertTrue(wolf.canMoveVoluntarily(), "Goals should resume after getting off");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = SPRINT_TICKS + 20)
  public void sprintingDrainsStaminaThatSurvivesAReload(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    wolf.getRiding().mount(owner);
    context.runAtTick(
        2,
        () -> {
          context.assertTrue(
              wolf.getRiding().stamina() == DireWolfStamina.PRIME_CAPACITY,
              "A fresh prime should start full, not " + wolf.getRiding().stamina());
          owner.setSprinting(true);
          context.assertTrue(
              wolf.getRiding().isSprinting(), "The rider sprinted but the wolf did not");
        });
    context.runAtTick(
        2 + SPRINT_TICKS,
        () -> {
          final float stamina = wolf.getRiding().stamina();
          context.assertTrue(
              stamina < DireWolfStamina.PRIME_CAPACITY - 7.0f,
              "Twenty ticks of sprinting should cost 8 stamina, but it is " + stamina);
          final NbtCompound nbt = new NbtCompound();
          wolf.writeNbt(nbt);
          final UnleashedDogEntity loaded = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
          loaded.readNbt(nbt);
          context.assertTrue(
              Math.abs(loaded.getRiding().stamina() - stamina) < 0.001f,
              "Stamina was " + stamina + " but loaded as " + loaded.getRiding().stamina());
          owner.stopRiding();
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = SPRINT_TICKS + 20, batchId = "stamina-drain-off")
  public void withDrainOffSprintingCostsNothing(final TestContext context) {
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG =
        before.withDireWolf(
            before.direWolf().withStamina(before.direWolf().stamina().withDrains(false)));
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.ALPHA);
    wolf.getRiding().mount(owner);
    context.runAtTick(2, () -> owner.setSprinting(true));
    context.runAtTick(
        2 + SPRINT_TICKS,
        () -> {
          DogsUnleashed.SERVER_CONFIG = before;
          context.assertTrue(
              wolf.getRiding().stamina() == DireWolfStamina.ALPHA_CAPACITY,
              "With drain off stamina should stay full, not " + wolf.getRiding().stamina());
          owner.stopRiding();
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void onlyItsOwnRidersSprintMakesTheWolfSprint(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = ownedWolf(context, owner, DireWolfGrowthStage.PRIME);
    owner.setSprinting(true);
    stranger.setSprinting(true);
    context.runAtTick(
        2,
        () -> {
          context.assertFalse(wolf.getRiding().isSprinting(), "The wolf sprints with nobody on it");
          context.assertFalse(wolf.canSprintAsVehicle(), "An empty wolf lets a rider sprint");
          wolf.getRiding().mount(owner);
          context.assertTrue(
              wolf.canSprintAsVehicle(), "The ridden wolf refuses its rider's sprint");
          context.assertTrue(
              wolf.getRiding().isSprinting(), "The rider sprints but the wolf does not");
          owner.setSprinting(false);
          context.assertFalse(
              wolf.getRiding().isSprinting(), "The wolf sprints on a stranger's say or by itself");
          owner.stopRiding();
          SurvivalTestPlayer.leave(context, owner);
          SurvivalTestPlayer.leave(context, stranger);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aRiddenWolfMovesAtItsStageSpeedOnTheRidersSide(final TestContext context) {
    for (final DireWolfGrowthStage stage :
        new DireWolfGrowthStage[] {DireWolfGrowthStage.PRIME, DireWolfGrowthStage.ALPHA}) {
      final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
      final UnleashedDogEntity wolf = ownedWolf(context, owner, stage);
      wolf.setMovementSpeed(0.0f);
      wolf.getRiding().mount(owner);
      context.assertTrue(
          Math.abs(wolf.getRiding().targetSpeed() - (float) stage.movementSpeed() * 0.55f)
              < 1.0e-4f,
          "A ridden "
              + stage
              + " should move at its stage speed "
              + stage.movementSpeed()
              + " even where its AI never ran, but moves at "
              + wolf.getRiding().targetSpeed());
      owner.stopRiding();
      SurvivalTestPlayer.leave(context, owner);
    }
    context.complete();
  }

  private static UnleashedDogEntity ownedWolf(
      final TestContext context, final ServerPlayerEntity owner, final DireWolfGrowthStage stage) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF);
    wolf.setAiDisabled(true);
    wolf.setOwner(owner);
    wolf.getGrowth().setStage(stage);
    wolf.getCommandController().apply(DogCommand.FOLLOW);
    return wolf;
  }
}
