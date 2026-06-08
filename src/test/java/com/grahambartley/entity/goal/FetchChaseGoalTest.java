package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.entity.ai.goal.Goal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchChaseGoalTest {

  @Test
  @DisplayName("fetch chase goal should extend goal and expose active fetch type sync")
  void fetchChaseGoalShouldSyncActiveFetchType() {
    assertTrue(FetchChaseGoal.class.getSuperclass() == Goal.class);
    boolean hasSyncMethod =
        Arrays.stream(FetchChaseGoal.class.getDeclaredMethods())
            .anyMatch(m -> m.getName().equals("syncActiveFetchTypeFromTarget"));
    assertTrue(hasSyncMethod, "FetchChaseGoal should have syncActiveFetchTypeFromTarget method");
    assertNotNull(FetchChaseGoal.class);
  }
}
