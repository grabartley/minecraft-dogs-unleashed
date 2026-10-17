package com.grahambartley.dogsunleashed.render.overlay;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class OverlayEffects<T> {

  private static final OverlayEffects<UnleashedDogEntity> DOGS = new OverlayEffects<>();

  private final List<Map.Entry<Identifier, Predicate<? super T>>> effects =
      new CopyOnWriteArrayList<>();

  public static OverlayEffects<UnleashedDogEntity> dogs() {
    return DOGS;
  }

  public void register(final Identifier sheet, final Predicate<? super T> showsOn) {
    this.effects.removeIf(effect -> effect.getKey().equals(sheet));
    this.effects.add(Map.entry(sheet, showsOn));
  }

  public List<Identifier> activeOn(final T target) {
    return this.effects.stream()
        .filter(effect -> effect.getValue().test(target))
        .map(Map.Entry::getKey)
        .toList();
  }
}
