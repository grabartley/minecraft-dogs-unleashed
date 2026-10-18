package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.PackGuardGoal;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PackGuardGoalGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final BlockPos REL_PUP = new BlockPos(4, FLOOR_Y, 4);
  private static final BlockPos REL_GUARD = new BlockPos(2, FLOOR_Y, 4);
  private static final Vec3d REL_NEARBY = new Vec3d(10.5, FLOOR_Y, 4.5);
  private static final int TRUST_AFTER_FEED = 20;

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void everySwordAxeAndConventionWeaponCountsAsAWeapon(final TestContext context) {
    final List<TagKey<Item>> weaponTags =
        List.of(
            ItemTags.SWORDS,
            ItemTags.AXES,
            ConventionalItemTags.MELEE_WEAPON_TOOLS,
            ConventionalItemTags.RANGED_WEAPON_TOOLS);
    for (final TagKey<Item> tag : weaponTags) {
      for (final RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(tag)) {
        context.assertTrue(
            PackGuardGoal.isWeapon(new ItemStack(entry)),
            entry.getIdAsString() + " in " + tag.id() + " should count as a weapon");
      }
    }
    for (final Item item : List.of(Items.TRIDENT, Items.BOW, Items.CROSSBOW, Items.MACE)) {
      context.assertTrue(
          PackGuardGoal.isWeapon(new ItemStack(item)), item + " should count as a weapon");
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void foodAndToolsAreNotWeapons(final TestContext context) {
    for (final Item item : List.of(Items.BEEF, Items.STICK, Items.IRON_PICKAXE, Items.SHEARS)) {
      context.assertFalse(
          PackGuardGoal.isWeapon(new ItemStack(item)), item + " should not count as a weapon");
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 60)
  public void walkingUpWithAnAxeSpooksThePack(final TestContext context) {
    final UnleashedDogEntity pup = this.guardedPup(context);
    final ServerPlayerEntity player = this.fedPlayer(context, pup, Items.IRON_AXE);
    context.runAtEveryTick(
        () -> {
          if (pup.getTrust().trustOf(player) < TRUST_AFTER_FEED) {
            SurvivalTestPlayer.leave(context, player);
            context.complete();
          }
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 60)
  public void walkingUpWithFoodLeavesThePackCalm(final TestContext context) {
    final UnleashedDogEntity pup = this.guardedPup(context);
    final ServerPlayerEntity player = this.fedPlayer(context, pup, Items.BEEF);
    context.runAtEveryTick(
        () ->
            context.assertTrue(
                pup.getTrust().trustOf(player) == TRUST_AFTER_FEED,
                "A player holding food spooked the pack"));
    context.runAtTick(
        50,
        () -> {
          SurvivalTestPlayer.leave(context, player);
          context.complete();
        });
  }

  private UnleashedDogEntity guardedPup(final TestContext context) {
    final UnleashedDogEntity pup = context.spawnEntity(ModEntities.DIRE_WOLF, REL_PUP);
    final EntityData data = initialize(context, pup, DireWolfGrowthStage.PUP, null);
    initialize(
        context,
        context.spawnEntity(ModEntities.DIRE_WOLF, REL_GUARD),
        DireWolfGrowthStage.ADULT,
        data);
    pup.setAiDisabled(true);
    return pup;
  }

  private ServerPlayerEntity fedPlayer(
      final TestContext context, final UnleashedDogEntity pup, final Item held) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_NEARBY);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BEEF));
    pup.interactMob(player, Hand.MAIN_HAND);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(held));
    return player;
  }

  private static EntityData initialize(
      final TestContext context,
      final UnleashedDogEntity wolf,
      final DireWolfGrowthStage stage,
      final EntityData data) {
    final EntityData packData =
        wolf.initialize(
            context.getWorld(),
            context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
            SpawnReason.NATURAL,
            data);
    wolf.getGrowth().setStage(stage);
    return packData;
  }
}
