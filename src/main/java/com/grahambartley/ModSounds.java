package com.grahambartley;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

  public static final SoundEvent BEAGLE_BARK = registerSound("entity.beagle.bark");
  public static final SoundEvent DACHSHUND_BARK = registerSound("entity.dachshund.bark");
  public static final SoundEvent GOLDEN_RETRIEVER_BARK =
      registerSound("entity.goldenretriever.bark");
  public static final SoundEvent SHIBA_INU_BARK = registerSound("entity.shibainu.bark");

  public static final SoundEvent HUSKY_HOWL = registerSound("entity.husky.howl");

  private static SoundEvent registerSound(final String name) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, name);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }

  public static void initialize() {}
}
