package com.grahambartley.dogsunleashed.advancement;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DogTamedCriterion extends AbstractCriterion<DogTamedCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "dog_tamed");
  public static final DogTamedCriterion INSTANCE = new DogTamedCriterion();

  private DogTamedCriterion() {}

  public static DogTamedCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final ServerPlayerEntity player, final String breed, final String coat) {
    this.trigger(player, conditions -> conditions.matches(breed, coat));
  }

  public record Conditions(
      Optional<LootContextPredicate> player, Optional<String> breed, Optional<String> coat)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Codec.STRING.optionalFieldOf("breed").forGetter(Conditions::breed),
                        Codec.STRING.optionalFieldOf("coat").forGetter(Conditions::coat))
                    .apply(instance, Conditions::new));

    public boolean matches(final String tamedBreed, final String tamedCoat) {
      return this.breed.map(tamedBreed::equals).orElse(true)
          && this.coat.map(tamedCoat::equals).orElse(true);
    }
  }
}
