package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.advancement.PetCuredCriterion;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.UUID;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

/**
 * The golden apple cure, mirroring the zombie villager: weaken the dog, feed it a Golden Apple, and
 * it converts back to its living self after a timer. The dog is recreated at the living entity type
 * on completion, so its identity survives the swap intact.
 */
public final class DogCuring {

  public static final int MIN_CONVERSION_TICKS = 3600;
  public static final int MAX_CONVERSION_TICKS = 6000;

  private static final int COMPLETION_PARTICLE_COUNT = 20;
  private static final double COMPLETION_PARTICLE_SPREAD = 0.5;

  private final UnleashedDogEntity dog;
  private int remainingTicks;
  private @Nullable UUID curerId;

  DogCuring(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static int rollConversionTicks(final Random random) {
    return MIN_CONVERSION_TICKS + random.nextInt(MAX_CONVERSION_TICKS - MIN_CONVERSION_TICKS + 1);
  }

  public static int sanitizedRemainingTicks(final int savedTicks) {
    return Math.max(0, savedTicks);
  }

  public boolean isConverting() {
    return this.remainingTicks > 0;
  }

  public int getRemainingTicks() {
    return this.remainingTicks;
  }

  public @Nullable UUID getCurerId() {
    return this.curerId;
  }

  public void restoreFromSave(final int remainingTicks, final @Nullable UUID curerId) {
    this.remainingTicks = sanitizedRemainingTicks(remainingTicks);
    this.curerId = curerId;
  }

  /** A cure needs an undead dog that is already weakened and is not mid-conversion already. */
  public boolean canStart(final ItemStack heldStack) {
    return this.dog.isUndead()
        && !this.isConverting()
        && heldStack.isOf(Items.GOLDEN_APPLE)
        && this.dog.hasStatusEffect(StatusEffects.WEAKNESS);
  }

  public void start(final PlayerEntity player) {
    this.remainingTicks = rollConversionTicks(this.dog.getRandom());
    this.curerId = player.getUuid();
    this.dog.removeStatusEffect(StatusEffects.WEAKNESS);
    this.dog.addStatusEffect(
        new StatusEffectInstance(StatusEffects.STRENGTH, this.remainingTicks, 0));
    this.dog
        .getWorld()
        .playSoundFromEntity(
            null,
            this.dog,
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE,
            SoundCategory.NEUTRAL,
            1.0f,
            1.0f);
  }

  void tick() {
    if (!this.isConverting()) {
      return;
    }
    this.remainingTicks--;
    if (this.remainingTicks <= 0) {
      this.finish();
    }
  }

  /**
   * Writes the living body back onto the pet record. The record's max health is only ever set at
   * registration and at each conversion, so without this a cured pet would keep advertising the
   * halved max health it had while undead.
   */
  private static void recordTheCure(final ServerWorld world, final UnleashedDogEntity cured) {
    final PetManager petManager = PetManager.get(world.getServer());
    petManager.markPetCured(cured.getUuid());
    final PetData pet = petManager.getPetByEntityId(cured.getUuid());
    if (pet != null) {
      pet.setHealth(cured.getHealth());
      pet.setMaxHealth(cured.getMaxHealth());
      petManager.updatePet(pet);
    }
  }

  private void finish() {
    if (!(this.dog.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final UUID curer = this.curerId;
    this.remainingTicks = 0;
    this.curerId = null;

    final UnleashedDogEntity cured =
        DogRecreation.recreateAs(
            this.dog, ModEntities.getDogEntityType(this.dog.getBreed()), world, this.dog.getPos());
    if (cured == null) {
      return;
    }

    cured.removeStatusEffect(StatusEffects.STRENGTH);
    DogUndeadState.restoreLivingAttributes(cured);
    recordTheCure(world, cured);

    world.playSoundFromEntity(
        null,
        cured,
        SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
        SoundCategory.NEUTRAL,
        1.0f,
        1.0f);
    world.spawnParticles(
        ParticleTypes.HAPPY_VILLAGER,
        cured.getX(),
        cured.getBodyY(1.0),
        cured.getZ(),
        COMPLETION_PARTICLE_COUNT,
        COMPLETION_PARTICLE_SPREAD,
        COMPLETION_PARTICLE_SPREAD,
        COMPLETION_PARTICLE_SPREAD,
        0.0);

    if (curer != null
        && world.getServer().getPlayerManager().getPlayer(curer)
            instanceof ServerPlayerEntity serverPlayer) {
      PetCuredCriterion.INSTANCE.trigger(serverPlayer);
    }
  }
}
