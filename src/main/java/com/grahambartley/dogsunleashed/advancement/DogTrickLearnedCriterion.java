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

public final class DogTrickLearnedCriterion
    extends AbstractCriterion<DogTrickLearnedCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "dog_trick_learned");
  public static final DogTrickLearnedCriterion INSTANCE = new DogTrickLearnedCriterion();

  private DogTrickLearnedCriterion() {}

  public static DogTrickLearnedCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final ServerPlayerEntity player, final int learnedCount) {
    this.trigger(player, conditions -> conditions.matches(learnedCount));
  }

  public record Conditions(Optional<LootContextPredicate> player, Optional<Integer> minLearned)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Codec.INT.optionalFieldOf("min_learned").forGetter(Conditions::minLearned))
                    .apply(instance, Conditions::new));

    public boolean matches(final int learnedCount) {
      return this.minLearned.map(min -> learnedCount >= min).orElse(true);
    }
  }
}
