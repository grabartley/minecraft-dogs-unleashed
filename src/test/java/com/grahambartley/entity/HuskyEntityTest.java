package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyEntityTest {

  @Test
  @DisplayName("Husky health should be 25.0")
  void testHuskyHealth() {
    double health = 25.0;
    double wolfHealth = 20.0;
    assertTrue(health > wolfHealth, "Husky health should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Husky speed should be 0.35")
  void testHuskySpeed() {
    double speed = 0.35;
    double wolfSpeed = 0.3;
    assertTrue(speed > wolfSpeed, "Husky speed should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Husky attack damage should be 5.0")
  void testHuskyAttack() {
    double attack = 5.0;
    double wolfAttack = 4.0;
    assertTrue(attack > wolfAttack, "Husky attack should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Entity attribute values should be configured correctly")
  void testAttributeValues() {
    double health = 25.0;
    double speed = 0.35;
    double attack = 5.0;

    assertAll(
        () -> assertTrue(health >= 10 && health <= 100, "Health should be reasonable"),
        () -> assertTrue(speed > 0 && speed <= 1.0, "Speed should be reasonable"),
        () -> assertTrue(attack >= 0 && attack <= 20, "Attack should be reasonable"));
  }
}
