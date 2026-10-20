package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.network.payload.ConnectionDogSyncData;
import com.grahambartley.dogsunleashed.network.payload.GeneSyncData;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.network.payload.RidingView;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.screen.FamilyTreeGeneLines.GeneLine;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyTreeGeneLinesTest {

  private static final DogGeneLocus HUSKY_COAT =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
  private static final DogGeneLocus HUSKY_EYES =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES);
  private static final FamilyTreeGeneLines.Translator KEYS =
      (key, args) ->
          args.length == 0
              ? key
              : key
                  + Arrays.stream(args)
                      .map(String::valueOf)
                      .collect(Collectors.joining("|", "(", ")"));
  private static final GeneSyncData WHITE_CARRIER =
      new GeneSyncData(
          HUSKY_COAT, HuskyCoat.BLACK_WHITE.ordinal(), List.of(HuskyCoat.WHITE.ordinal()));
  private static final GeneSyncData BLUE_EYES =
      new GeneSyncData(HUSKY_EYES, HuskyEyeColor.BLUE_BLUE.ordinal(), List.of());

  @Test
  @DisplayName("a dog that has bred lists what it shows and what it carries")
  void bredDogListsShownAndCarriedGenes() {
    assertEquals(
        List.of(
            new GeneLine(FamilyTreeGeneLines.GENES_KEY, FamilyTreeGeneLines.HEADER_COLOR),
            new GeneLine(
                FamilyTreeGeneLines.GENE_KEY
                    + "(gene.dogs-unleashed.coat|coat.dogs-unleashed.husky.black_white)",
                FamilyTreeGeneLines.GENE_COLOR),
            new GeneLine(
                FamilyTreeGeneLines.CARRIES_KEY + "(coat.dogs-unleashed.husky.white)",
                FamilyTreeGeneLines.CARRIER_COLOR),
            new GeneLine(
                FamilyTreeGeneLines.GENE_KEY
                    + "(gene.dogs-unleashed.eyes|eyes.dogs-unleashed.husky.blue_blue)",
                FamilyTreeGeneLines.GENE_COLOR)),
        FamilyTreeGeneLines.of(dog(List.of(WHITE_CARRIER, BLUE_EYES), true), KEYS));
  }

  @Test
  @DisplayName("a dog that has not bred lists what it shows and says how to see the rest")
  void unbredDogHintsAtBreeding() {
    final List<GeneLine> lines = FamilyTreeGeneLines.of(dog(List.of(BLUE_EYES), false), KEYS);
    assertEquals(
        new GeneLine(FamilyTreeGeneLines.HIDDEN_KEY, FamilyTreeGeneLines.HINT_COLOR),
        lines.getLast());
    assertEquals(3, lines.size());
  }

  @Test
  @DisplayName("a dog with no genes lists nothing")
  void dogWithoutGenesListsNothing() {
    assertTrue(FamilyTreeGeneLines.of(dog(List.of(), false), KEYS).isEmpty());
  }

  @Test
  @DisplayName("a dog is marked as a carrier only when a carried gene is known")
  void carrierMarkNeedsAKnownCarriedGene() {
    assertTrue(FamilyTreeGeneLines.isCarrier(dog(List.of(BLUE_EYES, WHITE_CARRIER), true)));
    assertFalse(FamilyTreeGeneLines.isCarrier(dog(List.of(BLUE_EYES), true)));
  }

  private static ConnectionDogSyncData dog(final List<GeneSyncData> genes, final boolean revealed) {
    return new ConnectionDogSyncData(
        new PetSyncData(
            "id",
            UnleashedDogBreed.HUSKY,
            "Luna",
            20,
            20,
            0,
            64,
            0,
            "minecraft:overworld",
            PetLifeState.LIVING,
            false,
            0,
            0,
            0,
            DogFurStage.NATURAL.ordinal(),
            0.3f,
            4f,
            List.of(),
            RidingView.NONE,
            false,
            false),
        "owner",
        "Owner",
        genes,
        revealed);
  }
}
