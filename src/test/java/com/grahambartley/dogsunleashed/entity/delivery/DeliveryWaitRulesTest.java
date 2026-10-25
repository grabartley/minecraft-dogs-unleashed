package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveryWaitRulesTest {

  @ParameterizedTest(name = "present {0}, same world {1}, {2} blocks, wait over {3} -> home {4}")
  @CsvSource({
    "true, true, 3, false, false",
    "true, true, 48, false, false",
    "true, true, 48.5, false, true",
    "true, true, 3, true, true",
    "true, false, 3, false, true",
    "false, true, 3, false, true"
  })
  @DisplayName("a dog waits beside its recipient until they leave, wander off or time runs out")
  void headsHome(
      final boolean present,
      final boolean sameWorld,
      final double distance,
      final boolean waitOver,
      final boolean expected) {
    assertEquals(expected, DeliveryWaitRules.headsHome(present, sameWorld, distance, waitOver));
  }

  @ParameterizedTest(name = "reason {0} -> chunk unload {1}")
  @CsvSource(
      nullValues = "none",
      value = {
        "none, true",
        "UNLOADED_TO_CHUNK, true",
        "DISCARDED, false",
        "KILLED, false",
        "CHANGED_DIMENSION, false",
        "UNLOADED_WITH_PLAYER, false"
      })
  @DisplayName("only a chunk going out of view, unloading or the world closing counts as an unload")
  void chunkUnload(final Entity.RemovalReason reason, final boolean expected) {
    assertEquals(expected, DeliveryWaitRules.isChunkUnload(reason));
  }

  @ParameterizedTest(name = "tagged {0}, leg {1} -> stale {2}")
  @CsvSource(
      nullValues = "none",
      value = {
        "true, none, true",
        "true, TO_RECIPIENT, true",
        "true, TO_OWNER, true",
        "true, WAITING, false",
        "false, none, false",
        "false, TO_OWNER, false"
      })
  @DisplayName("a dog marked as waiting is a stale copy unless its delivery is still waiting")
  void staleCopy(final boolean tagged, final DeliveryLeg leg, final boolean expected) {
    assertEquals(expected, DeliveryWaitRules.isStaleCopy(tagged, Optional.ofNullable(leg)));
  }

  @ParameterizedTest(name = "sneaking {0}, waiting for them {1}, already viewing {2} -> {3}")
  @CsvSource({
    "false, true, false, OPEN",
    "false, true, true, KEEP_OPEN",
    "true, true, false, PASS",
    "true, true, true, PASS",
    "false, false, false, PASS",
    "false, false, true, PASS"
  })
  @DisplayName("the recipient's click opens the bags, keeps them open, or passes")
  void recipientClick(
      final boolean sneaking,
      final boolean waitingFor,
      final boolean viewing,
      final DeliveryWaitRules.BagsClick expected) {
    assertEquals(expected, DeliveryWaitRules.recipientClick(sneaking, waitingFor, viewing));
  }
}
