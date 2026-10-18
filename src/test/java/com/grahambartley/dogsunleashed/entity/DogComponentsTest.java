package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogComponentsTest {

  private static class Counter extends DogComponent {
    private final List<String> calls;
    int value;

    Counter(final List<String> calls) {
      this.calls = calls;
    }

    @Override
    void tick() {
      this.value++;
      this.calls.add(this.getClass().getSimpleName() + ".tick");
    }

    @Override
    void writeNbt(final NbtCompound nbt) {
      nbt.putInt(this.getClass().getSimpleName(), this.value);
      this.calls.add(this.getClass().getSimpleName() + ".write");
    }

    @Override
    void readNbt(final NbtCompound nbt) {
      this.value = nbt.getInt(this.getClass().getSimpleName());
      this.calls.add(this.getClass().getSimpleName() + ".read");
    }
  }

  private static final class First extends Counter {
    First(final List<String> calls) {
      super(calls);
    }

    First() {
      this(new ArrayList<>());
    }
  }

  private static final class Second extends Counter {
    Second(final List<String> calls) {
      super(calls);
    }

    Second() {
      this(new ArrayList<>());
    }
  }

  private static final class Silent extends DogComponent {}

  @Test
  @DisplayName("each component is found by its own type")
  void findsEachComponentByType() {
    final First first = new First();
    final Second second = new Second();
    final DogComponents components = DogComponents.of(first, second);
    assertSame(first, components.get(First.class));
    assertSame(second, components.get(Second.class));
  }

  @Test
  @DisplayName("asking for a component that was never added fails loudly")
  void missingComponentFails() {
    final DogComponents components = DogComponents.of(new First());
    assertThrows(IllegalArgumentException.class, () -> components.get(Second.class));
  }

  @Test
  @DisplayName("the same component type cannot be added twice")
  void duplicateTypeFails() {
    assertThrows(IllegalArgumentException.class, () -> DogComponents.of(new First(), new First()));
  }

  @Test
  @DisplayName("tick, write and read reach every component in the order they were added")
  void callsReachEveryComponentInOrder() {
    final List<String> calls = new ArrayList<>();
    final DogComponents components =
        DogComponents.of(new First(calls), new Second(calls), new Silent());
    final NbtCompound nbt = new NbtCompound();
    components.tick();
    components.writeNbt(nbt);
    components.readNbt(nbt);
    assertEquals(
        List.of(
            "First.tick",
            "Second.tick",
            "First.write",
            "Second.write",
            "First.read",
            "Second.read"),
        calls);
  }

  @Test
  @DisplayName("component state survives a write and read through NBT")
  void stateSurvivesNbt() {
    final First written = new First();
    DogComponents.of(written).tick();
    final NbtCompound nbt = new NbtCompound();
    DogComponents.of(written).writeNbt(nbt);
    final First read = new First();
    DogComponents.of(read).readNbt(nbt);
    assertEquals(1, read.value);
  }
}
