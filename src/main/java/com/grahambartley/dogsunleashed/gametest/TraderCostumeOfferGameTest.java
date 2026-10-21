package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItemTags;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.trade.RareTraderOffer;
import com.grahambartley.dogsunleashed.trade.TraderCostumeOffer;
import com.grahambartley.dogsunleashed.trade.TraderPuppyOffer;
import java.util.Arrays;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

public final class TraderCostumeOfferGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_TRADER = new BlockPos(3, 2, 3);
  private static final int ROLLS = 50;
  private static final int FALLBACK_ROLLS = 500;
  private static final long SEED = 565L;

  private static final Map<String, Integer> EXPECTED_PRICES =
      Map.ofEntries(
          Map.entry("sheep_costume", 3),
          Map.entry("allay_costume", 5),
          Map.entry("axolotl_costume", 5),
          Map.entry("chicken_costume", 5),
          Map.entry("creeper_costume", 5),
          Map.entry("enderman_costume", 5),
          Map.entry("frog_costume", 5),
          Map.entry("mooshroom_costume", 5),
          Map.entry("skeleton_costume", 5),
          Map.entry("turtle_costume", 5),
          Map.entry("zombie_costume", 5),
          Map.entry("bee_costume", 5),
          Map.entry("panda_costume", 5),
          Map.entry("spider_costume", 7),
          Map.entry("wither_costume", 11),
          Map.entry("ender_dragon_costume", 13),
          Map.entry("warden_costume", 13),
          Map.entry("nautilus_shell", 11));

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void theRareTraderPoolHoldsOneCostumeOfferBesideThePuppyBasket(final TestContext context) {
    final TradeOffers.Factory[] pool =
        TradeOffers.WANDERING_TRADER_TRADES.get(RareTraderOffer.RARE_POOL);
    final long costumes = Arrays.stream(pool).filter(TraderCostumeOffer.class::isInstance).count();
    final long baskets = Arrays.stream(pool).filter(TraderPuppyOffer.class::isInstance).count();
    context.assertTrue(
        costumes == 1, "Expected one costume offer in the rare pool, not " + costumes);
    context.assertTrue(
        baskets == 1, "Expected one puppy basket offer in the rare pool, not " + baskets);
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void everyTaggedCostumeIsPricedByItsRecipe(final TestContext context) {
    int tagged = 0;
    for (final RegistryEntry<Item> entry :
        Registries.ITEM.iterateEntries(ModItemTags.DOG_COSTUMES)) {
      tagged++;
      final Item costume = entry.value();
      final Integer expected = EXPECTED_PRICES.get(Registries.ITEM.getId(costume).getPath());
      context.assertTrue(expected != null, "No expected price for " + costume);
      context.assertEquals(
          TraderCostumeOffer.priceOf(context.getWorld(), costume), expected, "Price of " + costume);
    }
    context.assertEquals(tagged, EXPECTED_PRICES.size(), "Tagged costume count");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aTraderRollsATaggedCostumeForEmeralds(final TestContext context) {
    final WanderingTraderEntity trader =
        context.spawnEntity(EntityType.WANDERING_TRADER, REL_TRADER);
    trader.setAiDisabled(true);
    for (int roll = 0; roll < ROLLS; roll++) {
      final TradeOffer offer = new TraderCostumeOffer().create(trader, trader.getRandom());
      context.assertTrue(offer != null, "The costume offer rolled nothing");
      final Item costume = offer.getSellItem().getItem();
      context.assertTrue(
          offer.getSellItem().isIn(ModItemTags.DOG_COSTUMES), "The trader sold " + costume);
      context.assertTrue(
          offer.getOriginalFirstBuyItem().isOf(Items.EMERALD),
          "The costume is not sold for emeralds");
      context.assertEquals(
          offer.getOriginalFirstBuyItem().getCount(),
          EXPECTED_PRICES.get(Registries.ITEM.getId(costume).getPath()),
          "Price of " + costume);
    }
    trader.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void withTraderPuppiesOffTheRareSlotCanRollACostume(final TestContext context) {
    final WanderingTraderEntity trader =
        context.spawnEntity(EntityType.WANDERING_TRADER, REL_TRADER);
    trader.setAiDisabled(true);
    final DogsUnleashedConfig before = DogsUnleashed.SERVER_CONFIG;
    final Random random = Random.create(SEED);
    boolean rolledCostume = false;
    try {
      DogsUnleashed.SERVER_CONFIG = before.withSpawning(before.spawning().withTraderPuppies(false));
      for (int roll = 0; roll < FALLBACK_ROLLS && !rolledCostume; roll++) {
        final TradeOffer offer = new TraderPuppyOffer().create(trader, random);
        rolledCostume = offer != null && offer.getSellItem().isIn(ModItemTags.DOG_COSTUMES);
      }
    } finally {
      DogsUnleashed.SERVER_CONFIG = before;
    }
    trader.discard();
    context.assertTrue(rolledCostume, "The basket fallback never rolled a costume");
    context.complete();
  }
}
