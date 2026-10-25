package com.grahambartley.dogsunleashed.whistle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WhistleBlowPosesTest {

  private static final long START = 1000L;

  @ParameterizedTest(name = "{0} ticks in, lift {1}")
  @CsvSource({"-1, 0", "0, 0", "1.5, 0.5", "3, 1", "7, 1", "11, 1", "12.5, 0.5", "14, 0", "40, 0"})
  @DisplayName("the whistle rises to the mouth, holds, then drops back")
  void liftFollowsTheBlow(final float elapsedTicks, final float expected) {
    assertEquals(expected, WhistleBlowPoses.liftAt(elapsedTicks), 1.0e-5f);
  }

  @ParameterizedTest(name = "{0} ticks after the blow, blowing {1}")
  @CsvSource({"-1, false", "0, true", "1, true", "13, true", "14, false", "100, false"})
  @DisplayName("the arm pose lasts exactly the blow")
  void blowingLastsTheBlow(final long elapsedTicks, final boolean expected) {
    final WhistleBlowPoses poses = new WhistleBlowPoses();
    poses.start(5, Hand.MAIN_HAND, START);
    assertEquals(expected, poses.isBlowing(5, Hand.MAIN_HAND, START + elapsedTicks));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Hand.class)
  @DisplayName("only the hand that blew is raised")
  void onlyTheBlowingHandIsRaised(final Hand hand) {
    final WhistleBlowPoses poses = new WhistleBlowPoses();
    poses.start(5, hand, START);
    final Hand other = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    assertEquals(true, poses.isBlowing(5, hand, START + 5));
    assertEquals(false, poses.isBlowing(5, other, START + 5));
    assertEquals(1.0f, poses.lift(5, hand, START + 5, 0.0f), 1.0e-5f);
    assertEquals(0.0f, poses.lift(5, other, START + 5, 0.0f), 1.0e-5f);
  }

  @ParameterizedTest(name = "player {0}")
  @CsvSource({"1", "42", "9000"})
  @DisplayName("each player blows on their own, and a reset stops them all")
  void playersAreTrackedApart(final int otherId) {
    final WhistleBlowPoses poses = new WhistleBlowPoses();
    poses.start(0, Hand.MAIN_HAND, START);
    poses.start(otherId, Hand.OFF_HAND, START + 2);
    assertEquals(true, poses.isBlowing(0, Hand.MAIN_HAND, START + 5));
    assertEquals(true, poses.isBlowing(otherId, Hand.OFF_HAND, START + 5));
    assertEquals(false, poses.isBlowing(otherId, Hand.MAIN_HAND, START + 5));
    poses.clear();
    assertEquals(false, poses.isBlowing(0, Hand.MAIN_HAND, START + 5));
    assertEquals(false, poses.isBlowing(otherId, Hand.OFF_HAND, START + 5));
  }

  @ParameterizedTest(name = "blown again {0} ticks later")
  @CsvSource({"5", "20", "200"})
  @DisplayName("blowing again restarts the animation")
  void blowingAgainRestarts(final long later) {
    final WhistleBlowPoses poses = new WhistleBlowPoses();
    poses.start(3, Hand.MAIN_HAND, START);
    poses.start(3, Hand.MAIN_HAND, START + later);
    assertEquals(true, poses.isBlowing(3, Hand.MAIN_HAND, START + later + 13));
    assertEquals(false, poses.isBlowing(3, Hand.MAIN_HAND, START + later + 14));
  }
}
