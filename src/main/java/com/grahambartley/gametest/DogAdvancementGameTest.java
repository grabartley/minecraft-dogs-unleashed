package com.grahambartley.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Gametest entrypoint reserved for advancement coverage.
 *
 * <p>The full suite that exercises the advancement criteria (husky howled, fetch returned, dog
 * slept in bed, the_whole_pack progression, etc.) lands together with the advancement set in #73.
 * Until that production wiring ships on main, this class registers the entrypoint with a single
 * smoke test so {@code ./gradlew runGametest} loads the class and the {@code Gametests} check from
 * #201 stays meaningful.
 *
 * <p>See issue #73 for the advancement rollout that backfills the real test methods here.
 */
public final class DogAdvancementGameTest implements FabricGameTest {
  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void advancementGametestEntrypointLoads(final TestContext context) {
    // Placeholder until #73 ships the advancement criteria this suite is meant to exercise.
    // Asserting the test context exists confirms the class was discovered as a gametest
    // entrypoint after the fabric.mod.json registration in #207.
    context.assertTrue(context.getWorld() != null, "Gametest context world should be available");
    context.complete();
  }
}
