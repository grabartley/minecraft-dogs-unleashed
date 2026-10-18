package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogGenesGameTest implements FabricGameTest {

  private static final BlockPos PARENT_POS = new BlockPos(1, 2, 1);
  private static final BlockPos OTHER_PARENT_POS = new BlockPos(3, 2, 1);
  private static final DogGeneLocus HUSKY_COAT =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
  private static final DogGeneLocus HUSKY_EYES =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES);
  private static final AllelePair WHITE_CARRIER =
      new AllelePair(HuskyCoat.BLACK_WHITE.ordinal(), HuskyCoat.WHITE.ordinal());
  private static final int LITTERS = 64;
  private static final double MIN_INHERITED_SHARE = 0.9;

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void pupsOfTwoCarriersTakeTheirAllelesFromTheirParents(TestContext context) {
    final UnleashedDogEntity parent = spawnCarrier(context, PARENT_POS);
    final UnleashedDogEntity otherParent = spawnCarrier(context, OTHER_PARENT_POS);
    final Set<Integer> parentAlleles = Set.of(WHITE_CARRIER.first(), WHITE_CARRIER.second());

    int inherited = 0;
    for (int litter = 0; litter < LITTERS; litter++) {
      final AllelePair pair =
          breed(context, parent, otherParent).getGenes().genes().pair(HUSKY_COAT);
      context.assertTrue(pair != null, "Every husky pup should carry a husky coat gene");
      inherited += parentAlleles.contains(pair.first()) ? 1 : 0;
      inherited += parentAlleles.contains(pair.second()) ? 1 : 0;
    }

    final int alleles = LITTERS * 2;
    context.assertTrue(
        inherited >= alleles * MIN_INHERITED_SHARE,
        "Pups should take their coat alleles from their parents apart from rare mutations, but only "
            + inherited
            + " of "
            + alleles
            + " came from a parent");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void twoCarriersCanHaveAPupShowingTheHiddenCoat(TestContext context) {
    final UnleashedDogEntity parent = spawnCarrier(context, PARENT_POS);
    final UnleashedDogEntity otherParent = spawnCarrier(context, OTHER_PARENT_POS);

    boolean whitePup = false;
    for (int litter = 0; litter < LITTERS; litter++) {
      final UnleashedDogEntity pup = breed(context, parent, otherParent);
      final AllelePair pair = pup.getGenes().genes().pair(HUSKY_COAT);
      final int expressed = HUSKY_COAT.dominance().express(pair);
      context.assertTrue(
          pup.getAppearance().coatVariantOrdinal() == expressed,
          "A pup's coat should be the one its genes express, genes " + pair);
      whitePup |= expressed == HuskyCoat.WHITE.ordinal();
    }

    context.assertTrue(
        whitePup, "Two carriers of the white coat should have a white pup within " + LITTERS);
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void genesSurviveAnNbtRoundTrip(TestContext context) {
    final UnleashedDogEntity original = spawnCarrier(context, PARENT_POS);

    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity reloaded = spawnHusky(context, OTHER_PARENT_POS);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        WHITE_CARRIER.equals(reloaded.getGenes().genes().pair(HUSKY_COAT)),
        "The carried coat should survive a save, but was "
            + reloaded.getGenes().genes().pair(HUSKY_COAT));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", tickLimit = 20)
  public void aDogSavedBeforeGenesLoadsWithGenesMatchingItsLook(TestContext context) {
    final UnleashedDogEntity original = spawnHusky(context, PARENT_POS);
    original.applyAppearance(
        new DogAppearance(
            UnleashedDogBreed.HUSKY, HuskyCoat.SABLE.ordinal(), HuskyEyeColor.BLUE_BLUE.ordinal()));
    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);
    nbt.remove(ModNbtKeys.GENES);

    final UnleashedDogEntity reloaded = spawnCarrier(context, OTHER_PARENT_POS);
    reloaded.readCustomDataFromNbt(nbt);

    final DogGenes genes = reloaded.getGenes().genes();
    context.assertTrue(
        AllelePair.matching(HuskyCoat.SABLE.ordinal()).equals(genes.pair(HUSKY_COAT)),
        "An old save should carry two copies of its own coat, but was " + genes.pair(HUSKY_COAT));
    context.assertTrue(
        AllelePair.matching(HuskyEyeColor.BLUE_BLUE.ordinal()).equals(genes.pair(HUSKY_EYES)),
        "An old save should carry two copies of its own eyes, but was " + genes.pair(HUSKY_EYES));
    context.assertTrue(
        reloaded.getAppearance().coatVariantOrdinal() == HuskyCoat.SABLE.ordinal()
            && reloaded.getAppearance().eyeColorVariantOrdinal()
                == HuskyEyeColor.BLUE_BLUE.ordinal(),
        "An old save should keep its look, but was " + reloaded.getAppearance());
    context.complete();
  }

  private static UnleashedDogEntity breed(
      final TestContext context, final UnleashedDogEntity parent, final UnleashedDogEntity other) {
    return (UnleashedDogEntity) parent.createChild(context.getWorld(), other);
  }

  private static UnleashedDogEntity spawnCarrier(
      final TestContext context, final BlockPos relativePos) {
    final UnleashedDogEntity husky = spawnHusky(context, relativePos);
    husky.applyAppearance(
        new DogAppearance(
            UnleashedDogBreed.HUSKY,
            HuskyCoat.BLACK_WHITE.ordinal(),
            HuskyEyeColor.HAZEL_HAZEL.ordinal()));
    husky
        .getGenes()
        .set(
            new DogGenes(
                Map.of(
                    HUSKY_COAT,
                    WHITE_CARRIER,
                    HUSKY_EYES,
                    AllelePair.matching(HuskyEyeColor.HAZEL_HAZEL.ordinal()))));
    return husky;
  }

  private static UnleashedDogEntity spawnHusky(
      final TestContext context, final BlockPos relativePos) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relativePos);
    husky.setAiDisabled(true);
    return husky;
  }
}
