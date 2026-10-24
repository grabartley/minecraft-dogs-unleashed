package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PuppyBasketRulesTest {

  @Test
  void holdsEveryFoundingBreedButNeverADireWolf() {
    assertEquals(
        List.of(
            UnleashedDogBreed.HUSKY,
            UnleashedDogBreed.DACHSHUND,
            UnleashedDogBreed.BEAGLE,
            UnleashedDogBreed.GOLDEN_RETRIEVER,
            UnleashedDogBreed.SHIBA_INU),
        PuppyBasketRules.BREEDS);
  }

  @ParameterizedTest
  @CsvSource({
    "0, HUSKY",
    "1, DACHSHUND",
    "2, BEAGLE",
    "3, GOLDEN_RETRIEVER",
    "4, SHIBA_INU",
    "5, HUSKY",
    "-1, SHIBA_INU",
    "2147483647, BEAGLE"
  })
  void anyRollPicksABreed(final int roll, final UnleashedDogBreed breed) {
    assertEquals(breed, PuppyBasketRules.pick(roll));
  }
}
