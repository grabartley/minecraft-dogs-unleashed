package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ProtectiveAngerGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int ROLLS = 50;
  private static final int ORDINARY_MAX_ANGER_TICKS = 39 * 20;
  private static final int PROTECTIVE_MIN_ANGER_TICKS = (20 + 10) * 20;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aGermanShepherdStaysAngryLongerThanABeagle(final TestContext context) {
    final UnleashedDogEntity shepherd = spawn(context, ModEntities.GERMAN_SHEPHERD, 2);
    final UnleashedDogEntity beagle = spawn(context, ModEntities.BEAGLE, 4);
    int shortestShepherd = Integer.MAX_VALUE;
    int longestBeagle = 0;
    for (int roll = 0; roll < ROLLS; roll++) {
      shepherd.chooseRandomAngerTime();
      beagle.chooseRandomAngerTime();
      shortestShepherd = Math.min(shortestShepherd, shepherd.getAngerTime());
      longestBeagle = Math.max(longestBeagle, beagle.getAngerTime());
    }

    context.assertTrue(
        shortestShepherd >= PROTECTIVE_MIN_ANGER_TICKS,
        "A German Shepherd rolled only " + shortestShepherd + " ticks of anger");
    context.assertTrue(
        longestBeagle <= ORDINARY_MAX_ANGER_TICKS,
        "A Beagle rolled " + longestBeagle + " ticks of anger");
    context.complete();
  }

  private static UnleashedDogEntity spawn(
      final TestContext context, final EntityType<UnleashedDogEntity> type, final int x) {
    final UnleashedDogEntity dog = context.spawnEntity(type, new BlockPos(x, 2, 3));
    dog.setAiDisabled(true);
    return dog;
  }
}
