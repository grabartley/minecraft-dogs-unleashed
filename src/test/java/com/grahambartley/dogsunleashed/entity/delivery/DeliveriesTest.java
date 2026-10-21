package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveriesTest {

  @Test
  @DisplayName("a dog can only be on one delivery at a time")
  void addRefusesASecondDelivery() {
    final Deliveries deliveries = new Deliveries();
    final UUID pet = UUID.randomUUID();
    assertTrue(deliveries.add(DeliveryTest.sample(pet, DeliveryLeg.TO_RECIPIENT)));
    assertFalse(deliveries.add(DeliveryTest.sample(pet, DeliveryLeg.TO_OWNER)));
    assertEquals(DeliveryLeg.TO_RECIPIENT, deliveries.of(pet).orElseThrow().leg());
  }

  @Test
  @DisplayName("replace moves a delivery on to its next leg and remove ends it")
  void replaceAndRemove() {
    final Deliveries deliveries = new Deliveries();
    final UUID pet = UUID.randomUUID();
    final Delivery outbound = DeliveryTest.sample(pet, DeliveryLeg.TO_RECIPIENT);
    deliveries.add(outbound);
    deliveries.replace(outbound.waiting(10L, 20L));
    assertEquals(DeliveryLeg.WAITING, deliveries.of(pet).orElseThrow().leg());
    assertTrue(deliveries.remove(pet));
    assertFalse(deliveries.remove(pet));
    assertTrue(deliveries.of(pet).isEmpty());
  }

  @ParameterizedTest(name = "{0} off world {1}")
  @CsvSource({"TO_RECIPIENT, true", "WAITING, false", "TO_OWNER, true"})
  @DisplayName("only travelling dogs count as out of the world")
  void offWorld(final DeliveryLeg leg, final boolean expected) {
    final Deliveries deliveries = new Deliveries();
    final UUID pet = UUID.randomUUID();
    deliveries.add(DeliveryTest.sample(pet, leg));
    assertEquals(expected, deliveries.isOffWorld(pet));
    assertFalse(deliveries.isOffWorld(UUID.randomUUID()));
  }

  @ParameterizedTest(name = "{0}, asked by recipient {1} -> {2}")
  @CsvSource({
    "WAITING, true, true",
    "WAITING, false, false",
    "TO_RECIPIENT, true, false",
    "TO_OWNER, true, false"
  })
  @DisplayName("a dog waits only for its own recipient, and only once it has arrived")
  void waitingFor(final DeliveryLeg leg, final boolean askedByRecipient, final boolean expected) {
    final Deliveries deliveries = new Deliveries();
    final Delivery delivery = DeliveryTest.sample(UUID.randomUUID(), leg);
    deliveries.add(delivery);
    final UUID asker = askedByRecipient ? delivery.recipient() : delivery.owner();
    assertEquals(expected, deliveries.isWaitingFor(delivery.pet(), asker));
  }

  @Test
  @DisplayName("every delivery survives a save and load")
  void roundTrips() {
    final Deliveries deliveries = new Deliveries();
    final Delivery first = DeliveryTest.sample(UUID.randomUUID(), DeliveryLeg.TO_RECIPIENT);
    final Delivery second = DeliveryTest.sample(UUID.randomUUID(), DeliveryLeg.TO_OWNER);
    deliveries.add(first);
    deliveries.add(second);
    final Deliveries loaded =
        Deliveries.fromNbt(deliveries.writeNbt(new NbtCompound(), null), null);
    assertEquals(first, loaded.of(first.pet()).orElseThrow());
    assertEquals(second, loaded.of(second.pet()).orElseThrow());
    assertEquals(2, loaded.all().size());
  }
}
