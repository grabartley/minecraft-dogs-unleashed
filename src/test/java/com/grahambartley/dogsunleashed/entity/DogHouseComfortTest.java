package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogHouseComfortTest {

  @Test
  @DisplayName("waking in a dog house grants Regeneration I for thirty seconds")
  void wakeEffectIsRegenerationOneForThirtySeconds() {
    final StatusEffectInstance effect = DogHouseComfort.wakeEffect();

    assertSame(StatusEffects.REGENERATION, effect.getEffectType());
    assertEquals(0, effect.getAmplifier(), "amplifier 0 is Regeneration I");
    assertEquals(600, effect.getDuration(), "30 seconds at 20 ticks a second");
  }

  @Test
  @DisplayName("each wake-up builds its own effect, so one dog's timer cannot drain another's")
  void wakeEffectIsANewInstanceEachTime() {
    assertEquals(
        DogHouseComfort.wakeEffect().getDuration(),
        DogHouseComfort.wakeEffect().getDuration(),
        "a fresh instance should always start at full duration");
  }
}
