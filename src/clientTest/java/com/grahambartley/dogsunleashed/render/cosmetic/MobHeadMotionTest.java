package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MobHeadMotionTest {

  private static final float EPSILON = 1.0e-5f;

  private static float dragonJawPitch(final float progress) {
    return (float) (Math.sin(progress * Math.PI * 0.2f) + 1.0) * 0.2f;
  }

  @ParameterizedTest(name = "{0} ticks since the bark")
  @ValueSource(floats = {0.0f, 10.0f, 40.0f, 1.0e7f})
  @DisplayName("the dragon jaw is shut before and after a bark")
  void dragonJawRestsShut(final float ticksSinceBark) {
    assertEquals(
        0.0f,
        dragonJawPitch(MobHeadMotion.progress(MobHead.DRAGON, 3.0f, ticksSinceBark)),
        EPSILON);
  }

  @Test
  @DisplayName("the dragon jaw opens wide halfway through a bark")
  void dragonJawOpensMidBark() {
    final float midBark = MobHeadMotion.BARK_TICKS / 2.0f;
    assertEquals(
        0.4f, dragonJawPitch(MobHeadMotion.progress(MobHead.DRAGON, 0.0f, midBark)), EPSILON);
    assertTrue(dragonJawPitch(MobHeadMotion.progress(MobHead.DRAGON, 0.0f, midBark / 2.0f)) > 0.0f);
  }

  @Test
  @DisplayName("the dragon jaw opens and shuts once per bark, as a powered dragon head does")
  void dragonJawOpensOnce() {
    for (float tick = 0.0f; tick <= MobHeadMotion.BARK_TICKS; tick += 0.25f) {
      assertTrue(dragonJawPitch(MobHeadMotion.progress(MobHead.DRAGON, 0.0f, tick)) >= -EPSILON);
    }
  }

  @Test
  @DisplayName("walking does not move the dragon jaw")
  void dragonIgnoresWalking() {
    assertEquals(
        MobHeadMotion.progress(MobHead.DRAGON, 0.0f, 100.0f),
        MobHeadMotion.progress(MobHead.DRAGON, 17.3f, 100.0f),
        EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = MobHead.class,
      names = {"DRAGON"},
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("other heads follow the walk, as vanilla does for piglin ears")
  void otherHeadsFollowTheWalk(final MobHead head) {
    assertEquals(12.5f, MobHeadMotion.progress(head, 12.5f, 2.0f), EPSILON);
  }
}
