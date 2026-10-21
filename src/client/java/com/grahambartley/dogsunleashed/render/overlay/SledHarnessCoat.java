package com.grahambartley.dogsunleashed.render.overlay;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class SledHarnessCoat {

  public static final Identifier SHEET =
      Identifier.of(MOD_ID, "textures/entity/overlay/harness.png");

  private SledHarnessCoat() {}

  public static void register() {
    registerOn(
        OverlayEffects.dogs(), dog -> dog.getComponents().get(DogSledHarness.class).isHitched());
  }

  static <T> void registerOn(final OverlayEffects<T> effects, final Predicate<? super T> hitched) {
    effects.register(SHEET, hitched);
  }
}
