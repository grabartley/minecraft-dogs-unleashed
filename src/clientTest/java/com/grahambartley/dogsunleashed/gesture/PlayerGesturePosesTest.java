package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PlayerGesturePosesTest {

  private static final long START = 1000L;

  @ParameterizedTest(name = "{0}, {1} ticks after it starts -> playing {2}")
  @CsvSource({
    "WHISTLE, -1, false",
    "WHISTLE, 0, true",
    "WHISTLE, 13, true",
    "WHISTLE, 14, false",
    "PAT, 0, true",
    "PAT, 29, true",
    "PAT, 30, false",
    "PAT, 100, false"
  })
  @DisplayName("a pose lasts exactly its gesture")
  void aPoseLastsItsGesture(
      final PlayerGesture gesture, final long elapsedTicks, final boolean expected) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(5, Hand.MAIN_HAND, gesture, START);
    assertEquals(expected, poses.isPlaying(5, Hand.MAIN_HAND, gesture, START + elapsedTicks));
  }

  @ParameterizedTest(name = "{0} ticks plus {1} partial")
  @CsvSource({"0, 0, 0", "4, 0.5, 4.5", "12, 0.25, 12.25"})
  @DisplayName("the elapsed time includes the partial tick")
  void elapsedIncludesThePartialTick(final long ticks, final float delta, final float expected) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(5, Hand.MAIN_HAND, PlayerGesture.PAT, START);
    assertEquals(
        expected,
        poses.elapsedTicks(5, Hand.MAIN_HAND, PlayerGesture.PAT, START + ticks, delta),
        1.0e-5f);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Hand.class)
  @DisplayName("only the hand that gestured moves")
  void onlyTheGesturingHandMoves(final Hand hand) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(5, hand, PlayerGesture.WHISTLE, START);
    final Hand other = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    assertEquals(true, poses.isPlaying(5, hand, PlayerGesture.WHISTLE, START + 5));
    assertEquals(false, poses.isPlaying(5, other, PlayerGesture.WHISTLE, START + 5));
    assertEquals(
        PlayerGesturePoses.NOT_PLAYING,
        poses.elapsedTicks(5, other, PlayerGesture.WHISTLE, START + 5, 0.0f),
        1.0e-5f);
  }

  @ParameterizedTest(name = "{0} asked about while {1} plays")
  @CsvSource({"WHISTLE, PAT", "PAT, WHISTLE"})
  @DisplayName("one gesture never shows as another")
  void oneGestureNeverShowsAsAnother(final PlayerGesture asked, final PlayerGesture started) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(5, Hand.MAIN_HAND, started, START);
    assertEquals(false, poses.isPlaying(5, Hand.MAIN_HAND, asked, START + 2));
  }

  @ParameterizedTest(name = "player {0}")
  @CsvSource({"1", "42", "9000"})
  @DisplayName("each player gestures on their own, and a reset stops them all")
  void playersAreTrackedApart(final int otherId) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(0, Hand.MAIN_HAND, PlayerGesture.PAT, START);
    poses.start(otherId, Hand.OFF_HAND, PlayerGesture.WHISTLE, START + 2);
    assertEquals(true, poses.isPlaying(0, Hand.MAIN_HAND, PlayerGesture.PAT, START + 5));
    assertEquals(true, poses.isPlaying(otherId, Hand.OFF_HAND, PlayerGesture.WHISTLE, START + 5));
    poses.clear();
    assertEquals(false, poses.isPlaying(0, Hand.MAIN_HAND, PlayerGesture.PAT, START + 5));
    assertEquals(false, poses.isPlaying(otherId, Hand.OFF_HAND, PlayerGesture.WHISTLE, START + 5));
  }

  @ParameterizedTest(name = "started again {0} ticks later")
  @CsvSource({"5", "20", "200"})
  @DisplayName("starting again restarts the gesture")
  void startingAgainRestarts(final long later) {
    final PlayerGesturePoses poses = new PlayerGesturePoses();
    poses.start(3, Hand.MAIN_HAND, PlayerGesture.PAT, START);
    poses.start(3, Hand.MAIN_HAND, PlayerGesture.PAT, START + later);
    assertEquals(true, poses.isPlaying(3, Hand.MAIN_HAND, PlayerGesture.PAT, START + later + 29));
    assertEquals(false, poses.isPlaying(3, Hand.MAIN_HAND, PlayerGesture.PAT, START + later + 30));
  }
}
