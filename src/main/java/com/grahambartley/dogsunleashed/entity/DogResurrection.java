package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.advancement.PetResurrectedCriterion;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Raises a deceased pet as its undead self. The pet record is the canonical identity, so the new
 * dog is rebuilt from it rather than from anything the dead entity left behind: name, breed, coat,
 * eye colour, collar, lineage, and genome all come back intact.
 */
public final class DogResurrection {

  private static final int RITUAL_PARTICLE_COUNT = 40;
  private static final double RITUAL_PARTICLE_SPREAD = 0.6;
  private static final double RITUAL_PARTICLE_SPEED = 0.15;

  // Matches what a held Totem of Undying grants in vanilla, minus Regeneration, which the dog's
  // own undead typing refuses. Fire Resistance is the load-bearing one: the ritual needs a
  // thunderstorm, and a storm keeps striking after the dog is up, so a freshly raised half-health
  // pet would otherwise risk burning to permanent loss at its own grave.
  private static final int TOTEM_FIRE_RESISTANCE_TICKS = 800;
  private static final int TOTEM_ABSORPTION_TICKS = 100;
  private static final int TOTEM_ABSORPTION_AMPLIFIER = 1;

  private DogResurrection() {}

  /**
   * Whether this grave is ready to give its pet back: it must hold a totem, and the pet it belongs
   * to must be merely deceased rather than lost while undead.
   */
  public static boolean canResurrect(final ServerWorld world, final DogGraveBlockEntity grave) {
    return resurrectablePet(world, grave) != null;
  }

  private static @Nullable PetData resurrectablePet(
      final ServerWorld world, final DogGraveBlockEntity grave) {
    if (!grave.hasTotem() || grave.getDogUuid() == null) {
      return null;
    }
    final PetData petData = PetManager.get(world.getServer()).getPetByEntityId(grave.getDogUuid());
    return petData != null && petData.getLifeState().isResurrectable() ? petData : null;
  }

  /**
   * @return whether the ritual fired; a grave with no totem, no pet record, or a permanently lost
   *     pet consumes nothing and changes nothing
   */
  public static boolean resurrect(
      final ServerWorld world, final BlockPos gravePos, final DogGraveBlockEntity grave) {
    final PetData petData = resurrectablePet(world, grave);
    if (petData == null) {
      return false;
    }
    final PetManager petManager = PetManager.get(world.getServer());

    final UnleashedDogEntity undeadDog = spawnUndeadDog(world, gravePos, petData);
    if (undeadDog == null) {
      return false;
    }

    final UUID installerId = grave.getTotemInstallerId();
    grave.clearTotem();
    world.removeBlock(gravePos, false);
    consumeRodAbove(world, gravePos);

    petManager.markPetResurrected(petData.getPetId());
    petData.setHealth(undeadDog.getHealth());
    petData.setMaxHealth(undeadDog.getMaxHealth());
    petData.setLastKnownPosition(gravePos);
    petData.setDimension(world.getRegistryKey().getValue().toString());
    petManager.updatePet(petData);

    playRitualEffects(world, gravePos);
    triggerAdvancement(world, installerId);
    return true;
  }

  private static @Nullable UnleashedDogEntity spawnUndeadDog(
      final ServerWorld world, final BlockPos gravePos, final PetData petData) {
    final UnleashedDogEntity dog =
        ModEntities.getZombieDogEntityType(petData.getBreed()).create(world);
    if (dog == null) {
      return null;
    }

    dog.setUuid(petData.getPetId());
    dog.refreshPositionAndAngles(
        gravePos.getX() + 0.5,
        gravePos.getY(),
        gravePos.getZ() + 0.5,
        world.getRandom().nextFloat() * 360.0f,
        0.0f);

    final DogGenome genome =
        petData.getGenome() != null ? petData.getGenome() : DogGenome.pure(petData.getBreed());
    dog.applyGenome(genome);
    dog.applyTraits(
        new DogTraits(
            dog.getRigSourceBreed(), petData.getCoatVariant(), petData.getHuskyEyeVariant()));
    dog.setCollarColor(DyeColor.byId(petData.getCollarColorId()));
    dog.getLineage().setParentDogUuid(petData.getParentAId());
    dog.getLineage().setSecondParentDogUuid(petData.getParentBId());
    dog.setBaby(petData.isBaby());
    dog.setCustomName(Text.literal(petData.getName()));
    dog.setCustomNameVisible(true);
    dog.setOwnerUuid(petData.getOwnerId());
    dog.setTamed(true, true);
    dog.getCommandController().apply(DogCommand.FOLLOW);
    dog.getUndeadState().applyAttributeScaling();
    dog.setHealth(dog.getMaxHealth());
    applyTotemBlessing(dog);

    world.spawnEntity(dog);
    return dog;
  }

  private static void applyTotemBlessing(final UnleashedDogEntity dog) {
    dog.addStatusEffect(
        new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, TOTEM_FIRE_RESISTANCE_TICKS, 0));
    dog.addStatusEffect(
        new StatusEffectInstance(
            StatusEffects.ABSORPTION, TOTEM_ABSORPTION_TICKS, TOTEM_ABSORPTION_AMPLIFIER));
  }

  /** The rod is spent along with the totem and the grave, rather than left hanging over nothing. */
  private static void consumeRodAbove(final ServerWorld world, final BlockPos gravePos) {
    if (world.getBlockState(gravePos.up()).isOf(Blocks.LIGHTNING_ROD)) {
      world.removeBlock(gravePos.up(), false);
    }
  }

  private static void playRitualEffects(final ServerWorld world, final BlockPos gravePos) {
    world.playSound(null, gravePos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    world.spawnParticles(
        ParticleTypes.TOTEM_OF_UNDYING,
        gravePos.getX() + 0.5,
        gravePos.getY() + 1.0,
        gravePos.getZ() + 0.5,
        RITUAL_PARTICLE_COUNT,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPEED);
    world.spawnParticles(
        ParticleTypes.SOUL,
        gravePos.getX() + 0.5,
        gravePos.getY() + 0.5,
        gravePos.getZ() + 0.5,
        RITUAL_PARTICLE_COUNT / 2,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPREAD,
        RITUAL_PARTICLE_SPEED);
  }

  private static void triggerAdvancement(
      final ServerWorld world, final @Nullable UUID installerId) {
    if (installerId == null) {
      return;
    }
    final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(installerId);
    if (player != null) {
      PetResurrectedCriterion.INSTANCE.trigger(player);
    }
  }
}
