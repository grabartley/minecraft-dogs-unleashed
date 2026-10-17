package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CacheReloaderTest {

  private static final Identifier ID = Identifier.of("dogs-unleashed", "some_cache");

  @Test
  @DisplayName("the listener is registered under the id it was given")
  void idIsTheOneGiven() {
    assertEquals(ID, new CacheReloader(ID, () -> {}).getFabricId());
  }

  @Test
  @DisplayName("every resource reload runs the clear action once")
  void reloadRunsTheClearAction() {
    final int[] clears = {0};
    final CacheReloader reloader = new CacheReloader(ID, () -> clears[0]++);

    reloader.reload(mock(ResourceManager.class));
    reloader.reload(mock(ResourceManager.class));

    assertEquals(2, clears[0]);
  }
}
