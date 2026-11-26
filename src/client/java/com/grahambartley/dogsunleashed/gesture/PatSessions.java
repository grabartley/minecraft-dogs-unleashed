package com.grahambartley.dogsunleashed.gesture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public final class PatSessions {

  public static final PatSessions SHARED = new PatSessions();
  static final long FORGET_AFTER_TICKS = 200L;

  public record Active(
      int targetEntityId, PatTimeline.Moment moment, Optional<PatTarget.Aim> lastAim) {}

  private record Entry(
      int targetEntityId, PatTimeline.Session session, @Nullable PatTarget.Aim lastAim) {

    Entry withSession(final PatTimeline.Session next) {
      return new Entry(this.targetEntityId, next, this.lastAim);
    }
  }

  private final Map<Integer, Entry> byPlayer = new HashMap<>();

  public void show(
      final int playerId,
      final PlayerGesture gesture,
      final int targetEntityId,
      final long worldTime) {
    this.forgetOld(worldTime);
    final Entry current = this.byPlayer.get(playerId);
    final boolean same = current != null && current.targetEntityId() == targetEntityId;
    switch (gesture) {
      case INVITE ->
          this.byPlayer.put(
              playerId, new Entry(targetEntityId, PatTimeline.invited(worldTime), null));
      case PAT ->
          this.byPlayer.put(
              playerId,
              same
                  ? current.withSession(current.session().patted(worldTime))
                  : new Entry(
                      targetEntityId,
                      PatTimeline.invited(
                              worldTime
                                  - (long) (PatTimeline.KNEEL_END * PatTimeline.TICKS_PER_SECOND))
                          .patted(worldTime),
                      null));
      case RELEASE -> {
        if (same) {
          this.byPlayer.put(playerId, current.withSession(current.session().released(worldTime)));
        }
      }
      default -> {}
    }
  }

  public void remember(final int playerId, final PatTarget.Aim aim) {
    final Entry current = this.byPlayer.get(playerId);
    if (current != null) {
      this.byPlayer.put(playerId, new Entry(current.targetEntityId(), current.session(), aim));
    }
  }

  public void lose(final int playerId, final long worldTime) {
    final Entry current = this.byPlayer.get(playerId);
    if (current != null) {
      this.byPlayer.put(playerId, current.withSession(current.session().released(worldTime)));
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
    return Optional.of(
        new Active(entry.targetEntityId(), moment.get(), Optional.ofNullable(entry.lastAim())));
  }

  public void clear() {
    this.byPlayer.clear();
  }

  private void forgetOld(final long worldTime) {
    this.byPlayer
        .values()
        .removeIf(entry -> worldTime - entry.session().invitedAt() > FORGET_AFTER_TICKS);
  }
}
