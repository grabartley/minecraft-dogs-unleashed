package com.grahambartley.dogsunleashed.trade;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import org.jetbrains.annotations.Nullable;

public final class TraderPuppyOffer implements TradeOffers.Factory {

  public static final int RARE_POOL = 2;
  public static final int PRICE_EMERALDS = 12;
  public static final int MAX_USES = 1;
  public static final int MERCHANT_EXPERIENCE = 1;
  public static final float PRICE_MULTIPLIER = 0.05F;

  public static void register() {
    TradeOfferHelper.registerWanderingTraderOffers(
        RARE_POOL, factories -> factories.add(new TraderPuppyOffer()));
  }

  @Override
  public @Nullable TradeOffer create(final Entity entity, final Random random) {
    if (DogsUnleashed.SERVER_CONFIG.spawning().traderPuppies()) {
      return new TradeOffer(
          new TradedItem(Items.EMERALD, PRICE_EMERALDS),
          new ItemStack(ModItems.PUPPY_BASKET),
          MAX_USES,
          MERCHANT_EXPERIENCE,
          PRICE_MULTIPLIER);
    }
    final List<TradeOffers.Factory> others = otherRareOffers();
    return others.isEmpty()
        ? null
        : others.get(random.nextInt(others.size())).create(entity, random);
  }

  private static List<TradeOffers.Factory> otherRareOffers() {
    final TradeOffers.Factory[] pool = TradeOffers.WANDERING_TRADER_TRADES.get(RARE_POOL);
    if (pool == null) {
      return List.of();
    }
    return Arrays.stream(pool).filter(factory -> !(factory instanceof TraderPuppyOffer)).toList();
  }
}
