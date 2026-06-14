package com.grahambartley.entity;

import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity.RemovalReason;
import org.jetbrains.annotations.Nullable;

final class ActivePlaySessions {

  private ActivePlaySessions() {}

  static void clear(
      Map<UUID, UUID> activePlaySessions,
      boolean inPlayMode,
      @Nullable UUID playerUuid,
      @Nullable UUID dogUuid) {
    if (!inPlayMode || playerUuid == null || dogUuid == null) {
      return;
    }
    activePlaySessions.remove(playerUuid, dogUuid);
  }

  static boolean shouldEndOnRemoval(RemovalReason reason) {
    return reason != RemovalReason.KILLED;
  }

  static @Nullable UUID takeover(
      Map<UUID, UUID> activePlaySessions, UUID playerUuid, UUID newDogUuid) {
    UUID previous = activePlaySessions.put(playerUuid, newDogUuid);
    if (previous == null || previous.equals(newDogUuid)) {
      return null;
    }
    return previous;
  }
}
