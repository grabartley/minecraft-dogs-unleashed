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

public final class DogExpeditionReturnedCriterion
    extends AbstractCriterion<DogExpeditionReturnedCriterion.Conditions> {
  public static final Identifier ID =
      Identifier.of(DogsUnleashed.MOD_ID, "dog_expedition_returned");
  public static final DogExpeditionReturnedCriterion INSTANCE =
      new DogExpeditionReturnedCriterion();

  private DogExpeditionReturnedCriterion() {}

  public static DogExpeditionReturnedCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final ServerPlayerEntity player, final boolean rareFind) {
    this.trigger(player, conditions -> conditions.matches(rareFind));
  }

  public record Conditions(Optional<LootContextPredicate> player, Optional<Boolean> rareFind)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Codec.BOOL.optionalFieldOf("rare_find").forGetter(Conditions::rareFind))
                    .apply(instance, Conditions::new));

    public boolean matches(final boolean rare) {
      return this.rareFind.map(wanted -> wanted == rare).orElse(true);
    }
  }
}
