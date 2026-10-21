package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryTest {

  static Delivery sample(final UUID pet, final DeliveryLeg leg) {
    final NbtCompound dog = new NbtCompound();
    dog.putString("CustomName", "\"Balto\"");
    return new Delivery(
        pet,
        UUID.randomUUID(),
        UUID.randomUUID(),
        Identifier.of("dogs-unleashed", "husky"),
        dog,
        leg,
        400L,
        1400L);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DeliveryLeg.class)
  @DisplayName("a delivery on any leg survives a save and load")
  void roundTrips(final DeliveryLeg leg) {
    final Delivery delivery = sample(UUID.randomUUID(), leg);
    assertEquals(delivery, Delivery.fromNbt(delivery.toNbt()).orElseThrow());
  }

  @ParameterizedTest(name = "missing {0}")
  @ValueSource(strings = {"Pet", "Owner", "Recipient", "Dog"})
  @DisplayName("an entry missing a required part is skipped")
  void skipsBrokenEntries(final String key) {
    final NbtCompound nbt = sample(UUID.randomUUID(), DeliveryLeg.TO_RECIPIENT).toNbt();
    nbt.remove(key);
    assertTrue(Delivery.fromNbt(nbt).isEmpty());
  }

  @Test
  @DisplayName("an entry with a bad type or leg is skipped")
  void skipsBadIds() {
    final NbtCompound badType = sample(UUID.randomUUID(), DeliveryLeg.TO_OWNER).toNbt();
    badType.putString(ModNbtKeys.DELIVERY_ENTITY_TYPE, "Not An Id");
    final NbtCompound badLeg = sample(UUID.randomUUID(), DeliveryLeg.TO_OWNER).toNbt();
    badLeg.putString(ModNbtKeys.DELIVERY_LEG, "lost");
    assertTrue(Delivery.fromNbt(badType).isEmpty());
    assertTrue(Delivery.fromNbt(badLeg).isEmpty());
  }

  @ParameterizedTest(name = "at {0} the leg is over: {1}")
  @CsvSource({"1399, false", "1400, true", "5000, true"})
  @DisplayName("a leg is over once game time reaches its end")
  void legOver(final long gameTime, final boolean expected) {
    assertEquals(expected, sample(UUID.randomUUID(), DeliveryLeg.TO_RECIPIENT).isLegOver(gameTime));
  }

  @Test
  @DisplayName("arriving drops the snapshot and waits from now")
  void waitingClearsSnapshot() {
    final Delivery waiting =
        sample(UUID.randomUUID(), DeliveryLeg.TO_RECIPIENT).waiting(2000L, 600L);
    assertEquals(DeliveryLeg.WAITING, waiting.leg());
    assertTrue(waiting.dog().isEmpty());
    assertEquals(2600L, waiting.legEndsAt());
    assertEquals(400L, waiting.legTicks());
  }

  @Test
  @DisplayName("heading home carries the new snapshot and leg time")
  void homewardCarriesSnapshot() {
    final NbtCompound snapshot = new NbtCompound();
    snapshot.putInt("Health", 7);
    final Delivery homeward =
        sample(UUID.randomUUID(), DeliveryLeg.WAITING).homeward(snapshot, 3000L, 250L);
    assertEquals(DeliveryLeg.TO_OWNER, homeward.leg());
    assertEquals(snapshot, homeward.dog());
    assertEquals(250L, homeward.legTicks());
    assertEquals(3250L, homeward.legEndsAt());
  }
}
