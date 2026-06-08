package com.grahambartley.entity.goal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchReturnGoalTest {
  private static final Path FETCH_RETURN_GOAL_SOURCE =
      Path.of("src/main/java/com/grahambartley/entity/goal/FetchReturnGoal.java");

  @Test
  @DisplayName("fetch return goal should announce the specific returned fetch item")
  void fetchReturnGoalShouldAnnounceSpecificFetchItem() throws IOException {
    String source = Files.readString(FETCH_RETURN_GOAL_SOURCE);

    assertTrue(source.contains("message.dogs-unleashed.play_fetch_returned"));
    assertTrue(source.contains("fetchItemType.item().getName()"));
  }

  @Test
  @DisplayName("fetch return goal should place or drop the active fetch item type near the owner")
  void fetchReturnGoalShouldUseActiveFetchTypeForDropoff() throws IOException {
    String source = Files.readString(FETCH_RETURN_GOAL_SOURCE);

    assertTrue(source.contains("fetchItemType.landedBlock().getDefaultState()"));
    assertTrue(source.contains("new ItemStack(fetchItemType.item())"));
  }
}
