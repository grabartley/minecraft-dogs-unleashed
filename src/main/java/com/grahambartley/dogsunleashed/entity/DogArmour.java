package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public final class DogArmour {

  static final float REPAIR_FRACTION = 0.125f;
  private static final int CRACK_PARTICLE_COUNT = 20;

  private final MobEntity wearer;

  DogArmour(final MobEntity wearer) {
    this.wearer = wearer;
  }

  public ItemStack worn() {
    return this.wearer.getEquippedStack(EquipmentSlot.BODY);
  }

  public boolean absorbs(final DamageSource source) {
    return !this.worn().isEmpty() && !source.isIn(DamageTypeTags.BYPASSES_WOLF_ARMOR);
  }

  @Nullable
  SoundEvent hurtSound(final DamageSource source) {
    return this.absorbs(source) ? SoundEvents.ITEM_WOLF_ARMOR_DAMAGE : null;
  }

  void takeHit(final DamageSource source, final float amount) {
    final ItemStack armour = this.worn();
    final int damageBefore = armour.getDamage();
    final int maxDamage = armour.getMaxDamage();
    final ItemStack shard = armour.copyWithCount(1);
    armour.damage(wearFrom(source, amount), this.wearer, EquipmentSlot.BODY);
    if (Cracks.WOLF_ARMOR.getCrackLevel(damageBefore, maxDamage)
        != Cracks.WOLF_ARMOR.getCrackLevel(this.worn())) {
      this.wearer.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_CRACK);
      if (this.wearer.getWorld() instanceof ServerWorld world) {
        world.spawnParticles(
            new ItemStackParticleEffect(ParticleTypes.ITEM, shard),
            this.wearer.getX(),
            this.wearer.getY() + 1.0,
            this.wearer.getZ(),
            CRACK_PARTICLE_COUNT,
            0.2,
            0.1,
            0.2,
            0.1);
      }
    }
  }

  private int wearFrom(final DamageSource source, final float amount) {
    if (amount <= 0.0f) {
      return 0;
    }
    if (source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
      return MathHelper.ceil(amount);
    }
    return MathHelper.ceil(
        DamageUtil.getDamageLeft(
            this.wearer,
            amount,
            source,
            this.wearer.getArmor(),
            (float) this.wearer.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)));
  }

  @Nullable
  ActionResult tryRepair(final PlayerEntity player, final ItemStack held) {
    final ItemStack armour = this.worn();
    if (armour.isEmpty() || !armour.isDamaged() || !armour.getItem().canRepair(armour, held)) {
      return null;
    }
    held.decrementUnlessCreative(1, player);
    armour.setDamage(
        Math.max(0, armour.getDamage() - (int) (armour.getMaxDamage() * REPAIR_FRACTION)));
    this.wearer.playSoundIfNotSilent(SoundEvents.ITEM_WOLF_ARMOR_REPAIR);
    return ActionResult.SUCCESS;
  }
}
