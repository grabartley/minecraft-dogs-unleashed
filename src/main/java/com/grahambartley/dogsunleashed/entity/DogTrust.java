package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public final class DogTrust {

  private static final String ENTRY_PLAYER = "Player";
  private static final String ENTRY_TRUST = "Trust";

  private final UnleashedDogEntity dog;
  private final Map<UUID, Integer> trustByPlayer = new HashMap<>();
  private long lastFeedTime = Long.MIN_VALUE / 2;
  private long lastKillTime = Long.MIN_VALUE / 2;
  private int decayTicks;

  DogTrust(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static DogTrustRules.Food foodOf(final ItemStack stack) {
    final Item item = stack.getItem();
    if (item == Items.MUTTON
        || item == Items.BEEF
        || item == Items.RABBIT
        || item == Items.CHICKEN
        || item == Items.PORKCHOP) {
      return DogTrustRules.Food.RAW_MEAT;
    }
    if (item == Items.BONE) {
      return DogTrustRules.Food.BONE;
    }
    if (item == Items.COOKED_MUTTON
        || item == Items.COOKED_BEEF
        || item == Items.COOKED_RABBIT
        || item == Items.COOKED_CHICKEN
        || item == Items.COOKED_PORKCHOP) {
      return DogTrustRules.Food.COOKED_MEAT;
    }
    return DogTrustRules.Food.REFUSED;
  }

  public static DogTrustRules.Kill killOf(final LivingEntity killed) {
    if (killed instanceof Monster) {
      return DogTrustRules.Kill.HOSTILE;
    }
    if (killed instanceof RabbitEntity
        || killed instanceof ChickenEntity
        || killed instanceof FoxEntity
        || killed instanceof SheepEntity) {
      return DogTrustRules.Kill.SMALL_PREY;
    }
    return DogTrustRules.Kill.OTHER;
  }

  public boolean applies() {
    return this.dog.getBreed().nature().tamesThroughTrust() && !this.dog.isTamed();
  }

  public boolean canBeWon() {
    return this.applies() && this.dog.isBaby();
  }

  public int trustOf(final PlayerEntity player) {
    return this.trustByPlayer.getOrDefault(player.getUuid(), 0);
  }

  public @Nullable UUID mostTrusted() {
    return this.trustByPlayer.entrySet().stream()
        .filter(entry -> DogTrustRules.follows(entry.getValue()))
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  ActionResult feed(final PlayerEntity player, final ItemStack stack) {
    final DogTrustRules.Food food = foodOf(stack);
    if (!this.dog.isBaby()) {
      this.dog.getGrowl().warn();
      this.refuse();
      return ActionResult.SUCCESS;
    }
    final long now = this.dog.getWorld().getTime();
    if (food == DogTrustRules.Food.REFUSED
        || !DogTrustRules.cooledDown(now, this.lastFeedTime, DogTrustRules.FEED_COOLDOWN_TICKS)) {
      this.refuse();
      return ActionResult.SUCCESS;
    }
    stack.decrementUnlessCreative(1, player);
    this.lastFeedTime = now;
    this.gain(player, food.gain());
    return ActionResult.SUCCESS;
  }

  public void recordKill(final PlayerEntity player, final LivingEntity killed) {
    final DogTrustRules.Kill kill = killOf(killed);
    final long now = this.dog.getWorld().getTime();
    if (!this.canBeWon()
        || kill == DogTrustRules.Kill.OTHER
        || !DogTrustRules.cooledDown(now, this.lastKillTime, DogTrustRules.KILL_COOLDOWN_TICKS)) {
      return;
    }
    this.lastKillTime = now;
    this.gain(player, kill.gain());
  }

  public void spook(final PlayerEntity player) {
    if (this.canBeWon() && this.trustByPlayer.containsKey(player.getUuid())) {
      this.set(player.getUuid(), this.trustOf(player) - DogTrustRules.SPOOK_LOSS);
    }
  }

  public void forget(final PlayerEntity player) {
    this.trustByPlayer.remove(player.getUuid());
  }

  void tick() {
    if (!this.canBeWon() || this.trustByPlayer.isEmpty()) {
      return;
    }
    if (++this.decayTicks < DogTrustRules.DECAY_INTERVAL_TICKS) {
      return;
    }
    this.decayTicks = 0;
    for (final UUID playerId : Map.copyOf(this.trustByPlayer).keySet()) {
      final PlayerEntity player = this.dog.getWorld().getPlayerByUuid(playerId);
      if (player == null || player.distanceTo(this.dog) > DogTrustRules.DECAY_DISTANCE) {
        this.set(playerId, this.trustByPlayer.get(playerId) - 1);
      }
    }
  }

  void writeNbt(final NbtCompound nbt) {
    if (this.trustByPlayer.isEmpty()) {
      return;
    }
    final NbtList entries = new NbtList();
    this.trustByPlayer.forEach(
        (playerId, trust) -> {
          final NbtCompound entry = new NbtCompound();
          entry.putUuid(ENTRY_PLAYER, playerId);
          entry.putInt(ENTRY_TRUST, trust);
          entries.add(entry);
        });
    nbt.put(ModNbtKeys.TRUST, entries);
  }

  void readNbt(final NbtCompound nbt) {
    this.trustByPlayer.clear();
    for (final NbtElement element : nbt.getList(ModNbtKeys.TRUST, NbtElement.COMPOUND_TYPE)) {
      final NbtCompound entry = (NbtCompound) element;
      if (entry.containsUuid(ENTRY_PLAYER)) {
        this.set(entry.getUuid(ENTRY_PLAYER), entry.getInt(ENTRY_TRUST));
      }
    }
  }

  private void gain(final PlayerEntity player, final int amount) {
    final int before = this.trustOf(player);
    final int after = DogTrustRules.clamp(before + amount);
    this.set(player.getUuid(), after);
    if (DogTrustRules.isWon(after)) {
      this.trustByPlayer.clear();
      this.dog.getInteractions().tame(player);
      return;
    }
    if (DogTrustRules.milestoneCrossed(before, after) > 0) {
      this.dog
          .getWorld()
          .sendEntityStatus(this.dog, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
    }
  }

  private void refuse() {
    this.dog
        .getWorld()
        .sendEntityStatus(this.dog, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
  }

  private void set(final UUID playerId, final int trust) {
    final int clamped = DogTrustRules.clamp(trust);
    if (clamped == 0) {
      this.trustByPlayer.remove(playerId);
    } else {
      this.trustByPlayer.put(playerId, clamped);
    }
  }
}
