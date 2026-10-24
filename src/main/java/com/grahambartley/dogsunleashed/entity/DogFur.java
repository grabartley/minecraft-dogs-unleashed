package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogShearBotchedCriterion;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.FurProfile;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import java.util.OptionalInt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class DogFur {

  public static final Identifier OVERGROWN_SPEED_MODIFIER_ID =
      Identifier.of(DogsUnleashed.MOD_ID, "overgrown_fur_movement_speed");

  private static final int TRIM_PARTICLES = 12;
  private static final double TRIM_PARTICLE_SPEED = 0.05;

  private final UnleashedDogEntity dog;
  private int ticksInStage;

  DogFur(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static boolean rollsStageOnSpawn(final SpawnReason reason) {
    return reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION;
  }

  public static EntityAttributeModifier overgrownSpeedModifier() {
    return new EntityAttributeModifier(
        OVERGROWN_SPEED_MODIFIER_ID,
        DogFurStage.OVERGROWN_SPEED_MULTIPLIER,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
  }

  public DogFurStage stage() {
    return DogFurStage.fromOrdinal(this.dog.getDataTracker().get(UnleashedDogEntity.FUR_STAGE));
  }

  public int ticksInStage() {
    return this.ticksInStage;
  }

  public FurProfile profile() {
    return DogFurRules.profileFor(this.dog.getBreed(), this.dog.getGenome());
  }

  public void setStage(final DogFurStage stage) {
    this.dog.getDataTracker().set(UnleashedDogEntity.FUR_STAGE, stage.ordinal());
    this.ticksInStage = 0;
    this.syncSpeedModifier();
  }

  public void restoreStage(final DogFurStage saved) {
    this.setStage(saved.atMost(this.profile().maxStage()));
  }

  void tick() {
    final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
    if (this.dog.isBaby() || !config.fur().furGrowthEnabled()) {
      return;
    }
    final FurProfile profile = this.profile();
    final DogFurStage stage = this.stage();
    if (stage.ordinal() >= profile.maxStage().ordinal()) {
      return;
    }
    final OptionalInt budget =
        DogFurRules.ticksPerStage(
            profile, stage, this.growthRate(), config.fur().furGrowthRateMultiplierPercent());
    if (budget.isEmpty()) {
      return;
    }
    this.ticksInStage++;
    if (this.ticksInStage >= budget.getAsInt()) {
      this.setStage(stage.next());
    }
  }

  void rollSpawnStage(final SpawnReason reason) {
    if (!rollsStageOnSpawn(reason) || this.dog.isBaby()) {
      return;
    }
    this.setStage(
        DogFurRules.spawnStage(
            this.dog.getRandom().nextInt(DogFurRules.PERCENT), this.profile().maxStage()));
  }

  @Nullable
  ActionResult tryTrim(final PlayerEntity player, final Hand hand, final ItemStack shears) {
    if (!(shears.getItem() instanceof ShearsItem)) {
      return null;
    }
    if (this.dog.isBaby()) {
      player.sendMessage(Text.translatable("message.dogs-unleashed.fur_puppy"), true);
      return ActionResult.SUCCESS;
    }
    final DogFurStage from = this.stage();
    if (!from.canBeTrimmed()) {
      player.sendMessage(Text.translatable("message.dogs-unleashed.fur_already_trimmed"), true);
      return ActionResult.SUCCESS;
    }
    final boolean botched =
        DogFurRules.isBotched(
            from,
            DogsUnleashed.SERVER_CONFIG.fur().badHairDayChanceMultiplierPercent(),
            this.dog.getRandom().nextInt(DogFurRules.PERCENT));
    final int yield = DogFurRules.shearYield(from, this.profile().shearYieldPerStage(), botched);
    this.setStage(botched ? DogFurStage.BOTCHED : DogFurStage.TRIMMED);
    shears.damage(1, player, LivingEntity.getSlotForHand(hand));
    this.dog.dropStack(new ItemStack(ModItems.DOG_FUR, yield), 1.0f);
    this.dog
        .getWorld()
        .playSoundFromEntity(
            null, this.dog, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0f, 1.0f);
    this.dog.emitGameEvent(GameEvent.SHEAR, player);
    if (this.dog.getWorld() instanceof ServerWorld world) {
      world.spawnParticles(
          new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(ModItems.DOG_FUR)),
          this.dog.getX(),
          this.dog.getBodyY(0.6),
          this.dog.getZ(),
          TRIM_PARTICLES,
          this.dog.getWidth() * 0.4,
          this.dog.getHeight() * 0.3,
          this.dog.getWidth() * 0.4,
          TRIM_PARTICLE_SPEED);
    }
    if (botched) {
      this.dog.playSoundIfNotSilent(SoundEvents.ENTITY_WOLF_WHINE);
      if (player instanceof ServerPlayerEntity serverPlayer) {
        DogShearBotchedCriterion.INSTANCE.trigger(serverPlayer);
      }
    }
    return ActionResult.SUCCESS;
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putInt(ModNbtKeys.FUR_STAGE, this.stage().ordinal());
    nbt.putInt(ModNbtKeys.FUR_TICKS_IN_STAGE, this.ticksInStage);
  }

  void readNbt(final NbtCompound nbt) {
    if (!nbt.contains(ModNbtKeys.FUR_STAGE, NbtElement.NUMBER_TYPE)) {
      this.syncSpeedModifier();
      return;
    }
    this.restoreStage(DogFurStage.fromOrdinal(nbt.getInt(ModNbtKeys.FUR_STAGE)));
    this.ticksInStage = Math.max(0, nbt.getInt(ModNbtKeys.FUR_TICKS_IN_STAGE));
  }

  private double growthRate() {
    final DogGenome genome = this.dog.getGenome();
    return genome != null ? genome.furGrowthRate() : DogGenome.BASELINE_FUR_GROWTH_RATE;
  }

  private void syncSpeedModifier() {
    final EntityAttributeInstance speed =
        this.dog.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    if (speed == null) {
      return;
    }
    final boolean slowed = this.stage().slowsMovement();
    if (slowed && !speed.hasModifier(OVERGROWN_SPEED_MODIFIER_ID)) {
      speed.addTemporaryModifier(overgrownSpeedModifier());
    } else if (!slowed) {
      speed.removeModifier(OVERGROWN_SPEED_MODIFIER_ID);
    }
  }
}
