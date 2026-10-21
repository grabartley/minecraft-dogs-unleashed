package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.herding.SheepHerding;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;

public final class SheepHerdingGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "sheep-herding";
  private static final int TICK_LIMIT = 80;
  private static final int ASSERT_TICK = 60;
  private static final BlockPos SHEEP_POS = new BlockPos(2, 2, 3);
  private static final BlockPos DOG_POS = new BlockPos(4, 2, 3);

  @CustomTestProvider
  public Collection<TestFunction> sheepFleeOnlyFromHerders() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
                    "sheepherdinggametest.sheepflee." + data.breed().serializedId(),
                    ARENA,
                    TICK_LIMIT,
                    0L,
                    true,
                    context -> assertSheepFlee(context, data)))
        .toList();
  }

  private static void assertSheepFlee(final TestContext context, final DogTestData data) {
    final boolean expectFlee = SheepHerding.herds(data.breed());
    final UnleashedDogEntity dog = context.spawnEntity(data.entityType(), DOG_POS);
    dog.setAiDisabled(true);
    final SheepEntity sheep = context.spawnEntity(EntityType.SHEEP, SHEEP_POS);
    final AtomicBoolean fled = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (sheep.goalSelector.getGoals().stream()
              .anyMatch(goal -> goal.isRunning() && goal.getGoal() instanceof FleeEntityGoal<?>)) {
            fled.set(true);
          }
        });
    context.runAtTick(
        ASSERT_TICK,
        () -> {
          context.assertTrue(
              fled.get() == expectFlee,
              "A sheep "
                  + (expectFlee ? "should" : "should not")
                  + " run from a "
                  + data.breed().serializedId());
          context.complete();
        });
  }
}
