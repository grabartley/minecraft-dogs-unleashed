package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.gesture.PlayerGesture;
import net.minecraft.util.math.MathHelper;

public final class PettingRules {

  public static final int COOLDOWN_TICKS = PlayerGesture.PAT.ticks();
  public static final long NEVER = Long.MIN_VALUE;
  public static final int SETTLE_TICKS = 12;
  public static final int APPROACH_TICKS = PlayerGesture.INVITE.ticks() - SETTLE_TICKS;
  public static final double WALK_AWAY_BLOCKS = 1.0;
  public static final double CLOSE_IN_BLOCKS = 1.5;
  public static final double TURN_AWAY_DEGREES = 100.0;
  public static final float TURN_IN_DEGREES_PER_TICK = 20.0f;
  public static final float FACING_SLACK_DEGREES = 15.0f;

  public record DogState(
      boolean asleep,
      boolean fighting,
      boolean angryAtPetter,
      boolean occupied,
      boolean performingTrick,
      boolean inVehicle,
      boolean ridden,
      boolean hitched,
      boolean howling,
      boolean shaking,
      boolean growling) {

    boolean calm() {
      return !this.asleep && !this.fighting && !this.angryAtPetter && !this.growling;
    }

    boolean free() {
      return !this.occupied
          && !this.performingTrick
          && !this.inVehicle
          && !this.ridden
          && !this.hitched
          && !this.howling
          && !this.shaking;
    }
  }

  private PettingRules() {}

  public static boolean isPetGesture(
      final boolean sneaking,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean standingSteady) {
    return sneaking && mainHand && emptyHand && standingSteady;
  }

  public static boolean cooledDown(final long now, final long lastPettedAt) {
    return lastPettedAt == NEVER || now - lastPettedAt >= COOLDOWN_TICKS || now < lastPettedAt;
  }

  public static boolean isBeingPetted(final long now, final long lastPettedAt) {
    return !cooledDown(now, lastPettedAt);
  }

  public static boolean isApproaching(final long now, final long requestedAt) {
    return requestedAt != NEVER
        && now >= requestedAt
        && now - requestedAt < SETTLE_TICKS + APPROACH_TICKS;
  }

  public static boolean hasSettled(final long now, final long requestedAt) {
    return requestedAt != NEVER && now - requestedAt >= SETTLE_TICKS;
  }

  public static boolean closeEnoughToStep(final double blocksToSpot, final double dogHalfWidth) {
    return blocksToSpot <= CLOSE_IN_BLOCKS + dogHalfWidth;
  }

  public static boolean walkedAway(final double movedBlocks) {
    return movedBlocks > WALK_AWAY_BLOCKS;
  }

  public static boolean turnedAway(final float yawDegrees, final float askedYawDegrees) {
    return Math.abs(MathHelper.wrapDegrees(yawDegrees - askedYawDegrees)) > TURN_AWAY_DEGREES;
  }

  public static boolean welcomes(
      final DogState dog, final long now, final long lastPettedAt, final long requestedAt) {
    return dog.calm()
        && dog.free()
        && cooledDown(now, lastPettedAt)
        && !isApproaching(now, requestedAt);
  }

  public static float turnedInToward(final float bodyYaw, final float spotYaw) {
    return MathHelper.stepUnwrappedAngleTowards(bodyYaw, spotYaw, TURN_IN_DEGREES_PER_TICK);
  }

  public static boolean facesSpot(final float bodyYaw, final float spotYaw) {
    return Math.abs(MathHelper.wrapDegrees(spotYaw - bodyYaw)) <= FACING_SLACK_DEGREES;
  }

  public static boolean standsSteady(
      final boolean onGround, final boolean riding, final boolean flying, final boolean afloat) {
    return onGround && !riding && !flying && !afloat;
  }
}
