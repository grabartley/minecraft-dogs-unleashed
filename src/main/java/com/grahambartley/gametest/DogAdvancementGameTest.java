package com.grahambartley.gametest;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.ModItems;
import com.grahambartley.advancement.HuskyHowledCriterion;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import com.grahambartley.entity.goal.FetchReturnGoal;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogAdvancementGameTest implements FabricGameTest {
  private static final List<String> ADVANCEMENT_IDS =
      List.of(
          "root",
          "best_friend",
          "the_whole_pack",
          "bark_at_the_moon",
          "fetch",
          "sweet_dreams",
          "forever_in_our_hearts",
          "cherry_companion");

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void advancementsLoadFromDataPackResources(final TestContext context) {
    for (final String advancementId : ADVANCEMENT_IDS) {
      context.assertTrue(
          getAdvancement(context, advancementId) != null,
          "Advancement should load: " + advancementId);
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamingUnlocksBreedAdvancements(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final ServerPlayerEntity player =
        (ServerPlayerEntity) context.createMockPlayer(GameMode.SURVIVAL);

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(context.getAbsolutePos(new BlockPos(1, 1, 0)), 0.0f, 0.0f);
    world.spawnEntity(husky);

    final ShibaInuEntity shiba = new ShibaInuEntity(ModEntities.SHIBA_INU, world);
    shiba.refreshPositionAndAngles(context.getAbsolutePos(new BlockPos(2, 1, 0)), 0.0f, 0.0f);
    world.spawnEntity(shiba);

    final AdvancementEntry bestFriend = getAdvancement(context, "best_friend");
    final AdvancementEntry cherryCompanion = getAdvancement(context, "cherry_companion");

    Criteria.TAME_ANIMAL.trigger(player, husky);
    Criteria.TAME_ANIMAL.trigger(player, shiba);

    context.assertTrue(
        player.getAdvancementTracker().getProgress(bestFriend).isDone(),
        "Taming a new breed should unlock best_friend");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(cherryCompanion).isDone(),
        "Taming a shiba inu should unlock cherry_companion");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void wholePackRequiresAllFiveBreeds(final TestContext context) {
    final ServerPlayerEntity player =
        (ServerPlayerEntity) context.createMockPlayer(GameMode.SURVIVAL);
    final AdvancementEntry wholePack = getAdvancement(context, "the_whole_pack");

    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_husky");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_dachshund");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_beagle");
    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_goldenretriever");

    context.assertTrue(
        !player.getAdvancementTracker().getProgress(wholePack).isDone(),
        "the_whole_pack should stay locked until every breed is tamed");

    player.getAdvancementTracker().grantCriterion(wholePack, "tamed_shibainu");

    context.assertTrue(
        player.getAdvancementTracker().getProgress(wholePack).isDone(),
        "the_whole_pack should unlock after every breed criterion is complete");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void customTriggersUnlockTheirAdvancements(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();
    final ServerPlayerEntity player =
        (ServerPlayerEntity) context.createMockPlayer(GameMode.SURVIVAL);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final AdvancementEntry barkAtTheMoon = getAdvancement(context, "bark_at_the_moon");
    final AdvancementEntry fetch = getAdvancement(context, "fetch");
    final AdvancementEntry sweetDreams = getAdvancement(context, "sweet_dreams");
    final AdvancementEntry foreverInOurHearts = getAdvancement(context, "forever_in_our_hearts");

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX() + 1.0, absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    husky.setOwnerUuid(player.getUuid());
    husky.setCarryingFetchItem(true);
    husky.setActiveFetchType(FetchTypes.TENNIS_BALL);
    husky.setCarriedFetchItemStack(new ItemStack(ModItems.TENNIS_BALL));
    world.spawnEntity(husky);

    HuskyHowledCriterion.INSTANCE.trigger(player);
    new FetchReturnGoal(husky).tick();
    husky.startSleepingInBed(absBedPos);

    final ItemStack graveStack = new ItemStack(ModItems.DOG_GRAVE);
    player.getInventory().setStack(0, graveStack);
    Criteria.INVENTORY_CHANGED.trigger(player, player.getInventory(), graveStack);

    context.assertTrue(
        player.getAdvancementTracker().getProgress(barkAtTheMoon).isDone(),
        "husky_howled should unlock bark_at_the_moon");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(fetch).isDone(),
        "fetch_returned should unlock fetch");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(sweetDreams).isDone(),
        "dog_slept_in_bed should unlock sweet_dreams");
    context.assertTrue(
        player.getAdvancementTracker().getProgress(foreverInOurHearts).isDone(),
        "inventory_changed with a dog grave should unlock forever_in_our_hearts");
    context.complete();
  }

  private static AdvancementEntry getAdvancement(final TestContext context, final String path) {
    final AdvancementEntry advancement =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, path));
    context.assertTrue(advancement != null, "Missing advancement: " + path);
    return advancement;
  }
}
