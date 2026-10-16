package com.grahambartley.dogsunleashed.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NextTickQueueTest {

  private final NextTickQueue queue = new NextTickQueue();
  private final List<String> ran = new ArrayList<>();

  @Test
  @DisplayName("runPending runs every queued task in the order it was queued")
  void runPendingRunsTasksInOrder() {
    queue.enqueue(() -> ran.add("first"));
    queue.enqueue(() -> ran.add("second"));

    queue.runPending();

    assertEquals(List.of("first", "second"), ran);
  }

  @Test
  @DisplayName("runPending runs each task only once")
  void runPendingRunsEachTaskOnce() {
    queue.enqueue(() -> ran.add("task"));

    queue.runPending();
    queue.runPending();

    assertEquals(List.of("task"), ran);
  }

  @Test
  @DisplayName("a task queued while the queue drains waits for the next drain")
  void taskQueuedDuringDrainRunsOnNextDrain() {
    queue.enqueue(() -> queue.enqueue(() -> ran.add("follow-up")));

    queue.runPending();
    assertTrue(ran.isEmpty(), "follow-up must not run in the same drain");

    queue.runPending();
    assertEquals(List.of("follow-up"), ran);
  }

  @Test
  @DisplayName("clear drops queued tasks so they never run against a later server")
  void clearDropsQueuedTasks() {
    queue.enqueue(() -> ran.add("stale"));

    queue.clear();
    queue.runPending();

    assertTrue(ran.isEmpty());
  }
}
