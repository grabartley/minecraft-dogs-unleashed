package com.grahambartley.dogsunleashed.advancement;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogMilestoneCriterion
    extends AbstractCriterion<DogMilestoneCriterion.Conditions> {
  public static final Identifier ID = Identifier.of(DogsUnleashed.MOD_ID, "dog_milestone");
  public static final DogMilestoneCriterion INSTANCE = new DogMilestoneCriterion();

  private DogMilestoneCriterion() {}

  public static DogMilestoneCriterion register() {
    return Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public void trigger(final @Nullable Entity player, final DogMilestone milestone) {
    if (player instanceof ServerPlayerEntity serverPlayer) {
      this.trigger(serverPlayer, conditions -> conditions.matches(milestone));
    }
  }

  public record Conditions(Optional<LootContextPredicate> player, DogMilestone milestone)
      implements AbstractCriterion.Conditions {
    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        DogMilestone.CODEC.fieldOf("milestone").forGetter(Conditions::milestone))
                    .apply(instance, Conditions::new));

    public boolean matches(final DogMilestone reached) {
      return this.milestone == reached;
    }
  }
}
