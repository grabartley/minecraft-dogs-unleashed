package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GreedySnackRulesTest {

  @ParameterizedTest(name = "{0}, sitting {1}, carrying {2}, owner {3} squared -> {4}")
  @CsvSource({
    "FOLLOW, false, false, 25.0, true",
    "FREE_ROAM, false, false, 256.0, true",
    "FREE_ROAM, false, false, 256.1, false",
    "FOLLOW, true, false, 25.0, false",
    "FOLLOW, false, true, 25.0, false",
    "HEEL, false, false, 25.0, false",
    "STAY, false, false, 25.0, false",
    "GUARD, false, false, 25.0, false"
  })
  @DisplayName("a greedy dog only goes for food near its owner when no command holds it")
  void mayLook(
      final DogCommand command,
      final boolean sitting,
      final boolean carrying,
      final double ownerSquaredDistance,
      final boolean expected) {
    assertEquals(
        expected, GreedySnackRules.mayLook(command, sitting, carrying, ownerSquaredDistance));
  }

  @ParameterizedTest(name = "now {0}, last {1} -> {2}")
  @CsvSource({"100, -9223372036854775808, true", "2499, 100, false", "2500, 100, true"})
  void cooledDown(final long now, final long lastSnackAt, final boolean expected) {
    assertEquals(expected, GreedySnackRules.cooledDown(now, lastSnackAt));
  }

  static Stream<Arguments> snacks() {
    return Stream.of(
        Arguments.of(new ItemStack(Items.BREAD), true),
        Arguments.of(new ItemStack(Items.COOKED_BEEF, 5), true),
        Arguments.of(new ItemStack(Items.ROTTEN_FLESH), true),
        Arguments.of(new ItemStack(Items.GOLDEN_APPLE), false),
        Arguments.of(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), false),
        Arguments.of(new ItemStack(Items.STICK), false),
        Arguments.of(new ItemStack(Items.DIAMOND), false),
        Arguments.of(ItemStack.EMPTY, false));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("snacks")
  @DisplayName("any common food counts as a snack, rare food is left alone")
  void isSnack(final ItemStack stack, final boolean expected) {
    assertEquals(expected, GreedySnackRules.isSnack(stack));
  }

  static Stream<Arguments> slots() {
    return Stream.of(
        Arguments.of(List.of(), GreedySnackRules.NO_SLOT),
        Arguments.of(List.of(new ItemStack(Items.STONE)), GreedySnackRules.NO_SLOT),
        Arguments.of(
            List.of(ItemStack.EMPTY, new ItemStack(Items.STONE), new ItemStack(Items.APPLE)), 2),
        Arguments.of(List.of(new ItemStack(Items.CARROT), new ItemStack(Items.APPLE)), 0));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("slots")
  @DisplayName("the dog takes from the first slot with food")
  void firstSnackSlot(final List<ItemStack> stacks, final int expected) {
    assertEquals(expected, GreedySnackRules.firstSnackSlot(stacks));
  }

  @ParameterizedTest(name = "tick {0}, {1} squared, reach {2} -> {3}")
  @CsvSource({
    "1, 2.25, 1.5, EAT",
    "1, 2.26, 1.5, MOVE",
    "1, 4.0, 2.0, EAT",
    "300, 50.0, 1.5, MOVE",
    "301, 1.0, 1.5, GIVE_UP"
  })
  void step(
      final int ticks,
      final double squaredDistance,
      final double reach,
      final GreedySnackRules.Step expected) {
    assertEquals(expected, GreedySnackRules.step(ticks, squaredDistance, reach));
  }
}
