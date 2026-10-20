package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogBondStep;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import com.grahambartley.dogsunleashed.entity.goal.DevotedRescueGoal;
import com.grahambartley.dogsunleashed.entity.goal.FetchReturnGoal;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogBondGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d REL_OWNER = new Vec3d(3.5, 2, 1.5);
  private static final double WALK_STEP = 0.5;
  private static final int WALK_TICKS = 100;

  @GameTest(templateName = ARENA, tickLimit = WALK_TICKS + 20)
  public void walkingNearTheOwnerGrowsBond(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.BEAGLE);
    walkBackAndForth(context, dog);

    context.runAtTick(
        WALK_TICKS + 5,
        () -> {
          context.assertTrue(
              bond(dog).points() == 2,
              "About 50 blocks walked near the owner gives 2 bond, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = WALK_TICKS + 20)
  public void walkingFarFromTheOwnerGivesNoBond(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER.add(40, 0, 0));
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.BEAGLE);
    walkBackAndForth(context, dog);

    context.runAtTick(
        WALK_TICKS + 5,
        () -> {
          context.assertTrue(
              bond(dog).points() == 0,
              "Walking with the owner out of range gives nothing, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void treatsGrowBondUpToTheDailyCap(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.HUSKY);

    context.runAtTick(
        2,
        () -> {
          owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT));
          dog.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.TREAT.points(),
              "One treat gives treat bond, got " + bond(dog).points());
          for (int i = 0; i < 4; i++) {
            dog.interact(owner, Hand.MAIN_HAND);
          }
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.TREAT.dailyCap(),
              "Treats stop at the daily cap, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void trimmingTheCoatGrowsBond(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.GOLDEN_RETRIEVER);
    dog.getFur().setStage(DogFurStage.OVERGROWN);

    context.runAtTick(
        2,
        () -> {
          owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.SHEARS));
          dog.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.GROOM.points(),
              "A trim gives grooming bond, got " + bond(dog).points());
          dog.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.GROOM.points(),
              "Shears on a trimmed coat do nothing, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void bringingBackAThrownToyGrowsBond(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.HUSKY);

    context.runAtTick(
        2,
        () -> {
          dog.refreshPositionAndAngles(owner.getX() + 1.0, owner.getY(), owner.getZ(), 0f, 0f);
          dog.getFetchCarry().setCarrying(true);
          dog.getFetchCarry().setActiveType(FetchTypes.TENNIS_BALL);
          dog.getFetchCarry().setCarriedStack(new ItemStack(ModItems.TENNIS_BALL));
          new FetchReturnGoal(dog).tick();
          context.assertFalse(dog.getFetchCarry().isCarrying(), "The dog handed the ball back");
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.PLAY.points(),
              "A game of fetch gives play bond, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void hostileKillsBesideTheOwnerGrowBond(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.SHIBA_INU);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 5));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 5));

    context.runAtTick(
        2,
        () -> {
          kill(context, dog, cow);
          context.assertTrue(bond(dog).points() == 0, "Killing a cow is not a fight");
          kill(context, dog, zombie);
          context.assertTrue(
              bond(dog).points() == DogBondRules.Source.FIGHT.points(),
              "The dog winning a fight beside its owner gives bond, got " + bond(dog).points());
          kill(context, owner, zombie);
          context.assertTrue(
              bond(dog).points() == 2 * DogBondRules.Source.FIGHT.points(),
              "The owner winning a fight beside the dog gives bond, got " + bond(dog).points());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDevotedDogKeepsUpOnFootFromFartherBeforeTeleporting(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER.add(0, 0, 16));
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.BEAGLE);

    context.runAtTick(
        2,
        () -> {
          context.assertTrue(
              dog.shouldTryTeleportToOwner(), "A Stranger 14 blocks away teleports to its owner");
          bond(dog).setPoints(DogBondStep.DEVOTED.minPoints());
          context.assertFalse(
              dog.shouldTryTeleportToOwner(), "A Devoted dog 14 blocks away keeps up on foot");
          owner.refreshPositionAndAngles(context.getAbsolute(REL_OWNER.add(0, 0, 20)), 0.0f, 0.0f);
          context.assertTrue(
              dog.shouldTryTeleportToOwner(), "A Devoted dog 18 blocks away teleports");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void onlyADevotedDogAnswersItsHurtOwner(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER.add(0, 0, 8));
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.HUSKY);
    final DevotedRescueGoal rescue = new DevotedRescueGoal(dog);

    context.runAtTick(
        2,
        () -> {
          bond(dog).setPoints(DogBondStep.DEVOTED.minPoints());
          context.assertFalse(rescue.canStart(), "An unhurt owner needs no rescue");
          owner.hurtTime = 10;
          context.assertTrue(rescue.canStart(), "A Devoted dog comes when its owner is hurt");
          bond(dog).setPoints(DogBondStep.COMPANION.minPoints());
          context.assertFalse(rescue.canStart(), "A Companion does not come running");
          bond(dog).setPoints(DogBondStep.DEVOTED.minPoints());
          dog.getCommandController().apply(DogCommand.STAY);
          context.assertFalse(rescue.canStart(), "A rescue never breaks a stay");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void bondResetsWhenTheDogChangesOwner(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.BEAGLE);

    context.runAtTick(2, () -> bond(dog).setPoints(DogBondStep.FRIEND.minPoints()));
    context.runAtTick(4, () -> dog.setOwnerUuid(UUID.randomUUID()));
    context.runAtTick(
        6,
        () -> {
          context.assertTrue(
              bond(dog).points() == 0,
              "A new owner starts as a Stranger, got " + bond(dog).points());
          context.assertTrue(bond(dog).step() == DogBondStep.STRANGER, "The synced step resets");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void bondSurvivesSavingAndLoading(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context, REL_OWNER);
    final UnleashedDogEntity dog = tamedStill(context, owner, ModEntities.BEAGLE);
    final UnleashedDogEntity reloaded =
        context.spawnEntity(ModEntities.BEAGLE, new BlockPos(1, 2, 1));
    reloaded.setAiDisabled(true);

    context.runAtTick(
        2,
        () -> {
          bond(dog).setPoints(DogBondStep.COMPANION.minPoints() + 5);
          final NbtCompound saved = new NbtCompound();
          dog.writeCustomDataToNbt(saved);
          reloaded.readCustomDataFromNbt(saved);
        });
    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              bond(reloaded).points() == DogBondStep.COMPANION.minPoints() + 5,
              "Bond points load back, got " + bond(reloaded).points());
          context.assertTrue(
              bond(reloaded).step() == DogBondStep.COMPANION, "The loaded dog shows its step");
          context.complete();
        });
  }

  private static void walkBackAndForth(final TestContext context, final UnleashedDogEntity dog) {
    final Vec3d start = context.getAbsolute(new Vec3d(1.5, 2, 3.5));
    context.runAtEveryTick(
        () -> {
          final long tick = context.getTick();
          if (tick < 1 || tick > WALK_TICKS) {
            return;
          }
          final long leg = tick % 16;
          final double offset = (leg < 8 ? leg : 16 - leg) * WALK_STEP;
          dog.refreshPositionAndAngles(start.x + offset, start.y, start.z, 0.0f, 0.0f);
        });
  }

  private static void kill(
      final TestContext context, final Entity killer, final LivingEntity killed) {
    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY
        .invoker()
        .afterKilledOtherEntity(context.getWorld(), killer, killed);
  }

  private static ServerPlayerEntity ownerAt(final TestContext context, final Vec3d relative) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    owner.refreshPositionAndAngles(context.getAbsolute(relative), 0.0f, 0.0f);
    return owner;
  }

  private static UnleashedDogEntity tamedStill(
      final TestContext context,
      final ServerPlayerEntity owner,
      final EntityType<UnleashedDogEntity> type) {
    final UnleashedDogEntity dog = context.spawnEntity(type, REL_DOG);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(DogCommand.FOLLOW);
    return dog;
  }

  private static DogBond bond(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogBond.class);
  }
}
