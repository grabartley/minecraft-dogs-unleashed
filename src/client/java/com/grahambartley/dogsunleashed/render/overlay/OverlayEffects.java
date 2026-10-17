package com.grahambartley.dogsunleashed.render.overlay;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class OverlayEffects<T> {

  private static final OverlayEffects<UnleashedDogEntity> DOGS = new OverlayEffects<>();

  private record Effect<T>(Identifier sheet, Predicate<? super T> showsOn) {}

  private final List<Effect<T>> effects = new CopyOnWriteArrayList<>();

  public static OverlayEffects<UnleashedDogEntity> dogs() {
    return DOGS;
  }

  public void register(final Identifier sheet, final Predicate<? super T> showsOn) {
    this.effects.removeIf(effect -> effect.sheet().equals(sheet));
    this.effects.add(new Effect<>(sheet, showsOn));
  }

  public List<Identifier> activeOn(final T target) {
    if (this.effects.isEmpty()) {
      return List.of();
    }
    final List<Identifier> active = new ArrayList<>();
    for (final Effect<T> effect : this.effects) {
      if (effect.showsOn().test(target)) {
        active.add(effect.sheet());
      }
    }
    return active;
  }
}
