package com.grahambartley.dogsunleashed.render.overlay;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.DogAging;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class SeniorMuzzle {

  public static final Identifier SHEET =
      Identifier.of(MOD_ID, "textures/entity/overlay/grey_muzzle.png");

  private SeniorMuzzle() {}

  public static void register() {
    registerOn(OverlayEffects.dogs(), dog -> dog.getComponents().get(DogAging.class).isSenior());
  }

  static <T> void registerOn(final OverlayEffects<T> effects, final Predicate<? super T> isSenior) {
    effects.register(SHEET, isSenior);
  }
}
