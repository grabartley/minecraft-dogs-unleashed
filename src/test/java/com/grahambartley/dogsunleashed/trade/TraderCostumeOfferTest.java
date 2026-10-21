package com.grahambartley.dogsunleashed.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModCostumeItems;
import com.grahambartley.dogsunleashed.item.costume.DogCostume;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TraderCostumeOfferTest {

  static Stream<Arguments> costumesAndPrices() {
    return Arrays.stream(DogCostume.values())
        .flatMap(costume -> Stream.of(3, 13, 64).map(price -> Arguments.of(costume, price)));
  }

  @ParameterizedTest
  @MethodSource("costumesAndPrices")
  void sellsOneCostumeForItsPriceInEmeralds(final DogCostume costume, final int emeralds) {
    final TradeOffer offer = TraderCostumeOffer.offerFor(ModCostumeItems.of(costume), emeralds);
    assertEquals(Items.EMERALD, offer.getOriginalFirstBuyItem().getItem());
    assertEquals(emeralds, offer.getOriginalFirstBuyItem().getCount());
    assertEquals(ModCostumeItems.of(costume), offer.getSellItem().getItem());
    assertEquals(1, offer.getSellItem().getCount());
    assertEquals(1, offer.getMaxUses());
  }
}
