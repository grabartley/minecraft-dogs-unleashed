package com.grahambartley.dogsunleashed.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModCostumeItems;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RareTraderOfferTest {

  static Stream<Arguments> soldItems() {
    return Stream.of(
        Arguments.of(ModItems.PUPPY_BASKET, 1),
        Arguments.of(ModItems.PUPPY_BASKET, 64),
        Arguments.of(ModCostumeItems.of(DogCostume.SHEEP), 1),
        Arguments.of(ModCostumeItems.of(DogCostume.SHEEP), 64));
  }

  @ParameterizedTest
  @MethodSource("soldItems")
  void sellsOneItemOnceForItsPriceInEmeralds(final Item sold, final int emeralds) {
    final TradeOffer offer = RareTraderOffer.sellOnce(new ItemStack(sold), emeralds);
    assertEquals(Items.EMERALD, offer.getOriginalFirstBuyItem().getItem());
    assertEquals(emeralds, offer.getOriginalFirstBuyItem().getCount());
    assertEquals(sold, offer.getSellItem().getItem());
    assertEquals(1, offer.getSellItem().getCount());
    assertEquals(1, offer.getMaxUses());
    assertEquals(1, offer.getMerchantExperience());
    assertEquals(0.05F, offer.getPriceMultiplier());
  }
}
