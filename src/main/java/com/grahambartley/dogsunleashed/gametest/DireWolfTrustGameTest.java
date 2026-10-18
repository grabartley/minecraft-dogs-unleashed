package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.network.payload.WildTrustView;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DireWolfTrustGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_PUP = new BlockPos(4, FLOOR_Y, 4);
  private static final BlockPos REL_GUARD = new BlockPos(2, FLOOR_Y, 4);
  private static final BlockPos REL_FAR_ADULT = new BlockPos(1, FLOOR_Y, 0);
  private static final Vec3d REL_NEARBY = new Vec3d(10.5, FLOOR_Y, 4.5);
  private static final Vec3d REL_FAR_END = new Vec3d(15.5, FLOOR_Y, 4.5);
  private static final int FEED_GAP = 41;

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aWildAdultRefusesFoodAndKeepsIt(final TestContext context) {
    final UnleashedDogEntity adult = spawnPack(context, DireWolfGrowthStage.ADULT).get(0);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    final ItemStack beef = feed(adult, player, Items.BEEF, 1);
    context.assertFalse(player.isCreative(), "The test player must be in survival");
    context.assertTrue(beef.getCount() == 1, "The adult took the food");
    context.assertFalse(adult.isTamed(), "A wild adult was tamed");
    context.assertTrue(adult.getTrust().trustOf(player) == 0, "A wild adult gained trust");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = FEED_GAP * 5 + 20)
  public void aWildPupIsTamedOnlyWhenItsTrustIsFull(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    pup.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    for (int feed = 1; feed <= 5; feed++) {
      final int expected = feed * 20;
      context.runAtTick(
          (long) (feed - 1) * FEED_GAP + 1,
          () -> {
            feed(pup, player, Items.BEEF, 1);
            if (expected < 100) {
              context.assertFalse(pup.isTamed(), "The pup was tamed at " + expected + " trust");
              context.assertTrue(
                  pup.getTrust().trustOf(player) == expected,
                  "Trust should be " + expected + " but is " + pup.getTrust().trustOf(player));
            } else {
              context.assertTrue(
                  pup.isTamed() && player.getUuid().equals(pup.getOwnerUuid()),
                  "The pup should be tamed by the player at full trust");
              SurvivalTestPlayer.leave(context, player);
              context.complete();
            }
          });
    }
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aFeedInsideTheCooldownGivesNothing(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    pup.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    final ItemStack left = feed(pup, player, Items.BEEF, 2);
    context.assertTrue(left.getCount() == 1, "The first feed should use one piece of food");
    pup.interactMob(player, Hand.MAIN_HAND);
    context.assertTrue(left.getCount() == 1, "The second feed should keep the food");
    context.assertTrue(pup.getTrust().trustOf(player) == 20, "The second feed should give nothing");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void rawMeatEarnsMoreTrustThanBones(final TestContext context) {
    final List<UnleashedDogEntity> pups =
        spawnPack(context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.PUP);
    pups.forEach(pup -> pup.setAiDisabled(true));
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pups.get(0), player, Items.MUTTON, 1);
    feed(pups.get(1), player, Items.BONE, 1);
    context.assertTrue(pups.get(0).getTrust().trustOf(player) == 20, "Raw mutton gives 20");
    context.assertTrue(pups.get(1).getTrust().trustOf(player) == 10, "A bone gives 10");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 120)
  public void aSprintingPlayerDrawsTheNearestAdultBetweenThemAndThePup(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(
            context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    final UnleashedDogEntity pup = pack.get(0);
    final UnleashedDogEntity guard = pack.get(1);
    final UnleashedDogEntity farAdult = pack.get(2);
    pup.setAiDisabled(true);
    farAdult.setAiDisabled(true);
    moveTo(context, pup, REL_PUP);
    moveTo(context, guard, REL_GUARD);
    moveTo(context, farAdult, REL_FAR_ADULT);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pup, player, Items.BEEF, 1);
    player.setSprinting(true);
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              pup.getTrust().trustOf(player) == 5,
              "Spooking the pack should cost 15 trust, but trust is "
                  + pup.getTrust().trustOf(player));
          context.assertTrue(
              guard.distanceTo(player) < pup.distanceTo(player),
              "The nearest adult has not stepped between the player and the pup");
          context.assertTrue(guard.getTarget() == null, "The guard attacked the player");
          SurvivalTestPlayer.leave(context, player);
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 200)
  public void aGuardingAdultFightsOnceThePlayerHurtsThePack(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(
            context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.ADULT, DireWolfGrowthStage.ADULT);
    final UnleashedDogEntity pup = pack.get(0);
    final UnleashedDogEntity guard = pack.get(1);
    final UnleashedDogEntity farAdult = pack.get(2);
    pup.setAiDisabled(true);
    farAdult.setAiDisabled(true);
    moveTo(context, pup, REL_PUP);
    moveTo(context, guard, REL_GUARD);
    moveTo(context, farAdult, REL_FAR_ADULT);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    player.setSprinting(true);
    context.runAtTick(
        10,
        () -> farAdult.damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f));
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              guard.getTarget() == player, "The guarding adult never went for the player");
          SurvivalTestPlayer.leave(context, player);
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 120)
  public void aSneakingPlayerIsTolerated(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.ADULT);
    final UnleashedDogEntity pup = pack.get(0);
    pup.setAiDisabled(true);
    moveTo(context, pup, REL_PUP);
    moveTo(context, pack.get(1), REL_GUARD);
    final ServerPlayerEntity player =
        SurvivalTestPlayer.joinAt(context, Vec3d.ofBottomCenter(REL_PUP.east(2)));
    feed(pup, player, Items.BEEF, 1);
    player.setSneaking(true);
    context.runAtEveryTick(
        () ->
            context.assertTrue(
                pup.getTrust().trustOf(player) == 20, "A sneaking player spooked the pack"));
    context.runAtTick(
        100,
        () -> {
          SurvivalTestPlayer.leave(context, player);
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void hurtingAPupTurnsThePackAndWipesTrust(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(
            context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.PUP, DireWolfGrowthStage.ADULT);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pack.get(0), player, Items.BEEF, 1);
    feed(pack.get(1), player, Items.BEEF, 1);
    pack.get(0).damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);
    context.assertTrue(pack.get(0).getTrust().trustOf(player) == 0, "The hurt pup kept its trust");
    context.assertTrue(pack.get(1).getTrust().trustOf(player) == 0, "Its packmate kept its trust");
    context.assertTrue(pack.get(2).getTarget() == player, "The adult should attack the player");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = FEED_GAP + 220)
  public void aTrustedPupFollowsThePlayer(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    moveTo(context, pup, new BlockPos(1, FLOOR_Y, 4));
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_FAR_END);
    feed(pup, player, Items.BEEF, 1);
    context.runAtTick(FEED_GAP, () -> feed(pup, player, Items.BEEF, 1));
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              pup.distanceTo(player) <= 10.5,
              "A pup at 40 trust should follow, but it is " + pup.distanceTo(player) + " away");
          SurvivalTestPlayer.leave(context, player);
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void killsNearThePupEarnTrustWithACooldown(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    pup.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pup, player, Items.BEEF, 1);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, REL_PUP.east(3));
    zombie.setAiDisabled(true);
    zombie.damage(context.getWorld().getDamageSources().playerAttack(player), 1000.0f);
    context.assertTrue(
        pup.getTrust().trustOf(player) == 40,
        "A hostile kill should add 20, trust is " + pup.getTrust().trustOf(player));
    final SheepEntity sheep = context.spawnEntity(EntityType.SHEEP, REL_PUP.east(4));
    sheep.setAiDisabled(true);
    sheep.damage(context.getWorld().getDamageSources().playerAttack(player), 1000.0f);
    context.assertTrue(
        pup.getTrust().trustOf(player) == 40, "A second kill inside the cooldown added trust");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 260)
  public void trustFadesWhileThePlayerIsAway(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    pup.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pup, player, Items.BEEF, 1);
    final Vec3d away = context.getAbsolute(REL_NEARBY).add(40.0, 0.0, 0.0);
    player.refreshPositionAndAngles(away.x, away.y, away.z, 0.0f, 0.0f);
    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              pup.getTrust().trustOf(player) == 19,
              "Trust should fade by one, but is " + pup.getTrust().trustOf(player));
          SurvivalTestPlayer.leave(context, player);
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void trustSurvivesASaveAndLoad(final TestContext context) {
    final UnleashedDogEntity pup = spawnPack(context, DireWolfGrowthStage.PUP).get(0);
    pup.setAiDisabled(true);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    feed(pup, player, Items.BEEF, 1);
    final NbtCompound nbt = new NbtCompound();
    pup.writeNbt(nbt);
    final UnleashedDogEntity loaded = context.spawnEntity(ModEntities.DIRE_WOLF, REL_GUARD);
    loaded.setAiDisabled(true);
    loaded.readNbt(nbt);
    context.assertTrue(loaded.getTrust().trustOf(player) == 20, "Trust was lost on reload");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void theInspectCardShowsTrustOnlyToTheLookingPlayer(final TestContext context) {
    final List<UnleashedDogEntity> pack =
        spawnPack(context, DireWolfGrowthStage.PUP, DireWolfGrowthStage.ADULT);
    pack.forEach(wolf -> wolf.setAiDisabled(true));
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, REL_GUARD);
    husky.setAiDisabled(true);
    final ServerPlayerEntity feeder = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_FAR_END);
    feed(pack.get(0), feeder, Items.BEEF, 1);
    context.assertTrue(
        ModNetworking.trustView(feeder, pack.get(0)).equals(WildTrustView.building(20)),
        "The feeder should see 20 trust");
    context.assertTrue(
        ModNetworking.trustView(stranger, pack.get(0)).equals(WildTrustView.building(0)),
        "Another player should see their own 0 trust");
    context.assertTrue(
        ModNetworking.trustView(feeder, pack.get(1)) == WildTrustView.TOO_WILD,
        "A wild adult should show as too wild");
    context.assertTrue(
        ModNetworking.trustView(feeder, husky) == WildTrustView.NONE, "Other breeds show no trust");
    SurvivalTestPlayer.leave(context, feeder);
    SurvivalTestPlayer.leave(context, stranger);
    context.complete();
  }

  private static ItemStack feed(
      final UnleashedDogEntity wolf,
      final ServerPlayerEntity player,
      final Item food,
      final int count) {
    final ItemStack stack = new ItemStack(food, count);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    wolf.interactMob(player, Hand.MAIN_HAND);
    return stack;
  }

  private static void moveTo(
      final TestContext context, final UnleashedDogEntity wolf, final BlockPos relative) {
    final Vec3d pos = context.getAbsolute(Vec3d.ofBottomCenter(relative));
    wolf.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0f, 0.0f);
  }

  private static List<UnleashedDogEntity> spawnPack(
      final TestContext context, final DireWolfGrowthStage... stages) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    EntityData data = null;
    for (int i = 0; i < stages.length; i++) {
      final UnleashedDogEntity wolf =
          context.spawnEntity(ModEntities.DIRE_WOLF, REL_PUP.south(i % 4).west(i / 4));
      data =
          wolf.initialize(
              context.getWorld(),
              context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
              SpawnReason.NATURAL,
              data);
      wolf.getGrowth().setStage(stages[i]);
      pack.add(wolf);
    }
    return pack;
  }
}
