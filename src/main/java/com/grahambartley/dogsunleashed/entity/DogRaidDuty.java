package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModCosmeticItems;
import com.grahambartley.dogsunleashed.advancement.DogDefendedVillageCriterion;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.raid.Raid;
import org.jetbrains.annotations.Nullable;

public final class DogRaidDuty extends DogComponent {

  private final UnleashedDogEntity dog;
  private @Nullable Raid foughtRaid;
  private int checkCooldown;

  DogRaidDuty(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public void recordFight(final LivingEntity opponent) {
    final Raid raid = opponent instanceof RaiderEntity raider ? raider.getRaid() : null;
    if (RaidDutyRules.counts(this.dog.isTamed(), raid != null && raid.isActive())) {
      this.foughtRaid = raid;
    }
  }

  public boolean hasFoughtInARaid() {
    return this.foughtRaid != null;
  }

  @Override
  void tick() {
    if (this.foughtRaid == null || this.checkCooldown-- > 0) {
      return;
    }
    this.checkCooldown = RaidDutyRules.CHECK_INTERVAL_TICKS;
    switch (RaidDutyRules.outcome(
        this.foughtRaid.hasWon(), this.foughtRaid.hasLost() || this.foughtRaid.hasStopped())) {
      case REWARD -> {
        this.foughtRaid = null;
        this.rewardVictory();
      }
      case FORGET -> this.foughtRaid = null;
      case PENDING -> {}
    }
  }

  private void rewardVictory() {
    if (this.dog.getOwner() instanceof ServerPlayerEntity owner) {
      DogDefendedVillageCriterion.INSTANCE.trigger(owner);
    }
    final ItemStack rosette = new ItemStack(ModCosmeticItems.of(DogCosmetic.ROSETTE));
    final DogEquipmentHolder equipment = this.dog.getEquipmentHolder();
    if (equipment.getStack(DogEquipmentSlot.COSMETIC).isEmpty()) {
      equipment.setStack(DogEquipmentSlot.COSMETIC, rosette);
    } else {
      this.dog.dropStack(rosette);
    }
  }
}
