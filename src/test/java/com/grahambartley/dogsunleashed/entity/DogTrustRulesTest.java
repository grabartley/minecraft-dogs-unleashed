package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogTrustRulesTest {

  @Test
  @DisplayName("raw meat earns twice the trust of bones or cooked meat")
  void foodGains() {
    assertEquals(20, DogTrustRules.Food.RAW_MEAT.gain());
    assertEquals(10, DogTrustRules.Food.BONE.gain());
    assertEquals(10, DogTrustRules.Food.COOKED_MEAT.gain());
    assertEquals(0, DogTrustRules.Food.REFUSED.gain());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "mutton, RAW_MEAT",
    "beef, RAW_MEAT",
    "rabbit, RAW_MEAT",
    "chicken, RAW_MEAT",
    "porkchop, RAW_MEAT",
    "bone, BONE",
    "cooked_mutton, COOKED_MEAT",
    "cooked_beef, COOKED_MEAT",
    "cooked_rabbit, COOKED_MEAT",
    "cooked_chicken, COOKED_MEAT",
    "cooked_porkchop, COOKED_MEAT",
    "rotten_flesh, REFUSED",
    "cod, REFUSED",
    "bread, REFUSED",
  })
  @DisplayName("a pup takes raw meat, bones and cooked meat and refuses everything else")
  void foodOfItem(final String item, final DogTrustRules.Food expected) {
    assertEquals(expected, DogTrustRules.foodOf(Identifier.ofVanilla(item)));
  }

  @Test
  @DisplayName("a modded item with a vanilla meat's path is refused")
  void foodNeedsTheVanillaNamespace() {
    assertEquals(DogTrustRules.Food.REFUSED, DogTrustRules.foodOf(Identifier.of("other", "beef")));
  }

  @ParameterizedTest(name = "{0}, stray {1} -> {2}")
  @CsvSource({
    "RAW_MEAT, false, 20",
    "RAW_MEAT, true, 40",
    "BONE, false, 10",
    "BONE, true, 20",
    "COOKED_MEAT, true, 20",
    "REFUSED, true, 0",
  })
  @DisplayName("a stray earns trust twice as fast from the same food")
  void feedGain(final DogTrustRules.Food food, final boolean stray, final int expected) {
    assertEquals(expected, DogTrustRules.feedGain(food, stray));
  }

  @ParameterizedTest(name = "trust breed {0}, stray {1}, tamed {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "false, true, false, true",
    "true, true, false, true",
    "false, false, false, false",
    "true, false, true, false",
    "false, true, true, false",
  })
  @DisplayName("trust taming covers wild dire wolves and strays until they are tamed")
  void applies(
      final boolean tamesThroughTrust,
      final boolean stray,
      final boolean tamed,
      final boolean expected) {
    assertEquals(expected, DogTrustRules.applies(tamesThroughTrust, stray, tamed));
  }

  @ParameterizedTest(name = "applies {0}, baby {1}, stray {2} -> {3}")
  @CsvSource({
    "true, true, false, true",
    "true, false, false, false",
    "true, false, true, true",
    "true, true, true, true",
    "false, true, false, false",
    "false, false, true, false",
  })
  @DisplayName("a pup or a stray can be won over, a wild adult cannot")
  void canBeWon(
      final boolean applies, final boolean baby, final boolean stray, final boolean expected) {
    assertEquals(expected, DogTrustRules.canBeWon(applies, baby, stray));
  }

  @ParameterizedTest(name = "stray {0} -> wary {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("only a wild pup is wary, so a stray is never spooked and never forgets")
  void isWary(final boolean stray, final boolean wary) {
    assertEquals(wary, DogTrustRules.isWary(stray));
  }

  @Test
  @DisplayName("hostile kills earn more than small prey, other kills earn nothing")
  void killGains() {
    assertEquals(15, DogTrustRules.Kill.SMALL_PREY.gain());
    assertEquals(20, DogTrustRules.Kill.HOSTILE.gain());
    assertEquals(0, DogTrustRules.Kill.OTHER.gain());
  }

  @ParameterizedTest
  @CsvSource({"-20,0", "0,0", "55,55", "100,100", "140,100"})
  @DisplayName("trust stays between 0 and 100")
  void clamp(final int raw, final int clamped) {
    assertEquals(clamped, DogTrustRules.clamp(raw));
  }

  @ParameterizedTest(name = "{0} -> {1} crosses {2}")
  @CsvSource({"0,20,0", "20,40,25", "45,55,50", "70,80,75", "75,95,0", "24,25,25", "40,90,75"})
  @DisplayName("hearts show when trust passes 25, 50 or 75")
  void milestones(final int before, final int after, final int milestone) {
    assertEquals(milestone, DogTrustRules.milestoneCrossed(before, after));
  }

  @Test
  @DisplayName("feeds wait two seconds and kills five")
  void cooldowns() {
    assertEquals(40, DogTrustRules.FEED_COOLDOWN_TICKS);
    assertEquals(100, DogTrustRules.KILL_COOLDOWN_TICKS);
    assertFalse(DogTrustRules.cooledDown(139, 100, 40));
    assertTrue(DogTrustRules.cooledDown(140, 100, 40));
  }

  @Test
  @DisplayName("a pup is won at 100 trust and follows from 25")
  void thresholds() {
    assertFalse(DogTrustRules.isWon(99));
    assertTrue(DogTrustRules.isWon(100));
    assertFalse(DogTrustRules.follows(24));
    assertTrue(DogTrustRules.follows(25));
  }

  @ParameterizedTest(name = "sneak={0} sprint={1} weapon={2} distance={3} -> {4}")
  @CsvSource({
    "false,true,false,8,true",
    "false,false,true,8,true",
    "false,false,false,2.5,true",
    "false,false,false,8,false",
    "true,true,true,1,false",
    "true,false,false,1,false"
  })
  @DisplayName("sprinting, a weapon or crowding a pup provokes the pack, sneaking never does")
  void provokes(
      final boolean sneaking,
      final boolean sprinting,
      final boolean weapon,
      final double distance,
      final boolean expected) {
    assertEquals(expected, DogTrustRules.provokes(sneaking, sprinting, weapon, distance));
  }
}
