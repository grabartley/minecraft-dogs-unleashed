package com.grahambartley.dogsunleashed.trade;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public final class RareTraderOffer {

  public static final int RARE_POOL = 2;
  public static final int MAX_USES = 1;
  public static final int MERCHANT_EXPERIENCE = 1;
  public static final float PRICE_MULTIPLIER = 0.05F;

  private RareTraderOffer() {}

  public static TradeOffer sellOnce(final ItemStack sold, final int emeralds) {
    return new TradeOffer(
        new TradedItem(Items.EMERALD, emeralds),
        sold,
        MAX_USES,
        MERCHANT_EXPERIENCE,
        PRICE_MULTIPLIER);
  }
}
