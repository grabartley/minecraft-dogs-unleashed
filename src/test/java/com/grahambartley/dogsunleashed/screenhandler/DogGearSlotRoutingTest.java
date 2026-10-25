package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.screenhandler.DogGearSlotRouting.Section;
import com.grahambartley.dogsunleashed.screenhandler.DogGearSlotRouting.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogGearSlotRoutingTest {

  private static final DogGearSlotRouting ROUTING = new DogGearSlotRouting(9, 5, 27, 9);

  @Test
  @DisplayName("the bags come first so sort mods find them, then the gear, then the player")
  void sectionStarts() {
    assertEquals(0, ROUTING.bagStart());
    assertEquals(9, ROUTING.gearStart());
    assertEquals(14, ROUTING.mainStart());
    assertEquals(41, ROUTING.hotbarStart());
    assertEquals(50, ROUTING.total());
  }

  @ParameterizedTest(name = "slot {0} is {1}")
  @CsvSource({
    "0, BAGS",
    "8, BAGS",
    "9, GEAR",
    "13, GEAR",
    "14, MAIN",
    "40, MAIN",
    "41, HOTBAR",
    "49, HOTBAR"
  })
  @DisplayName("every slot index belongs to one section")
  void sectionOfIndex(final int index, final Section expected) {
    assertEquals(expected, ROUTING.sectionOf(index));
  }

  @ParameterizedTest(name = "{0}, owner {1}, bags open {2} -> {3}")
  @CsvSource({
    "GEAR, true, true, PLAYER",
    "BAGS, true, true, PLAYER",
    "BAGS, false, true, PLAYER",
    "MAIN, true, true, GEAR BAGS HOTBAR",
    "MAIN, true, false, GEAR HOTBAR",
    "HOTBAR, true, true, GEAR BAGS MAIN",
    "HOTBAR, true, false, GEAR MAIN",
    "MAIN, false, true, HOTBAR",
    "HOTBAR, false, true, MAIN"
  })
  @DisplayName("shift-clicks reach the dog only for its owner and the bags only when worn")
  void targetsFor(
      final Section from, final boolean owner, final boolean bagsOpen, final String expected) {
    final List<Target> targets =
        Arrays.stream(expected.split(" ")).map(Target::valueOf).collect(Collectors.toList());
    assertEquals(targets, DogGearSlotRouting.targetsFor(from, owner, bagsOpen));
  }
}
