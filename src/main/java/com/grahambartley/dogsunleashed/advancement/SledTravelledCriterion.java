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

public final class SledTravelledCriterion
    extends AbstractCriterion<SledTravelledCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "sled_travelled");
  public static final SledTravelledCriterion INSTANCE = new SledTravelledCriterion();

  private SledTravelledCriterion() {}

  public static SledTravelledCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final ServerPlayerEntity player, final double blocks) {
    this.trigger(player, conditions -> conditions.matches(blocks));
  }

  public record Conditions(Optional<LootContextPredicate> player, Optional<Double> minBlocks)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Codec.DOUBLE.optionalFieldOf("min_blocks").forGetter(Conditions::minBlocks))
                    .apply(instance, Conditions::new));

    public boolean matches(final double blocks) {
      return this.minBlocks.map(min -> blocks >= min).orElse(true);
    }
  }
}
