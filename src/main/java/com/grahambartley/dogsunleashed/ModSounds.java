package com.grahambartley.dogsunleashed;

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
  public static final SoundEvent DIRE_WOLF_BARK = registerSound("entity.direwolf.bark");

  public static final SoundEvent BEAGLE_ALERT = registerSound("entity.beagle.alert");
  public static final SoundEvent DACHSHUND_ALERT = registerSound("entity.dachshund.alert");
  public static final SoundEvent GOLDEN_RETRIEVER_ALERT =
      registerSound("entity.goldenretriever.alert");
  public static final SoundEvent SHIBA_INU_ALERT = registerSound("entity.shibainu.alert");
  public static final SoundEvent DIRE_WOLF_ALERT = registerSound("entity.direwolf.alert");

  public static final SoundEvent HUSKY_HOWL = registerSound("entity.husky.howl");
  public static final SoundEvent DIRE_WOLF_HOWL = registerSound("entity.direwolf.howl");
  public static final SoundEvent DIRE_WOLF_GROWL = registerSound("entity.direwolf.growl");

  public static final SoundEvent DOG_WHISTLE_BLOW = registerSound("item.dog_whistle.blow");
  public static final SoundEvent DOG_WHISTLE_POINT = registerSound("item.dog_whistle.point");

  private static SoundEvent registerSound(final String name) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, name);
    return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
  }

  public static void initialize() {}
}
