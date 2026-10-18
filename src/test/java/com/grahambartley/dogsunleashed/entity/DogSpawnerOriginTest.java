package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogSpawnerOriginTest {

  @Test
  @DisplayName("a new dog did not come from a spawner")
  void startsNotFromSpawner() {
    assertFalse(new DogSpawnerOrigin().isFromSpawner());
  }

  @ParameterizedTest(name = "fromSpawner={0} survives a save and load")
  @ValueSource(booleans = {true, false})
  @DisplayName("the flag survives a save and load under the same key")
  void flagSurvivesNbt(final boolean fromSpawner) {
    final DogSpawnerOrigin saved = new DogSpawnerOrigin();
    saved.setFromSpawner(fromSpawner);
    final NbtCompound nbt = new NbtCompound();
    saved.writeNbt(nbt);
    assertEquals(fromSpawner, nbt.getBoolean(ModNbtKeys.SPAWNED_BY_DOG_SPAWNER));
    final DogSpawnerOrigin loaded = new DogSpawnerOrigin();
    loaded.setFromSpawner(!fromSpawner);
    loaded.readNbt(nbt);
    assertEquals(fromSpawner, loaded.isFromSpawner());
  }

  @Test
  @DisplayName("a save without the key leaves the flag alone")
  void missingKeyKeepsTheFlag() {
    final DogSpawnerOrigin origin = new DogSpawnerOrigin();
    origin.setFromSpawner(true);
    origin.readNbt(new NbtCompound());
    assertEquals(true, origin.isFromSpawner());
  }
}
