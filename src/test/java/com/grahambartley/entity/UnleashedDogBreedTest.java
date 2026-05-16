package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnleashedDogBreedTest {

  @Test
  @DisplayName("Breed parser should support canonical ids and legacy aliases")
  void testBreedAliases() {
    assertEquals(UnleashedDogBreed.HUSKY, UnleashedDogBreed.fromSerializedId("husky"));
    assertEquals(
        UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.fromSerializedId("goldenretriever"));
    assertEquals(
        UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.fromSerializedId("golden_retriever"));
    assertEquals(UnleashedDogBreed.SHIBA_INU, UnleashedDogBreed.fromSerializedId("shibainu"));
    assertEquals(UnleashedDogBreed.SHIBA_INU, UnleashedDogBreed.fromSerializedId("shiba_inu"));
  }

  @Test
  @DisplayName("Unknown breed ids should safely fall back or return null")
  void testUnknownBreedFallback() {
    assertEquals(UnleashedDogBreed.HUSKY, UnleashedDogBreed.fromSerializedId("???"));
    assertNull(UnleashedDogBreed.fromSerializedIdOrNull("???"));
    assertNull(UnleashedDogBreed.fromSerializedIdOrNull(null));
  }
}
