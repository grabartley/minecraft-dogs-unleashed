package com.grahambartley.dogsunleashed.render.overlay;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.DogMud;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public final class MuddyCoat {

  public static final Identifier SHEET = Identifier.of(MOD_ID, "textures/entity/overlay/mud.png");

  private MuddyCoat() {}

  public static void register() {
    registerOn(OverlayEffects.dogs(), dog -> dog.getComponents().get(DogMud.class).isMuddy());
  }

  static <T> void registerOn(final OverlayEffects<T> effects, final Predicate<? super T> isMuddy) {
    effects.register(SHEET, isMuddy);
  }
}
