package com.grahambartley.dogsunleashed.render.pawprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PawPrintTrailTest {

  @ParameterizedTest(name = "walking {0} blocks in 0.1 steps leaves {1} prints")
  @CsvSource({"0.0, 1", "0.4, 1", "0.5, 2", "2.0, 5"})
  @DisplayName("a walking dog leaves one print per stride")
  void onePrintPerStride(final double distance, final int prints) {
    final PawPrintTrail trail = new PawPrintTrail();
    for (int i = 0; i <= Math.round(distance * 10); i++) {
      trail.step(1, i / 10.0, 64, 0, 0, 0.6f);
    }
    assertEquals(prints, trail.prints().size());
  }

  @Test
  @DisplayName("prints alternate left and right")
  void alternatesSides() {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 1.0f);
    trail.step(1, 1, 64, 0, 0, 1.0f);
    trail.step(1, 2, 64, 0, 0, 1.0f);
    final List<PawPrintTrail.Print> prints = trail.prints();
    assertTrue(prints.get(0).sideOffset() < 0);
    assertTrue(prints.get(1).sideOffset() > 0);
    assertTrue(prints.get(2).sideOffset() < 0);
  }

  @Test
  @DisplayName("each dog keeps its own stride")
  void dogsTrackSeparately() {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 0.6f);
    trail.step(2, 0.1, 64, 0, 0, 0.6f);
    assertEquals(2, trail.prints().size());
  }

  @ParameterizedTest(name = "after {0} ticks {1} prints are left")
  @CsvSource({"0, 1", "599, 1", "600, 0"})
  @DisplayName("prints disappear once they have faded")
  void expiresPrints(final int ticks, final int left) {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 0.6f);
    for (int i = 0; i < ticks; i++) {
      trail.advance();
    }
    assertEquals(left, trail.prints().size());
  }

  @Test
  @DisplayName("the oldest prints are dropped past the cap")
  void capsPrints() {
    final PawPrintTrail trail = new PawPrintTrail();
    for (int i = 0; i < PawPrintRules.MAX_PRINTS + 10; i++) {
      trail.step(1, i, 64, 0, 0, 0.6f);
    }
    assertEquals(PawPrintRules.MAX_PRINTS, trail.prints().size());
    assertEquals(10.0, trail.prints().get(0).x());
  }

  @Test
  @DisplayName("a forgotten dog stamps straight away when it next steps")
  void forgetRestartsStride() {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 0.6f);
    trail.forget(1);
    trail.step(1, 0.1, 64, 0, 0, 0.6f);
    assertEquals(2, trail.prints().size());
  }

  @Test
  @DisplayName("a dog that is gone loses its stride but its prints stay to fade")
  void keepOnlyDropsGoneDogs() {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 0.6f);
    trail.step(2, 5, 64, 0, 0, 0.6f);
    trail.keepOnly(Set.of(2));
    trail.step(1, 0.1, 64, 0, 0, 0.6f);
    trail.step(2, 5.1, 64, 0, 0, 0.6f);
    assertEquals(3, trail.prints().size());
  }

  @Test
  @DisplayName("clearing removes every print")
  void clears() {
    final PawPrintTrail trail = new PawPrintTrail();
    trail.step(1, 0, 64, 0, 0, 0.6f);
    trail.clear();
    assertEquals(List.of(), trail.prints());
  }
}
