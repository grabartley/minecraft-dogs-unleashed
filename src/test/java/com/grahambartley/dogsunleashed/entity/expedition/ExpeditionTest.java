package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpeditionTest {

  static Expedition sample(final UUID pet, final long returnTime) {
    final NbtCompound dog = new NbtCompound();
    dog.putString("CustomName", "\"Balto\"");
    return new Expedition(
        pet,
        UUID.randomUUID(),
        Identifier.of("dogs-unleashed", "husky"),
        dog,
        Identifier.ofVanilla("overworld"),
        640,
        -220,
        returnTime,
        100L);
  }

  @Test
  @DisplayName("an expedition survives a save and load")
  void roundTrips() {
    final Expedition expedition = sample(UUID.randomUUID(), 24000L);
    assertEquals(expedition, Expedition.fromNbt(expedition.toNbt()).orElseThrow());
  }

  @Test
  @DisplayName("an entry missing its dog or with a bad id is skipped")
  void skipsBrokenEntries() {
    final NbtCompound noDog = sample(UUID.randomUUID(), 24000L).toNbt();
    noDog.remove(ModNbtKeys.EXPEDITION_DOG);
    final NbtCompound badType = sample(UUID.randomUUID(), 24000L).toNbt();
    badType.putString(ModNbtKeys.EXPEDITION_ENTITY_TYPE, "Not An Id");
    assertTrue(Expedition.fromNbt(noDog).isEmpty());
    assertTrue(Expedition.fromNbt(badType).isEmpty());
    assertTrue(Expedition.fromNbt(new NbtCompound()).isEmpty());
  }
}
