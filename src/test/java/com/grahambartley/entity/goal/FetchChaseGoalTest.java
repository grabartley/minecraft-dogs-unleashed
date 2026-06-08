package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchChaseGoalTest {
  private static final Path FETCH_CHASE_GOAL_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/goal/FetchChaseGoal.java");

  @Test
  @DisplayName("fetch chase goal should sync the active fetch type from the chased projectile")
  void fetchChaseGoalShouldSyncActiveFetchTypeFromProjectile() throws IOException {
    String source = Files.readString(FETCH_CHASE_GOAL_SOURCE);

    assertTrue(source.contains("this.syncActiveFetchTypeFromTarget();"));
    assertTrue(
        source.contains("this.dog.setActiveFetchType(fetchProjectileEntity.getFetchItemType());"));
    assertTrue(source.contains("FetchTypes.forEntityType(this.targetFetchProjectile.getType())"));
  }
}
