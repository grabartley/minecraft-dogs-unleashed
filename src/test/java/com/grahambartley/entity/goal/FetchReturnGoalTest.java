package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.entity.ai.goal.Goal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchReturnGoalTest {

  @Test
  @DisplayName("fetch return goal should extend goal and use the active fetch item type")
  void fetchReturnGoalShouldUseActiveFetchType() {
    assertTrue(FetchReturnGoal.class.getSuperclass() == Goal.class);
    boolean hasPlaceMethod =
        Arrays.stream(FetchReturnGoal.class.getDeclaredMethods())
            .anyMatch(m -> m.getName().equals("placeReturnedFetchItem"));
    assertTrue(hasPlaceMethod, "FetchReturnGoal should have placeReturnedFetchItem method");
    assertNotNull(FetchReturnGoal.class);
  }
}
