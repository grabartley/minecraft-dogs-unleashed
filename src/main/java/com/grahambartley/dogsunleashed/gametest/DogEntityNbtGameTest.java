package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogCollar;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.DogSpawnerOrigin;
import com.grahambartley.dogsunleashed.entity.DogTreatBuff;
import com.grahambartley.dogsunleashed.entity.DogTreatBuffState;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class DogEntityNbtGameTest implements FabricGameTest {

  private static final String HUSKY_SAVED_ON_MAIN =
      "{CollarColor:5,SpawnedByDogSpawner:1b,TreatBuffTicks:300,CommandMode:2,"
          + "SleepingInBed:0b,BedPosX:7,BedPosY:64,BedPosZ:-3,FurStage:3,CoatVariant:1}";

  private static final String DIRE_WOLF_SAVED_ON_MAIN = "{GrowthStage:4,CollarColor:10}";

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void aHuskySavedOnMainLoadsEveryValue(final TestContext context) throws Exception {
    final NbtCompound saved = StringNbtReader.parse(HUSKY_SAVED_ON_MAIN);
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, BlockPos.ORIGIN.up());
    dog.setAiDisabled(true);
    dog.readCustomDataFromNbt(saved);

    context.assertTrue(
        dog.getComponents().get(DogCollar.class).getColor() == DyeColor.LIME, "collar colour");
    context.assertTrue(
        dog.getComponents().get(DogSpawnerOrigin.class).isFromSpawner(), "spawner flag");
    context.assertTrue(
        dog.getComponents().get(DogTreatBuffState.class).getRemainingTicks() == 300,
        "treat buff ticks");
    context.assertTrue(DogTreatBuff.isApplied(dog), "treat buff modifiers re-applied on load");
    context.assertTrue(dog.getCommand() == DogCommand.STAY, "command");
    context.assertTrue(
        dog.getSleepController().getAssignedBedPos().orElseThrow().equals(new BlockPos(7, 64, -3)),
        "bed position");
    context.assertTrue(!dog.getSleepController().isSleepingInBed(), "sleeping flag");
    context.assertTrue(dog.getFur().stage() == DogFurStage.fromOrdinal(3), "fur stage");
    context.assertTrue(dog.getAppearance().coatVariantOrdinal() == 1, "coat variant");
    assertWritesBack(context, dog, saved);
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void aDireWolfSavedOnMainLoadsItsGrowthStage(final TestContext context) throws Exception {
    final NbtCompound saved = StringNbtReader.parse(DIRE_WOLF_SAVED_ON_MAIN);
    final UnleashedDogEntity wolf =
        context.spawnEntity(ModEntities.DIRE_WOLF, BlockPos.ORIGIN.up());
    wolf.setAiDisabled(true);
    wolf.readCustomDataFromNbt(saved);

    context.assertTrue(
        wolf.getGrowth().stage() == DireWolfGrowthStage.fromNumber(4), "growth stage");
    context.assertTrue(
        wolf.getComponents().get(DogCollar.class).getColor() == DyeColor.PURPLE, "collar colour");
    assertWritesBack(context, wolf, saved);
    context.complete();
  }

  private static void assertWritesBack(
      final TestContext context, final UnleashedDogEntity dog, final NbtCompound saved) {
    final NbtCompound written = new NbtCompound();
    dog.writeCustomDataToNbt(written);
    for (final String key : Set.copyOf(saved.getKeys())) {
      context.assertTrue(
          saved.get(key).equals(written.get(key)),
          key + " was saved as " + saved.get(key) + " but writes back as " + written.get(key));
    }
  }
}
