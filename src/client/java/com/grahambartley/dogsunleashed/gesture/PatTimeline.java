package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.PettingRules;
import java.util.Optional;
import net.minecraft.util.math.MathHelper;

public final class PatTimeline {

  static final float TICKS_PER_SECOND = 20.0f;
  static final float KNEEL_END = PettingRules.SETTLE_TICKS / TICKS_PER_SECOND;
  static final float STROKES_END = KNEEL_END + PlayerGesture.PAT.ticks() / TICKS_PER_SECOND;
  static final float CROSSFADE = 0.2f;
  static final float AIM_OUT = 0.3f;
  static final float BODY_FADE = 0.1f;
  static final long NEVER = Long.MIN_VALUE;

  public record Session(long invitedAt, long pattedAt, long releasedAt) {

    public Session patted(final long worldTime) {
      return new Session(this.invitedAt, worldTime, this.releasedAt);
    }

    public Session released(final long worldTime) {
      return this.releasedAt == NEVER
          ? new Session(this.invitedAt, this.pattedAt, worldTime)
          : this;
    }
  }

  public record Moment(float clipTime, float fromClipTime, float fade, float aim, float body) {}

  private PatTimeline() {}

  public static Session invited(final long worldTime) {
    return new Session(worldTime, NEVER, NEVER);
  }

  public static Optional<Moment> at(
      final Session session, final float nowTicks, final float clipLength) {
    final long releasedAt = releaseOf(session);
    if (releasedAt != NEVER && nowTicks >= releasedAt) {
      final float from = unreleasedTime(session, releasedAt);
      final float since = (nowTicks - releasedAt) / TICKS_PER_SECOND;
      final float rise = Math.max(STROKES_END, from) + since;
      if (rise >= clipLength) {
        return Optional.empty();
      }
      final float fade = smooth(since / CROSSFADE);
      final float aim = aimAt(from) * (1.0f - smooth(since / AIM_OUT));
      return Optional.of(new Moment(rise, from, fade, aim, bodyAt(rise, clipLength)));
    }
    final float time = unreleasedTime(session, nowTicks);
    if (time >= clipLength) {
      return Optional.empty();
    }
    return Optional.of(new Moment(time, time, 1.0f, aimAt(time), bodyAt(time, clipLength)));
  }

  static float aimAt(final float time) {
    if (time < KNEEL_END) {
      return smooth(time / KNEEL_END);
    }
    if (time <= STROKES_END) {
      return 1.0f;
    }
    return 1.0f - smooth((time - STROKES_END) / AIM_OUT);
  }

  static float bodyAt(final float time, final float clipLength) {
    return Math.min(smooth(time / BODY_FADE), smooth((clipLength - time) / BODY_FADE));
  }

  static float smooth(final float x) {
    final float t = MathHelper.clamp(x, 0.0f, 1.0f);
    return t * t * (3.0f - 2.0f * t);
  }

  private static float unreleasedTime(final Session session, final float nowTicks) {
    if (session.pattedAt() != NEVER && nowTicks >= session.pattedAt()) {
      return KNEEL_END + (nowTicks - session.pattedAt()) / TICKS_PER_SECOND;
    }
    return Math.min(KNEEL_END, Math.max(0.0f, (nowTicks - session.invitedAt()) / TICKS_PER_SECOND));
  }

  private static long releaseOf(final Session session) {
    if (session.releasedAt() != NEVER) {
      return session.releasedAt();
    }
    return session.pattedAt() == NEVER ? session.invitedAt() + PlayerGesture.INVITE.ticks() : NEVER;
  }
}
