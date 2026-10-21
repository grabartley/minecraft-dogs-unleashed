package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.BowlFill;
import com.grahambartley.dogsunleashed.block.FoodBowlBlock;
import com.grahambartley.dogsunleashed.block.WaterBowlBlock;
import com.grahambartley.dogsunleashed.block.entity.FoodBowlBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogBowlMeal;
import com.grahambartley.dogsunleashed.entity.DogFoods;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogBowlGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_BOWL = new BlockPos(1, 2, 1);
  private static final BlockPos REL_HOPPER = REL_BOWL.up();
  private static final BlockPos REL_DOG = new BlockPos(5, 2, 4);
  private static final Vec3d REL_PLAYER = new Vec3d(3.5, 2, 5.5);
  private static final float HURT_HEALTH = 4.0F;
  private static final int HOPPER_FOOD = 20;
  private static final int BOWL_FOOD = 4;
  private static final int MEALS_TO_WATCH = 2;
  private static final int WATCH_TICKS = 240;
  private static final int BURN_TICKS = 400;

  private static UnleashedDogEntity hurtDog(final TestContext context) {
    final ServerPlayerEntity owner = DogTestHelper.mockPlayerStandingClearInArena(context);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, REL_DOG, owner.getUuid());
    dog.setHealth(HURT_HEALTH);
    return dog;
  }

  private static FoodBowlBlockEntity placeFoodBowl(final TestContext context, final int food) {
    context.setBlockState(REL_BOWL, ModBlocks.FOOD_BOWL);
    final FoodBowlBlockEntity bowl = context.getBlockEntity(REL_BOWL);
    bowl.addFood(new ItemStack(Items.COOKED_BEEF, food));
    return bowl;
  }

  private static int mealsEaten(final UnleashedDogEntity dog) {
    return Math.round((dog.getHealth() - HURT_HEALTH) / DogFoods.HEAL_AMOUNT);
  }

  @GameTest(templateName = ARENA, tickLimit = 400, batchId = "bowl-food")
  public void aHurtDogWalksToTheFoodBowlAndHeals(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, BOWL_FOOD);
    final UnleashedDogEntity dog = hurtDog(context);
    context.runAtEveryTick(
        () -> {
          final int eaten = mealsEaten(dog);
          context.assertTrue(
              bowl.foodCount() == BOWL_FOOD - eaten,
              "The dog healed " + eaten + " meals but the bowl holds " + bowl.foodCount());
          if (eaten >= 1) {
            context.complete();
          }
        });
  }

  private static DogBowlMeal meal(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogBowlMeal.class);
  }

  @GameTest(templateName = ARENA, tickLimit = 400, batchId = "bowl-meal-state")
  public void aDogIsEatingOnlyWhileItsMealLasts(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, BOWL_FOOD);
    final UnleashedDogEntity dog = hurtDog(context);
    final boolean[] sawEating = {false};
    context.runAtEveryTick(
        () -> {
          if (meal(dog).isEating()) {
            context.assertTrue(mealsEaten(dog) == 0, "The dog was still eating after its meal");
            sawEating[0] = true;
            return;
          }
          if (mealsEaten(dog) >= 1) {
            context.assertTrue(sawEating[0], "The dog healed without ever eating");
            context.assertTrue(bowl.foodCount() == BOWL_FOOD - 1, "One meal should take one food");
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 400, batchId = "bowl-meal-cut-short")
  public void dangerCutsAMealShortAndClearsTheEatingState(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, BOWL_FOOD);
    final UnleashedDogEntity dog = hurtDog(context);
    final boolean[] setAlight = {false};
    context.runAtEveryTick(
        () -> {
          if (!setAlight[0]) {
            if (meal(dog).isEating()) {
              dog.addStatusEffect(
                  new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, BURN_TICKS));
              dog.setFireTicks(BURN_TICKS);
              setAlight[0] = true;
            }
            return;
          }
          if (!meal(dog).isEating()) {
            context.assertTrue(
                bowl.foodCount() == BOWL_FOOD, "A meal cut short should not take any food");
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 600, batchId = "bowl-hopper")
  public void foodFromAHopperIsNeverDuplicatedOnTheWayToTheDog(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, 0);
    context.setBlockState(REL_HOPPER, Blocks.HOPPER);
    final HopperBlockEntity hopper = context.getBlockEntity(REL_HOPPER);
    hopper.setStack(0, new ItemStack(Items.COOKED_BEEF, HOPPER_FOOD));
    final UnleashedDogEntity dog = hurtDog(context);
    context.runAtEveryTick(
        () -> {
          final int inHopper = hopper.getStack(0).getCount();
          final int eaten = mealsEaten(dog);
          final int total = inHopper + bowl.foodCount() + eaten;
          context.assertTrue(
              total == HOPPER_FOOD,
              "Food was "
                  + (total > HOPPER_FOOD ? "duplicated" : "lost")
                  + ": hopper "
                  + inHopper
                  + ", bowl "
                  + bowl.foodCount()
                  + ", eaten "
                  + eaten);
          context.assertTrue(
              bowl.foodCount() <= 16, "The bowl holds " + bowl.foodCount() + ", over sixteen");
          if (eaten >= MEALS_TO_WATCH && inHopper < HOPPER_FOOD) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 400, batchId = "bowl-water")
  public void aHurtDogDrinksFromTheWaterBowlAndRegenerates(final TestContext context) {
    context.setBlockState(
        REL_BOWL, ModBlocks.WATER_BOWL.getDefaultState().with(WaterBowlBlock.DRINKS, 3));
    final UnleashedDogEntity dog = hurtDog(context);
    context.runAtEveryTick(
        () -> {
          if (dog.hasStatusEffect(StatusEffects.REGENERATION)) {
            context.assertTrue(
                context.getBlockState(REL_BOWL).get(WaterBowlBlock.DRINKS) == 2,
                "One drink should leave two in the bowl");
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 260, batchId = "bowl-idle")
  public void aDogAtFullHealthLeavesTheBowlAlone(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, BOWL_FOOD);
    final UnleashedDogEntity dog = hurtDog(context);
    dog.setHealth(dog.getMaxHealth());
    context.runAtTick(
        WATCH_TICKS,
        () -> {
          context.assertTrue(bowl.foodCount() == BOWL_FOOD, "A healthy dog ate from the bowl");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 260, batchId = "bowl-burning")
  public void aBurningDogLeavesTheBowlAlone(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, BOWL_FOOD);
    final UnleashedDogEntity dog = hurtDog(context);
    dog.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, BURN_TICKS));
    dog.setFireTicks(BURN_TICKS);
    context.runAtTick(
        WATCH_TICKS,
        () -> {
          context.assertTrue(dog.isOnFire(), "The dog stopped burning before the check");
          context.assertTrue(bowl.foodCount() == BOWL_FOOD, "A burning dog stopped to eat");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHandfulOfFoodFillsTheBowlUpToSixteen(final TestContext context) {
    final FoodBowlBlockEntity bowl = placeFoodBowl(context, 0);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ItemStack beef = new ItemStack(Items.BEEF, HOPPER_FOOD);
    player.setStackInHand(Hand.MAIN_HAND, beef);
    context.useBlock(REL_BOWL, player);
    context.assertTrue(bowl.foodCount() == 16, "The bowl holds " + bowl.foodCount());
    context.assertTrue(beef.getCount() == HOPPER_FOOD - 16, "Left " + beef.getCount() + " beef");
    context.assertTrue(
        context.getBlockState(REL_BOWL).get(FoodBowlBlock.FILL) == BowlFill.FULL,
        "A full bowl should look full");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWaterBucketFillsTheBowlAndHandsBackTheBucket(final TestContext context) {
    context.setBlockState(REL_BOWL, ModBlocks.WATER_BOWL);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
    context.useBlock(REL_BOWL, player);
    context.assertTrue(
        context.getBlockState(REL_BOWL).get(WaterBowlBlock.DRINKS) == 3,
        "A bucket should fill the bowl");
    context.assertTrue(
        player.getMainHandStack().isOf(Items.BUCKET), "The player should get the bucket back");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWaterBottleAddsOneDrink(final TestContext context) {
    context.setBlockState(REL_BOWL, ModBlocks.WATER_BOWL);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    final ItemStack bottle = new ItemStack(Items.POTION);
    bottle.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.WATER));
    player.setStackInHand(Hand.MAIN_HAND, bottle);
    context.useBlock(REL_BOWL, player);
    context.assertTrue(
        context.getBlockState(REL_BOWL).get(WaterBowlBlock.DRINKS) == 1,
        "A bottle should add one drink");
    context.assertTrue(
        player.getMainHandStack().isOf(Items.GLASS_BOTTLE),
        "The player should get the glass bottle back");
    SurvivalTestPlayer.leave(context, player);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void breakingTheBowlDropsTheFoodInIt(final TestContext context) {
    placeFoodBowl(context, BOWL_FOOD);
    context.getWorld().breakBlock(context.getAbsolutePos(REL_BOWL), false);
    final int dropped =
        DogTestHelper.droppedStacksOf(context, Items.COOKED_BEEF).stream()
            .mapToInt(ItemStack::getCount)
            .sum();
    context.assertTrue(dropped == BOWL_FOOD, "Dropped " + dropped + " of " + BOWL_FOOD + " beef");
    context.complete();
  }
}
