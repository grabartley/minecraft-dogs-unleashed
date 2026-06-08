package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchTemptGoalTest {
  private static final Path FETCH_TEMPT_GOAL_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/goal/FetchTemptGoal.java");

  @Test
  @DisplayName(
      "fetch tempt goal should gate start and continuation through shared suppression logic")
  void fetchTemptGoalShouldUseSharedSuppressionLogic() throws IOException {
    String source = Files.readString(FETCH_TEMPT_GOAL_SOURCE);

    assertTrue(source.contains("if (this.isTemptSuppressed()) {"));
    assertTrue(source.contains("private boolean isTemptSuppressed()"));
    assertTrue(source.contains("return UnleashedDogEntity.isAnyDogInPlayMode();"));
  }

  @Test
  @DisplayName("fetch tempt goal should stop immediately when fetch play suppresses temptation")
  void fetchTemptGoalShouldStopWhenSuppressed() throws IOException {
    String source = Files.readString(FETCH_TEMPT_GOAL_SOURCE);

    assertTrue(source.contains("public void tick()"));
    assertTrue(source.contains("this.stop();"));
    assertTrue(source.contains("return this.dog.isActivelyFetching();"));
  }

  @Test
  @DisplayName(
      "fetch tempt goal should still allow the active play partner to tempt the fetch dog before a throw")
  void fetchTemptGoalShouldAllowActivePlayPartnerBeforeThrow() throws IOException {
    String source = Files.readString(FETCH_TEMPT_GOAL_SOURCE);

    assertTrue(source.contains("private boolean canFollowClosestPlayer()"));
    assertTrue(
        source.contains(
            "this.closestPlayer.getUuid().equals(this.dog.getPlayPartnerPlayerUuid())"));
    assertTrue(
        source.contains(
            "return !UnleashedDogEntity.isAnyDogInPlayModeFor(this.closestPlayer.getUuid());"));
  }
}
