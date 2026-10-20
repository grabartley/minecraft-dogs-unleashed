package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class DogBond extends DogComponent {

  private final UnleashedDogEntity dog;
  private final Map<DogBondRules.Source, Integer> earnedToday =
      new EnumMap<>(DogBondRules.Source.class);
  private int points;
  private @Nullable UUID ownerId;
  private long day = DogBondRules.NEVER;
  private double walkedBlocks;
  private long lastNearOwner = DogBondRules.NEVER;
  private @Nullable Vec3d lastPos;

  DogBond(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public int points() {
    return this.points;
  }

  public DogBondStep step() {
    return DogBondStep.byId(this.dog.getDataTracker().get(UnleashedDogEntity.BOND_STEP));
  }

  public int earnedToday(final DogBondRules.Source source) {
    this.rollDay();
    return this.earnedToday.getOrDefault(source, 0);
  }

  public void reward(final DogBondRules.Source source) {
    this.reward(source, source.points());
  }

  public void reward(final DogBondRules.Source source, final int amount) {
    if (!this.dog.isTamed() || !this.ensureOwner()) {
      return;
    }
    this.rollDay();
    final int earned = this.earnedToday.getOrDefault(source, 0);
    final int gain = DogBondRules.allowedGain(source, amount, earned);
    if (gain <= 0) {
      return;
    }
    this.earnedToday.put(source, earned + gain);
    this.setPoints(this.points + gain);
  }

  public void setPoints(final int points) {
    this.points = DogBondRules.clamp(points);
    this.dog
        .getDataTracker()
        .set(UnleashedDogEntity.BOND_STEP, DogBondStep.ofPoints(this.points).ordinal());
  }

  void restore(final int points, final @Nullable UUID ownerId) {
    this.ownerId = ownerId;
    this.setPoints(points);
  }

  @Override
  void tick() {
    if (!this.dog.isTamed() || !this.ensureOwner()) {
      this.lastPos = null;
      return;
    }
    final LivingEntity owner = this.dog.getOwner();
    final Vec3d pos = this.dog.getPos();
    final Vec3d previous = this.lastPos;
    this.lastPos = pos;
    if (!(owner instanceof PlayerEntity player) || player.getWorld() != this.dog.getWorld()) {
      return;
    }
    final double distance = this.dog.distanceTo(player);
    if (previous != null && distance <= DogBondRules.WALK_RANGE && !this.dog.hasVehicle()) {
      this.walk(pos.subtract(previous).horizontalLength());
    }
    if (distance <= DogBondRules.NEAR_OWNER_RANGE) {
      this.noticeOwner();
    }
  }

  private void walk(final double step) {
    if (!DogBondRules.countsAsWalkStep(step)) {
      return;
    }
    this.walkedBlocks += step;
    final int walkPoints = DogBondRules.walkPoints(this.walkedBlocks);
    if (walkPoints > 0) {
      this.walkedBlocks -= walkPoints * DogBondRules.BLOCKS_PER_WALK_POINT;
      this.reward(DogBondRules.Source.WALK, walkPoints);
    }
  }

  private void noticeOwner() {
    final long now = this.dog.getWorld().getTime();
    final long absent = DogBondRules.absentFor(this.lastNearOwner, now);
    this.lastNearOwner = now;
    if (DogBondRules.greetsOnReturn(this.step(), absent)) {
      this.dog.triggerAnim(DogAnimationKeys.PLAY_BOW, DogAnimationKeys.PLAY_BOW);
      this.dog.getAmbienceEffects().startTailWag();
    }
  }

  private boolean ensureOwner() {
    final UUID current = this.dog.getOwnerUuid();
    if (current == null) {
      return false;
    }
    if (!Objects.equals(current, this.ownerId)) {
      this.ownerId = current;
      this.earnedToday.clear();
      this.walkedBlocks = 0.0;
      this.lastNearOwner = DogBondRules.NEVER;
      this.setPoints(
          DogBondRules.startingPoints(
              this.dog.getComponents().get(DogAging.class).hasSeniorParent()));
    }
    return true;
  }

  private void rollDay() {
    final long today = DogBondRules.dayOf(this.dog.getWorld().getTimeOfDay());
    if (today != this.day) {
      this.day = today;
      this.earnedToday.clear();
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.putInt(ModNbtKeys.BOND_POINTS, this.points);
    if (this.ownerId != null) {
      nbt.putUuid(ModNbtKeys.BOND_OWNER, this.ownerId);
    }
    nbt.putLong(ModNbtKeys.BOND_DAY, this.day);
    final NbtCompound earned = new NbtCompound();
    this.earnedToday.forEach((source, amount) -> earned.putInt(source.name(), amount));
    nbt.put(ModNbtKeys.BOND_EARNED, earned);
    nbt.putDouble(ModNbtKeys.BOND_WALKED, this.walkedBlocks);
    nbt.putLong(ModNbtKeys.BOND_LAST_NEAR_OWNER, this.lastNearOwner);
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.ownerId =
        nbt.containsUuid(ModNbtKeys.BOND_OWNER) ? nbt.getUuid(ModNbtKeys.BOND_OWNER) : null;
    this.setPoints(nbt.getInt(ModNbtKeys.BOND_POINTS));
    this.day = longOr(nbt, ModNbtKeys.BOND_DAY, DogBondRules.NEVER);
    this.earnedToday.clear();
    this.earnedToday.putAll(earnedFromSave(nbt.getCompound(ModNbtKeys.BOND_EARNED)));
    this.walkedBlocks = Math.max(0.0, nbt.getDouble(ModNbtKeys.BOND_WALKED));
    this.lastNearOwner = longOr(nbt, ModNbtKeys.BOND_LAST_NEAR_OWNER, DogBondRules.NEVER);
  }

  static Map<DogBondRules.Source, Integer> earnedFromSave(final NbtCompound earned) {
    final Map<DogBondRules.Source, Integer> bySource = new EnumMap<>(DogBondRules.Source.class);
    for (final DogBondRules.Source source : DogBondRules.Source.values()) {
      if (earned.contains(source.name(), NbtElement.NUMBER_TYPE)) {
        bySource.put(source, Math.max(0, earned.getInt(source.name())));
      }
    }
    return bySource;
  }

  static long longOr(final NbtCompound nbt, final String key, final long fallback) {
    return nbt.contains(key, NbtElement.NUMBER_TYPE) ? nbt.getLong(key) : fallback;
  }
}
