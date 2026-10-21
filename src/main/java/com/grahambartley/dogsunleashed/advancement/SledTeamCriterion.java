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

public final class SledTeamCriterion extends AbstractCriterion<SledTeamCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "sled_team");
  public static final SledTeamCriterion INSTANCE = new SledTeamCriterion();

  private SledTeamCriterion() {}

  public static SledTeamCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final ServerPlayerEntity player, final int teamSize) {
    this.trigger(player, conditions -> conditions.matches(teamSize));
  }

  public record Conditions(Optional<LootContextPredicate> player, Optional<Integer> minTeam)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Codec.INT.optionalFieldOf("min_team").forGetter(Conditions::minTeam))
                    .apply(instance, Conditions::new));

    public boolean matches(final int teamSize) {
      return this.minTeam.map(min -> teamSize >= min).orElse(true);
    }
  }
}
