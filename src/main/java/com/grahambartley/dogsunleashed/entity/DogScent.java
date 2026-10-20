package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.scent.ScentLandmark;
import com.grahambartley.dogsunleashed.entity.scent.ScentLandmarkCache;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import com.grahambartley.dogsunleashed.entity.scent.ScentRange;
import com.grahambartley.dogsunleashed.entity.scent.ScentRule;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import com.grahambartley.dogsunleashed.entity.scent.ScentTargetFinder;
import com.grahambartley.dogsunleashed.entity.scent.ScentTargetFinders;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogScent extends DogComponent {

  private final UnleashedDogEntity dog;
  private final DogScentCues cues;
  private final ScentLandmarkCache landmarks = new ScentLandmarkCache();

  private @Nullable Identifier ruleId;
  private @Nullable Item scentItem;
  private int generation;
  private boolean settled;

  DogScent(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.cues = new DogScentCues(dog);
  }

  public boolean isTracking() {
    return this.scentItem != null;
  }

  public boolean isTrackingItem(final ItemStack stack) {
    return this.isTracking() && stack.isOf(this.scentItem);
  }

  public int generation() {
    return this.generation;
  }

  public DogScentCues cues() {
    return this.cues;
  }

  public @Nullable ScentRule rule() {
    if (this.ruleId == null) {
      return null;
    }
    return ScentRules.byId(this.dog.getWorld(), this.ruleId).map(RegistryEntry::value).orElse(null);
  }

  public @Nullable ScentLandmark landmark() {
    return this.landmarks.known().orElse(null);
  }

  public @Nullable ScentTargetFinder newFinder() {
    if (this.ruleId == null) {
      return this.landmarks.known().isPresent()
          ? ScentTargetFinders.known(this.dog, this.landmarks)
          : null;
    }
    final ScentRule rule = this.rule();
    return rule == null
        ? null
        : ScentTargetFinders.create(rule, this.dog, this.range(), this.landmarks);
  }

  public int range() {
    final DogGenome genome = this.dog.getGenome();
    return ScentRange.of(this.dog.getBreed(), genome == null ? List.of() : genome.composition());
  }

  public ScentPhase phase() {
    return ScentPhase.fromId(this.dog.getDataTracker().get(UnleashedDogEntity.SCENT_PHASE));
  }

  public void setPhase(final ScentPhase phase) {
    this.dog.getDataTracker().set(UnleashedDogEntity.SCENT_PHASE, phase.id());
  }

  void followMark(final ScentLandmark mark, final Item item) {
    this.start(null, item);
    this.landmarks.remember(mark);
  }

  void start(final @Nullable Identifier rule, final Item item) {
    this.ruleId = rule;
    this.scentItem = item;
    this.landmarks.clear();
    this.settled = false;
    this.generation++;
  }

  public void settle() {
    this.settled = true;
  }

  public void stop() {
    this.ruleId = null;
    this.scentItem = null;
    this.landmarks.clear();
    this.setPhase(ScentPhase.NONE);
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.scentItem == null || this.settled) {
      return;
    }
    nbt.putString(ModNbtKeys.SCENT_ITEM, Registries.ITEM.getId(this.scentItem).toString());
    if (this.ruleId != null) {
      nbt.putString(ModNbtKeys.SCENT_RULE, this.ruleId.toString());
    }
    this.landmarks
        .known()
        .ifPresent(
            mark -> nbt.putIntArray(ModNbtKeys.SCENT_LANDMARK, new int[] {mark.x(), mark.z()}));
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    this.stop();
    final Identifier rule = savedId(nbt, ModNbtKeys.SCENT_RULE);
    final Identifier item = savedId(nbt, ModNbtKeys.SCENT_ITEM);
    final ScentLandmark mark = savedLandmark(nbt);
    if (item == null || !Registries.ITEM.containsId(item) || (rule == null && mark == null)) {
      return;
    }
    this.start(rule, Registries.ITEM.get(item));
    if (mark != null) {
      this.landmarks.remember(mark);
    }
  }

  static @Nullable Identifier savedId(final NbtCompound nbt, final String key) {
    return nbt.contains(key, NbtElement.STRING_TYPE)
        ? Identifier.tryParse(nbt.getString(key))
        : null;
  }

  static @Nullable ScentLandmark savedLandmark(final NbtCompound nbt) {
    if (!nbt.contains(ModNbtKeys.SCENT_LANDMARK, NbtElement.INT_ARRAY_TYPE)) {
      return null;
    }
    final int[] column = nbt.getIntArray(ModNbtKeys.SCENT_LANDMARK);
    return column.length == 2 ? new ScentLandmark(column[0], column[1]) : null;
  }
}
