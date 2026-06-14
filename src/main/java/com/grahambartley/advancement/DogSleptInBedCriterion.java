package com.grahambartley.advancement;

import com.grahambartley.DogsUnleashed;
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

public final class DogSleptInBedCriterion
    extends AbstractCriterion<DogSleptInBedCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "dog_slept_in_bed");
  public static final DogSleptInBedCriterion INSTANCE = new DogSleptInBedCriterion();

  private DogSleptInBedCriterion() {}

  public static DogSleptInBedCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(ServerPlayerEntity player) {
    this.trigger(player, conditions -> true);
  }

  public record Conditions(Optional<LootContextPredicate> player)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player))
                    .apply(instance, Conditions::new));
  }
}
