package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.item.pendant.PendantItem;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class DogPendants {

  private static final int LOYALTY_REGENERATION_TICKS = 900;
  private static final int LOYALTY_ABSORPTION_TICKS = 100;
  private static final int LOYALTY_FIRE_RESISTANCE_TICKS = 800;

  private final UnleashedDogEntity dog;

  DogPendants(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable PendantType worn() {
    final ItemStack stack = this.dog.getEquipmentHolder().getStack(DogEquipmentSlot.PENDANT);
    return stack.getItem() instanceof PendantItem pendant ? pendant.type() : null;
  }

  public boolean isWearing(final PendantType type) {
    return this.worn() == type;
  }

  boolean guardsAgainst(final DamageSource source) {
    final PendantType worn = this.worn();
    return worn != null && worn.guardedDamage() != null && source.isIn(worn.guardedDamage());
  }

  boolean isFireImmune() {
    return this.isWearing(PendantType.GUARDIAN_FIRE);
  }

  void tick() {
    if (this.isWearing(PendantType.GUARDIAN_DROWNING)) {
      this.dog.setAir(this.dog.getMaxAir());
    }
  }

  void saveIfFatal(final DamageSource source) {
    if (this.dog.getHealth() > 0.0f
        || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
        || !this.isWearing(PendantType.LOYALTY)) {
      return;
    }
    this.dog.getEquipmentHolder().setStack(DogEquipmentSlot.PENDANT, ItemStack.EMPTY);
    this.dog.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
    this.dog.setHealth(1.0f);
    this.dog.clearStatusEffects();
    this.dog.addStatusEffect(
        new StatusEffectInstance(StatusEffects.REGENERATION, LOYALTY_REGENERATION_TICKS, 1));
    this.dog.addStatusEffect(
        new StatusEffectInstance(StatusEffects.ABSORPTION, LOYALTY_ABSORPTION_TICKS, 1));
    this.dog.addStatusEffect(
        new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, LOYALTY_FIRE_RESISTANCE_TICKS, 0));
    this.dog.getWorld().sendEntityStatus(this.dog, EntityStatuses.USE_TOTEM_OF_UNDYING);
  }
}
