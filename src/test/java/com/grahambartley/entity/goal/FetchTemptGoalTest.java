package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.entity.ai.goal.TemptGoal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchTemptGoalTest {

  @Test
  @DisplayName("fetch tempt goal should extend tempt goal with shared suppression logic")
  void fetchTemptGoalShouldHaveSuppressionMethods() {
    assertTrue(FetchTemptGoal.class.getSuperclass() == TemptGoal.class);
    boolean hasIsSuppressed =
        Arrays.stream(FetchTemptGoal.class.getDeclaredMethods())
            .anyMatch(m -> m.getName().equals("isTemptSuppressed"));
    assertTrue(hasIsSuppressed, "FetchTemptGoal should have isTemptSuppressed method");
    boolean hasCanFollow =
        Arrays.stream(FetchTemptGoal.class.getDeclaredMethods())
            .anyMatch(m -> m.getName().equals("canFollowClosestPlayer"));
    assertTrue(hasCanFollow, "FetchTemptGoal should have canFollowClosestPlayer method");
    assertNotNull(FetchTemptGoal.class);
  }
}
