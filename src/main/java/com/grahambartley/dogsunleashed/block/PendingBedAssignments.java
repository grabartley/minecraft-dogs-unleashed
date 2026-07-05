package com.grahambartley.dogsunleashed.block;

import java.util.Map;
import java.util.UUID;

final class PendingBedAssignments {

  private PendingBedAssignments() {}

  static void clearAll(Map<UUID, UUID> pendingBedAssignments) {
    pendingBedAssignments.clear();
  }
}
