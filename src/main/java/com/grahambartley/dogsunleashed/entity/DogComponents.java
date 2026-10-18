package com.grahambartley.dogsunleashed.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;

public final class DogComponents {

  private final Map<Class<? extends DogComponent>, DogComponent> byType = new LinkedHashMap<>();

  static DogComponents of(final DogComponent... components) {
    final DogComponents holder = new DogComponents();
    for (final DogComponent component : components) {
      if (holder.byType.putIfAbsent(component.getClass(), component) != null) {
        throw new IllegalArgumentException("Duplicate dog component " + component.getClass());
      }
    }
    return holder;
  }

  public <T extends DogComponent> T get(final Class<T> type) {
    final DogComponent component = this.byType.get(type);
    if (component == null) {
      throw new IllegalArgumentException("No dog component " + type);
    }
    return type.cast(component);
  }

  List<DogComponent> inOrder() {
    return List.copyOf(this.byType.values());
  }

  void tick() {
    this.byType.values().forEach(DogComponent::tick);
  }

  void writeNbt(final NbtCompound nbt) {
    this.byType.values().forEach(component -> component.writeNbt(nbt));
  }

  void readNbt(final NbtCompound nbt) {
    this.byType.values().forEach(component -> component.readNbt(nbt));
  }
}
