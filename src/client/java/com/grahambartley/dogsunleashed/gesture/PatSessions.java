package com.grahambartley.dogsunleashed.gesture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PatSessions {

  public static final PatSessions SHARED = new PatSessions();

  public record Active(int targetEntityId, PatTimeline.Moment moment) {}

  private record Entry(int targetEntityId, PatTimeline.Session session) {}

  private final Map<Integer, Entry> byPlayer = new HashMap<>();

  public void show(
      final int playerId,
      final PlayerGesture gesture,
      final int targetEntityId,
      final long worldTime) {
    final Entry current = this.byPlayer.get(playerId);
    final boolean same = current != null && current.targetEntityId() == targetEntityId;
    switch (gesture) {
      case INVITE ->
          this.byPlayer.put(playerId, new Entry(targetEntityId, PatTimeline.invited(worldTime)));
      case PAT ->
          this.byPlayer.put(
              playerId,
              new Entry(
                  targetEntityId,
                  (same
                          ? current.session()
                          : PatTimeline.invited(
                              worldTime
                                  - (long) (PatTimeline.KNEEL_END * PatTimeline.TICKS_PER_SECOND)))
                      .patted(worldTime)));
      case RELEASE -> {
        if (same) {
          this.byPlayer.put(
              playerId, new Entry(targetEntityId, current.session().released(worldTime)));
        }
      }
      default -> {}
    }
  }

  public boolean targets(final int dogId) {
    return this.byPlayer.values().stream().anyMatch(entry -> entry.targetEntityId() == dogId);
  }

  public Optional<Active> of(
      final int playerId, final long worldTime, final float tickDelta, final float clipLength) {
    final Entry entry = this.byPlayer.get(playerId);
    if (entry == null) {
      return Optional.empty();
    }
    final Optional<PatTimeline.Moment> moment =
        PatTimeline.at(entry.session(), worldTime + tickDelta, clipLength);
    if (moment.isEmpty()) {
      this.byPlayer.remove(playerId);
      return Optional.empty();
    }
    return Optional.of(new Active(entry.targetEntityId(), moment.get()));
  }

  public void clear() {
    this.byPlayer.clear();
  }
}
