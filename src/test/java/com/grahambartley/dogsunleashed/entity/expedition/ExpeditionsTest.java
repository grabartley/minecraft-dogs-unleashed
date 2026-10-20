package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpeditionsTest {

  @Test
  @DisplayName("a dog can only be on one expedition at a time")
  void oneExpeditionPerDog() {
    final Expeditions expeditions = new Expeditions();
    final UUID pet = UUID.randomUUID();
    assertTrue(expeditions.add(ExpeditionTest.sample(pet, 24000L)));
    assertFalse(expeditions.add(ExpeditionTest.sample(pet, 48000L)));
    assertTrue(expeditions.isAway(pet));
    assertEquals(24000L, expeditions.of(pet).orElseThrow().returnTimeOfDay());
    assertTrue(expeditions.isDirty());
    assertTrue(expeditions.remove(pet));
    assertFalse(expeditions.remove(pet));
    assertFalse(expeditions.isAway(pet));
  }

  @Test
  @DisplayName("only trips whose return time has come are due")
  void dueTrips() {
    final Expeditions expeditions = new Expeditions();
    final UUID early = UUID.randomUUID();
    final UUID late = UUID.randomUUID();
    expeditions.add(ExpeditionTest.sample(early, 24000L));
    expeditions.add(ExpeditionTest.sample(late, 48000L));
    assertEquals(List.of(), expeditions.due(23999L, 200L));
    assertEquals(
        List.of(early), expeditions.due(24000L, 200L).stream().map(Expedition::pet).toList());
  }

  @Test
  @DisplayName("trips survive a save and load")
  void roundTrips() {
    final Expeditions expeditions = new Expeditions();
    final UUID pet = UUID.randomUUID();
    expeditions.add(ExpeditionTest.sample(pet, 24000L));
    final Expeditions loaded =
        Expeditions.fromNbt(expeditions.writeNbt(new NbtCompound(), null), null);
    assertEquals(expeditions.of(pet), loaded.of(pet));
  }
}
